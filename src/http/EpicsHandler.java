package http;

import static http.HttpUtil.isNewId;
import static http.HttpUtil.parseIdOrNull;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import java.io.IOException;
import java.net.URI;
import manager.TaskManager;
import model.Epic;

/** /epics, /epics/{id}, /epics/{id}/subtasks */
public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

  private final TaskManager manager;
  private final Gson gson;

  public EpicsHandler(TaskManager manager, Gson gson) {
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
    if (parts.length == 2) { // /epics
      sendOk(exchange, gson.toJson(manager.getEpics()));
      return;
    }
    if (parts.length == 3) { // /epics/{id}
      Integer id = parseIdOrNull(parts[2]);
      if (id == null) {
        sendNotFound(exchange, "incorrect id");
        return;
      }
      try {
        sendOk(exchange, gson.toJson(manager.getEpic(id)));
      } catch (NotFoundException nf) {
        sendNotFound(exchange, nf.getMessage());
      }
      return;
    }
    if (parts.length == 4 && "subtasks".equals(parts[3])) { // /epics/{id}/subtasks
      Integer epicId = parseIdOrNull(parts[2]);
      if (epicId == null) {
        sendNotFound(exchange, "incorrect id");
        return;
      }
      try {
        // вызов getEpic для явного 404, затем выдаём список
        manager.getEpic(epicId);
        sendOk(exchange, gson.toJson(manager.getEpicSubtasks(epicId)));
      } catch (NotFoundException nf) {
        sendNotFound(exchange, nf.getMessage());
      }
      return;
    }
    sendNotFound(exchange, "incorrect path");
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String body = readBody(exchange);
    Epic epic = gson.fromJson(body, Epic.class);
    if (epic == null) {
      sendServerError(exchange, "empty body");
      return;
    }
    try {
      if (isNewId(epic.getId())) {
        manager.addNewEpic(epic);
      } else {
        manager.updateEpic(epic); // NotFound → 404
      }
      sendCreated(exchange);
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
      manager.removeEpic(id); // NotFound → 404
      sendOk(exchange, "\"deleted\"");
    } catch (NotFoundException nf) {
      sendNotFound(exchange, nf.getMessage());
    }
  }
}
