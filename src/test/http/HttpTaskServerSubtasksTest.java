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
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.*;

class HttpTaskServerSubtasksTest extends BaseHttpTest {

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
  void subtask_crud_and_overlap406() throws Exception {
    // epic
    Epic e = new Epic("E", "");
    e.setStatus(Status.NEW);
    assertEquals(201, post("/epics", gson.toJson(e)).statusCode());
    int epicId = firstIdFromArray(get("/epics").body());

    // create subtask
    Subtask s =
        new Subtask(
            "S1",
            "",
            Status.NEW,
            Duration.ofMinutes(10),
            LocalDateTime.of(2035, 1, 1, 12, 0),
            epicId);
    assertEquals(201, post("/subtasks", gson.toJson(s)).statusCode());
    int sid = firstIdFromArray(get("/subtasks").body());

    // get by id
    assertEquals(200, get("/subtasks/" + sid).statusCode());

    // list epic's subtasks -> 1
    JsonArray arr =
        JsonParser.parseString(get("/epics/" + epicId + "/subtasks").body()).getAsJsonArray();
    assertEquals(1, arr.size());

    // overlap by creating another subtask at same time for same epic
    Subtask clash =
        new Subtask(
            "S2",
            "",
            Status.NEW,
            Duration.ofMinutes(15),
            LocalDateTime.of(2035, 1, 1, 12, 5),
            epicId);
    assertEquals(406, post("/subtasks", gson.toJson(clash)).statusCode());

    // update existing
    s.setId(sid);
    s.setStatus(Status.IN_PROGRESS);
    s.setStartTime(LocalDateTime.of(2035, 1, 1, 12, 30));
    s.setDuration(Duration.ofMinutes(20));
    assertEquals(201, post("/subtasks", gson.toJson(s)).statusCode());

    // delete
    assertEquals(200, delete("/subtasks/" + sid).statusCode());
    assertEquals(404, get("/subtasks/" + sid).statusCode());
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
