package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskValidationException;
import java.io.IOException;
import java.net.URI;
import manager.TaskManager;
import model.Subtask;

/** /subtasks и /subtasks/{id} — sprint 9 */
public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

  private final TaskManager manager;
  private final Gson gson;

  public SubtasksHandler(TaskManager manager, Gson gson) {
    this.manager = manager;
    this.gson = gson;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String method = exchange.getRequestMethod();
      URI uri = exchange.getRequestURI();
      String[] parts = uri.getPath().split("/");
      switch (method) {
        case "GET" -> handleGet(exchange, parts);
        case "POST" -> handlePost(exchange);
        case "DELETE" -> handleDelete(exchange, parts);
        default -> sendServerError(exchange, "Unsupported method");
      }
    } catch (Exception e) {
      sendServerError(exchange, e.getMessage() == null ? e.toString() : e.getMessage());
    }
  }

  private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
    if (parts.length == 2) { // /subtasks
      sendOk(exchange, gson.toJson(manager.getSubtasks()));
      return;
    }
    if (parts.length == 3) { // /subtasks/{id}
      Integer id = parseId(parts[2]);
      if (id == null) {
        sendNotFound(exchange, "incorrect id");
        return;
      }
      var st = manager.getSubtask(id);
      if (st == null) {
        sendNotFound(exchange, "subtask " + id + " not found");
      } else {
        sendOk(exchange, gson.toJson(st));
      }
      return;
    }
    sendNotFound(exchange, "incorrect path");
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String body = readBody(exchange);
    Subtask st = gson.fromJson(body, Subtask.class);
    if (st == null) {
      sendServerError(exchange, "empty body");
      return;
    }
    try {
      // sprint9: проверяем, что эпик существует — иначе 404 (а не 500)
      Integer epicId = st.getEpicId(); // sprint9
      if (epicId == null || manager.getEpic(epicId) == null) { // sprint9
        sendNotFound(exchange, "epic " + epicId + " not found"); // sprint9
        return; // sprint9
      }

      if (isNewId(st.getId())) {
        manager.addNewSubtask(st); // create
        sendCreated(exchange);
      } else {
        // sprint9: update только если подзадача действительно существует — иначе 404
        Integer id = st.getId(); // sprint9
        if (manager.getSubtask(id) == null) { // sprint9
          sendNotFound(exchange, "subtask " + id + " not found"); // sprint9
          return; // sprint9
        }
        manager.updateSubtask(st);
        sendCreated(exchange);
      }
    } catch (TaskValidationException overlap) {
      sendHasOverlaps(exchange, overlap.getMessage());
    } catch (IllegalArgumentException | java.util.NoSuchElementException e) { // sprint9
      // На всякий случай маппим возможные исключения из менеджера в 404 — чтобы не было 500. //
      // sprint9
      sendNotFound(exchange, e.getMessage() == null ? "not found" : e.getMessage()); // sprint9
    }
  }

  private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
    if (parts.length != 3) {
      sendNotFound(exchange, "incorrect path");
      return;
    }
    Integer id = parseId(parts[2]);
    if (id == null) {
      sendNotFound(exchange, "incorrect id");
      return;
    }
    if (manager.getSubtask(id) == null) {
      sendNotFound(exchange, "subtask " + id + " not found");
      return;
    }
    manager.removeSubtask(id);
    sendOk(exchange, "\"deleted\"");
  }

  private boolean isNewId(Integer id) {
    return id == null || id == 0;
  }

  private Integer parseId(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
