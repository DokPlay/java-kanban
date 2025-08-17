package http;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.*;

class PrioritizedUniquenessTest extends BaseHttpTest {

  private static final LocalDateTime BASE = LocalDateTime.of(2035, 1, 1, 0, 0);

  @BeforeEach
  void setUp() throws IOException {
    setUpBase();
  }

  @AfterEach
  void tearDown() {
    tearDownBase();
  }

  @Test
  void updateSubtaskDoesNotDuplicateInPrioritized() throws Exception {
    // 1) создаём эпик
    Epic e = new Epic("E", "");
    e.setStatus(Status.NEW);
    assertEquals(201, post(baseUrl + "/epics", HttpTaskServer.getGson().toJson(e)).statusCode());

    int epicId =
        external
            ? firstIdFromArray(get(baseUrl + "/epics").body())
            : manager.getEpics().get(0).getId();

    // 2) создаём сабтаску на 12:00
    Subtask s = new Subtask("S", "", Status.NEW, Duration.ofMinutes(10), BASE.withHour(12), epicId);
    assertEquals(201, post(baseUrl + "/subtasks", gson.toJson(s)).statusCode());

    int subId =
        external
            ? firstIdFromArray(get(baseUrl + "/subtasks").body())
            : manager.getSubtasks().get(0).getId();

    // 3) апдейт той же сабтаски на 12:30
    s.setId(subId);
    s.setStatus(Status.IN_PROGRESS);
    s.setStartTime(BASE.withHour(12).withMinute(30));
    s.setDuration(Duration.ofMinutes(15));
    assertEquals(201, post(baseUrl + "/subtasks", gson.toJson(s)).statusCode());

    // 4) проверка /prioritized — ровно одно вхождение id
    HttpResponse<String> pr = get(baseUrl + "/prioritized");
    assertEquals(200, pr.statusCode());
    long occurrences =
        JsonParser.parseString(pr.body()).getAsJsonArray().asList().stream()
            .map(JsonElement::getAsJsonObject)
            .filter(o -> o.has("id") && o.get("id").getAsInt() == subId)
            .count();
    assertEquals(1, occurrences);
  }

  /* helpers */
  private static HttpResponse<String> get(String url) throws IOException, InterruptedException {
    return HttpClient.newHttpClient()
        .send(
            HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private static HttpResponse<String> post(String url, String body)
      throws IOException, InterruptedException {
    return HttpClient.newHttpClient()
        .send(
            HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private static int firstIdFromArray(String json) {
    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    if (arr.size() == 0) throw new IllegalStateException("empty array");
    return arr.get(0).getAsJsonObject().get("id").getAsInt();
  }
}
