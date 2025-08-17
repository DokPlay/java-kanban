package http;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.*;

class HttpTaskServerHistoryPrioritizedTest extends BaseHttpTest {

    private Gson gson;

    private static final LocalDateTime BASE = LocalDateTime.of(2035, 1, 1, 0, 0);

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
    void history_and_prioritized_ok() throws Exception {
        // task t1 10:00
        Task t1 = new Task("T1", "D", Status.NEW);
        t1.setDuration(Duration.ofMinutes(10));
        t1.setStartTime(BASE.withHour(10));
        assertEquals(201, post("/tasks", gson.toJson(t1)).statusCode());

        // task t2 11:00
        Task t2 = new Task("T2", "D", Status.NEW);
        t2.setDuration(Duration.ofMinutes(10));
        t2.setStartTime(BASE.withHour(11));
        assertEquals(201, post("/tasks", gson.toJson(t2)).statusCode());

        // epic + sub 12:00
        Epic e = new Epic("E", "");
        e.setStatus(Status.NEW);
        assertEquals(201, post("/epics", gson.toJson(e)).statusCode());
        int epicId = firstId(get("/epics").body());

        Subtask s = new Subtask("S", "", Status.NEW, Duration.ofMinutes(20), BASE.withHour(12), epicId);
        assertEquals(201, post("/subtasks", gson.toJson(s)).statusCode());

        // дергаем /tasks/{id}, /epics/{id}, /subtasks/{id} -> попадут в историю
        int t1Id = byIndex(get("/tasks").body(), 0);
        int t2Id = byIndex(get("/tasks").body(), 1);
        int sId  = firstId(get("/subtasks").body());

        assertEquals(200, get("/tasks/" + t1Id).statusCode());
        assertEquals(200, get("/epics/" + epicId).statusCode());
        assertEquals(200, get("/subtasks/" + sId).statusCode());

        // history should be non-empty (contains 3 entries)
        JsonArray hist = JsonParser.parseString(get("/history").body()).getAsJsonArray();
        assertTrue(hist.size() >= 3);

        // prioritized — строго по возрастанию startTime: 10:00, 11:00, 12:00
        JsonArray pr = JsonParser.parseString(get("/prioritized").body()).getAsJsonArray();
        assertTrue(pr.size() >= 3);
        long first = startMillis(pr.get(0));
        long second = startMillis(pr.get(1));
        long third = startMillis(pr.get(2));
        assertTrue(first <= second && second <= third, "prioritized must be sorted by startTime");
    }

    /* ===== helpers ===== */

    private static long startMillis(JsonElement el) {
        // адаптер сериализует LocalDateTime в строку ISO; парсить до millis не обязательно, достаточно лекс. сравнения,
        // но для простоты — используем java.time
        String s = el.getAsJsonObject().get("startTime").getAsString();
        return java.time.LocalDateTime.parse(s).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private int firstId(String json) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        return arr.get(0).getAsJsonObject().get("id").getAsInt();
    }

    private int byIndex(String json, int idx) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        return arr.get(idx).getAsJsonObject().get("id").getAsInt();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return client().send(
                HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body)
            throws IOException, InterruptedException {
        return client().send(
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static HttpClient client() {
        return HttpClient.newHttpClient();
    }
}
