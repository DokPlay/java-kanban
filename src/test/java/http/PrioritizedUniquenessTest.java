package http;

import static http.TestHttpUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.*;

class PrioritizedUniquenessTest extends BaseHttpTest {

  private static final LocalDateTime BASE = LocalDateTime.of(2035, 1, 1, 0, 0);
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
  void updateSubtaskDoesNotDuplicateInPrioritized() throws Exception {
    // 1) epic
    Epic e = new Epic("E", "");
    e.setStatus(Status.NEW);
    assertEquals(201, post(baseUrl, "/epics", gson.toJson(e)).statusCode());

    int epicId =
        external
            ? firstIdFromArray(get(baseUrl, "/epics").body())
            : manager.getEpics().get(0).getId();

    // 2) sub 12:00
    Subtask s = new Subtask("S", "", Status.NEW, Duration.ofMinutes(10), BASE.withHour(12), epicId);
    assertEquals(201, post(baseUrl, "/subtasks", gson.toJson(s)).statusCode());

    int subId =
        external
            ? firstIdFromArray(get(baseUrl, "/subtasks").body())
            : manager.getSubtasks().get(0).getId();

    // 3) update same sub to 12:30
    s.setId(subId);
    s.setStatus(Status.IN_PROGRESS);
    s.setStartTime(BASE.withHour(12).withMinute(30));
    s.setDuration(Duration.ofMinutes(15));
    assertEquals(201, post(baseUrl, "/subtasks", gson.toJson(s)).statusCode());

    // 4) /prioritized — ровно одно вхождение id
    String prBody = get(baseUrl, "/prioritized").body();
    JsonArray arr = JsonParser.parseString(prBody).getAsJsonArray();
    int occurrences = 0;
    for (int i = 0; i < arr.size(); i++) {
      var obj = arr.get(i).getAsJsonObject();
      if (obj.has("id") && obj.get("id").getAsInt() == subId) {
        occurrences++;
      }
    }
    assertEquals(1, occurrences);
  }
}
