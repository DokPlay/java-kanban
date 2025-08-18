package http;

import static http.TestHttpUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.*;

class HttpTaskServerSubtasksTest extends BaseHttpTest {

  @BeforeEach
  void setUp() throws IOException {
    setUpBase(); // gson уже инициализирован в BaseHttpTest
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
    assertEquals(201, post(baseUrl, "/epics", gson.toJson(e)).statusCode());
    int epicId = firstIdFromArray(get(baseUrl, "/epics").body());

    // create subtask
    Subtask s =
        new Subtask(
            "S1",
            "",
            Status.NEW,
            Duration.ofMinutes(10),
            LocalDateTime.of(2035, 1, 1, 12, 0),
            epicId);
    assertEquals(201, post(baseUrl, "/subtasks", gson.toJson(s)).statusCode());
    int sid = firstIdFromArray(get(baseUrl, "/subtasks").body());

    // get by id
    assertEquals(200, get(baseUrl, "/subtasks/" + sid).statusCode());

    // list epic's subtasks -> 1
    JsonArray arr =
        JsonParser.parseString(get(baseUrl, "/epics/" + epicId + "/subtasks").body())
            .getAsJsonArray();
    assertEquals(1, arr.size());

    // overlap
    Subtask clash =
        new Subtask(
            "S2",
            "",
            Status.NEW,
            Duration.ofMinutes(15),
            LocalDateTime.of(2035, 1, 1, 12, 5),
            epicId);
    assertEquals(406, post(baseUrl, "/subtasks", gson.toJson(clash)).statusCode());

    // update existing
    s.setId(sid);
    s.setStatus(Status.IN_PROGRESS);
    s.setStartTime(LocalDateTime.of(2035, 1, 1, 12, 30));
    s.setDuration(Duration.ofMinutes(20));
    assertEquals(201, post(baseUrl, "/subtasks", gson.toJson(s)).statusCode());

    // delete
    assertEquals(200, delete(baseUrl, "/subtasks/" + sid).statusCode());
    assertEquals(404, get(baseUrl, "/subtasks/" + sid).statusCode());
  }
}
