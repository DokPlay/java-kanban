package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskValidationException;
import java.io.IOException;
import java.net.URI;
import manager.TaskManager;
import model.Task;

/** /tasks и /tasks/{id} — sprint 9 */
public class TasksHandler extends BaseHttpHandler implements HttpHandler {

  private final TaskManager manager;
  private final Gson gson;

  public TasksHandler(TaskManager manager, Gson gson) {
    this.manager = manager;
    this.gson = gson;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String method = exchange.getRequestMethod();
      URI uri = exchange.getRequestURI();
      String[] parts = uri.getPath().split("/"); // ["", "tasks", ...]
      switch (method) {
        case "GET" -> handleGet(exchange, parts);
        case "POST" -> handlePost(exchange);
        case "DELETE" -> handleDelete(exchange, parts);
        default -> sendServerError(exchange, "Unsupported method " + method);
      }
    } catch (Exception e) {
      sendServerError(exchange, e.getMessage() == null ? e.toString() : e.getMessage());
    }
  }

  private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
    if (parts.length == 2) { // /tasks
      sendOk(exchange, gson.toJson(manager.getTasks()));
      return;
    }
    if (parts.length == 3) { // /tasks/{id}
      Integer id = parseId(parts[2]);
      if (id == null) {
        sendNotFound(exchange, "incorrect id");
        return;
      }
      Task task = manager.getTask(id);
      if (task == null) {
        sendNotFound(exchange, "task " + id + " not found");
      } else {
        sendOk(exchange, gson.toJson(task));
      }
      return;
    }
    sendNotFound(exchange, "incorrect path");
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String body = readBody(exchange);
    Task incoming = gson.fromJson(body, Task.class);
    if (incoming == null) {
      sendServerError(exchange, "empty body");
      return;
    }
    try {
      if (isNewId(incoming.getId())) {
        manager.addNewTask(incoming);
        sendCreated(exchange);
      } else {
        // sprint9: обновляем ТОЛЬКО существующую задачу — иначе 404
        Integer id = incoming.getId(); // sprint9
        if (manager.getTask(id) == null) { // sprint9
          sendNotFound(exchange, "task " + id + " not found"); // sprint9
          return; // sprint9
        }
        manager.updateTask(incoming);
        sendCreated(exchange);
      }
    } catch (TaskValidationException overlap) {
      sendHasOverlaps(exchange, overlap.getMessage());
    } catch (IllegalArgumentException | java.util.NoSuchElementException e) { // sprint9
      // На случай если менеджер бросит «не найдено» — отдаём 404, а не 500. // sprint9
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
    if (manager.getTask(id) == null) {
      sendNotFound(exchange, "task " + id + " not found");
      return;
    }
    manager.removeTask(id);
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
