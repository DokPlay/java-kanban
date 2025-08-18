package http;

import static http.TestHttpUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Status;
import model.Task;
import org.junit.jupiter.api.*;

class HttpTaskServerTasksTest extends BaseHttpTest {

  @BeforeEach
  void setUp() throws IOException {
    setUpBase(); // BaseHttpTest заполняет protected gson, baseUrl и т.д.
  }

  @AfterEach
  void tearDown() {
    tearDownBase();
  }

  @Test
  void addGetUpdateDelete_Task_ok_and_overlap406() throws Exception {
    Task t1 = new Task("T1", "D1", Status.NEW);
    t1.setDuration(Duration.ofMinutes(10));
    t1.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 0));
    assertEquals(201, post(baseUrl, "/tasks", gson.toJson(t1)).statusCode());

    int id = firstIdFromArray(get(baseUrl, "/tasks").body());
    assertTrue(id > 0);

    assertEquals(200, get(baseUrl, "/tasks/" + id).statusCode());

    t1.setId(id);
    t1.setStatus(Status.IN_PROGRESS);
    t1.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 30));
    t1.setDuration(Duration.ofMinutes(15));
    assertEquals(201, post(baseUrl, "/tasks", gson.toJson(t1)).statusCode());

    Task clash = new Task("Clash", "overlap", Status.NEW);
    clash.setDuration(Duration.ofMinutes(20));
    clash.setStartTime(LocalDateTime.of(2035, 1, 1, 10, 35));
    assertEquals(406, post(baseUrl, "/tasks", gson.toJson(clash)).statusCode());

    assertEquals(200, delete(baseUrl, "/tasks/" + id).statusCode());
    assertEquals(404, get(baseUrl, "/tasks/" + id).statusCode());
  }
}
