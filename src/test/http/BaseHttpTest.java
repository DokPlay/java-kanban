package http;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import manager.InMemoryTaskManager;
import manager.TaskManager;

public abstract class BaseHttpTest {
  protected TaskManager manager; // null в «внешнем» режиме
  protected HttpTaskServer server; // не создаём, если работаем с внешним
  protected Gson gson;
  protected String baseUrl;
  protected boolean external;

  protected void setUpBase() throws IOException {
    gson = HttpTaskServer.getGson();
    String prop = System.getProperty("BASE_URL"); // -DBASE_URL=http://localhost:8080
    external = prop != null && !prop.isBlank();

    if (external && isReachable(prop)) {
      baseUrl = prop;
      System.out.println("[tests] Using EXTERNAL server: " + baseUrl);
      return;
    }

    manager = new InMemoryTaskManager();
    server = new HttpTaskServer(manager, 0); // порт 0 → свободный
    server.start();
    baseUrl = "http://localhost:" + server.getPort();
    external = false;
    System.out.println("[tests] Using LOCAL server: " + baseUrl);
  }

  protected void tearDownBase() {
    if (!external && server != null) {
      server.stop();
      server = null;
      System.out.println("[tests] Local server stopped");
    }
  }

  private static boolean isReachable(String baseUrl) {
    try {
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(800)).build();
      HttpRequest req =
          HttpRequest.newBuilder(URI.create(baseUrl + "/tasks"))
              .timeout(Duration.ofMillis(1200))
              .GET()
              .build();
      HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
      return resp.statusCode() > 0;
    } catch (Exception e) {
      System.out.println("[tests] BASE_URL not reachable, fallback to local: " + e);
      return false;
    }
  }
}
