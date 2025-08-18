package http;

import static http.TestHttpUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
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
    assertEquals(201, post(baseUrl, "/epics", gson.toJson(e)).statusCode());

    int id = firstIdFromArray(get(baseUrl, "/epics").body());
    assertEquals(200, get(baseUrl, "/epics/" + id).statusCode());

    // update title/description
    Epic upd = new Epic("Epic A*", "bigger");
    upd.setId(id);
    upd.setStatus(Status.NEW);
    assertEquals(201, post(baseUrl, "/epics", gson.toJson(upd)).statusCode());

    // subtasks of epic -> []
    String subs = get(baseUrl, "/epics/" + id + "/subtasks").body();
    JsonArray arr = JsonParser.parseString(subs).getAsJsonArray();
    assertEquals(0, arr.size());

    assertEquals(200, delete(baseUrl, "/epics/" + id).statusCode());
    assertEquals(404, get(baseUrl, "/epics/" + id).statusCode());
  }
}
