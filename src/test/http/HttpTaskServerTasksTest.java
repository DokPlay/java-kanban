package http;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Status;
import model.Task;
import org.junit.jupiter.api.*;

class HttpTaskServerTasksTest extends BaseHttpTest {

  private Gson gson;

  @BeforeEach
  void setUp() throws IOException {
    setUpBase();
    gson = HttpTaskServer.getGson();
  }

  @AfterEach
  void tearDown() {
    tearDownBase();
  }

  @Test
  void addGetUpdateDelete_Task_ok_and_overlap406() throws Exception {
    // create
    Task t1 = new Task("T1", "D1", Status.NEW);
    t1.setDuration(Duration.ofMinutes(10));
    t1.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 0));
    assertEquals(201, post("/tasks", gson.toJson(t1)).statusCode());

    // list -> id
    int id = firstIdFromArray(get("/tasks").body());
    assertTrue(id > 0);

    // get by id
    assertEquals(200, get("/tasks/" + id).statusCode());

    // update same id
    t1.setId(id);
    t1.setStatus(Status.IN_PROGRESS);
    t1.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 30));
    t1.setDuration(Duration.ofMinutes(15));
    assertEquals(201, post("/tasks", gson.toJson(t1)).statusCode());

    // try overlap with previous time 10:30–10:45 → ok exists; now create an overlapping new one at
    // 10:35
    Task clash = new Task("Clash", "overlap", Status.NEW);
    clash.setDuration(Duration.ofMinutes(20));
    clash.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 35));
    assertEquals(406, post("/tasks", gson.toJson(clash)).statusCode());

    // delete
    assertEquals(200, delete("/tasks/" + id).statusCode());
    assertEquals(404, get("/tasks/" + id).statusCode());
  }

  /* ===== helpers ===== */

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> post(String path, String body)
      throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> delete(String path) throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path)).DELETE().build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private static int firstIdFromArray(String json) {
    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    JsonObject o = arr.get(0).getAsJsonObject();
    return o.get("id").getAsInt();
  }

  private static HttpClient client() {
    return HttpClient.newHttpClient();
  }
}
