package http;

import static http.HttpUtil.isNewId;
import static http.HttpUtil.parseIdOrNull;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TaskValidationException;
import java.io.IOException;
import java.net.URI;
import manager.TaskManager;
import model.Subtask;

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
      String[] parts = uri.getPath().split("/"); // ["", "subtasks", ...]
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
    if (parts.length == 2) { // /subtasks
      sendOk(exchange, gson.toJson(manager.getSubtasks()));
      return;
    }
    if (parts.length == 3) { // /subtasks/{id}
      Integer id = parseIdOrNull(parts[2]);
      if (id == null) {
        sendNotFound(exchange, "incorrect id");
        return;
      }
      try {
        sendOk(exchange, gson.toJson(manager.getSubtask(id)));
      } catch (NotFoundException nf) {
        sendNotFound(exchange, nf.getMessage());
      }
      return;
    }
    sendNotFound(exchange, "incorrect path");
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String body = readBody(exchange);
    Subtask incoming = gson.fromJson(body, Subtask.class);
    if (incoming == null) {
      sendServerError(exchange, "empty body");
      return;
    }
    try {
      if (isNewId(incoming.getId())) {
        manager.addNewSubtask(incoming); // бросит NotFound, если epic не найден
      } else {
        manager.updateSubtask(incoming); // NotFound → 404
      }
      sendCreated(exchange);
    } catch (TaskValidationException overlap) {
      sendHasOverlaps(exchange, overlap.getMessage());
    } catch (NotFoundException nf) {
      sendNotFound(exchange, nf.getMessage());
    }
  }

  private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
    if (parts.length != 3) {
      sendNotFound(exchange, "incorrect path");
      return;
    }
    Integer id = parseIdOrNull(parts[2]);
    if (id == null) {
      sendNotFound(exchange, "incorrect id");
      return;
    }
    try {
      manager.removeSubtask(id); // NotFound → 404
      sendOk(exchange, "\"deleted\"");
    } catch (NotFoundException nf) {
      sendNotFound(exchange, nf.getMessage());
    }
  }
}
