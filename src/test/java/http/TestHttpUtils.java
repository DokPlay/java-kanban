package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;

/** sprint-9: общие HTTP-хелперы для тестов (без копипасты в каждом тесте). */
public final class TestHttpUtils {

  private TestHttpUtils() {}

  public static HttpResponse<String> get(String baseUrl, String path)
      throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
  }

  public static HttpResponse<String> post(String baseUrl, String path, String body)
      throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
  }

  public static HttpResponse<String> delete(String baseUrl, String path)
      throws IOException, InterruptedException {
    return client()
        .send(
            HttpRequest.newBuilder(URI.create(baseUrl + path)).DELETE().build(),
            HttpResponse.BodyHandlers.ofString());
  }

  /** Наивно берёт id первого элемента массива JSON. Удобно для тестов. */
  public static int firstIdFromArray(String json) {
    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
    JsonObject o = arr.get(0).getAsJsonObject();
    return o.get("id").getAsInt();
  }

  /** Единый клиент — не плодим экземпляры. */
  public static HttpClient client() {
    return HttpClient.newHttpClient();
  }
}
