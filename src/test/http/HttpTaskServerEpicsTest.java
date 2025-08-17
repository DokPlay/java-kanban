package http;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import model.Epic;
import model.Status;
import org.junit.jupiter.api.*;

class HttpTaskServerEpicsTest extends BaseHttpTest {

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
  void epic_crud_and_subtasks_emptyList() throws Exception {
    Epic e = new Epic("Epic A", "big");
    e.setStatus(Status.NEW);
    assertEquals(201, post("/epics", gson.toJson(e)).statusCode());

    int id = firstIdFromArray(get("/epics").body());
    assertEquals(200, get("/epics/" + id).statusCode());

    // update title/description
    Epic upd = new Epic("Epic A*", "bigger");
    upd.setId(id);
    upd.setStatus(Status.NEW);
    assertEquals(201, post("/epics", gson.toJson(upd)).statusCode());

    // subtasks of epic -> []
    String subs = get("/epics/" + id + "/subtasks").body();
    JsonArray arr = JsonParser.parseString(subs).getAsJsonArray();
    assertEquals(0, arr.size());

    assertEquals(200, delete("/epics/" + id).statusCode());
    assertEquals(404, get("/epics/" + id).statusCode());
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
