package http; // sprint 9

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Общая база для HTTP-обработчиков. Содержит вспомогательные методы. Sprint 9 */
public abstract class BaseHttpHandler {

  protected String readBody(HttpExchange exchange) throws IOException { // sprint 9
    try (InputStream is = exchange.getRequestBody()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  protected void sendJson(HttpExchange exchange, int status, String json)
      throws IOException { // sprint 9
    byte[] resp = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
    exchange.sendResponseHeaders(status, resp.length);
    exchange.getResponseBody().write(resp);
    exchange.close();
  }

  protected void sendOk(HttpExchange exchange, String json) throws IOException { // 200
    sendJson(exchange, 200, json);
  }

  protected void sendCreated(HttpExchange exchange) throws IOException { // 201
    sendJson(exchange, 201, "\"created\"");
  }

  protected void sendNotFound(HttpExchange exchange, String message) throws IOException { // 404
    sendJson(exchange, 404, "{\"error\":\"" + escape(message) + "\"}");
  }

  protected void sendHasOverlaps(HttpExchange exchange, String message) throws IOException { // 406
    sendJson(exchange, 406, "{\"error\":\"" + escape(message) + "\"}");
  }

  protected void sendServerError(HttpExchange exchange, String message) throws IOException { // 500
    sendJson(exchange, 500, "{\"error\":\"" + escape(message) + "\"}");
  }

  private String escape(String s) {
    return s == null ? "" : s.replace("\"", "\\\"");
  }
}
