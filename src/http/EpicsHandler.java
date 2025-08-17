package http; // sprint 9

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

/** /epics, /epics/{id}, /epics/{id}/subtasks — sprint 9 */
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
            Integer id = parseId(parts[2]);
            if (id == null) {
                sendNotFound(exchange, "incorrect id");
                return;
            }
            Epic epic = manager.getEpic(id);
            if (epic == null) {
                sendNotFound(exchange, "epic " + id + " not found");
            } else {
                sendOk(exchange, gson.toJson(epic));
            }
            return;
        }
        if (parts.length == 4 && "subtasks".equals(parts[3])) { // /epics/{id}/subtasks
            Integer epicId = parseId(parts[2]);
            if (epicId == null) {
                sendNotFound(exchange, "incorrect id");
                return;
            }
            Epic epic = manager.getEpic(epicId);
            if (epic == null) {
                sendNotFound(exchange, "epic " + epicId + " not found");
                return;
            }

            // sprint9: безопасная выборка, если у эпика subtaskIds == null
            List<Subtask> subs =
                    manager.getSubtasks().stream()
                            .filter(st -> Objects.equals(st.getEpicId(), epicId))
                            .collect(Collectors.toList());

            sendOk(exchange, gson.toJson(subs));
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
                sendCreated(exchange);
            } else {
                // sprint9: обновляем ТОЛЬКО существующий эпик — иначе 404
                Integer id = epic.getId(); // sprint9
                if (manager.getEpic(id) == null) { // sprint9
                    sendNotFound(exchange, "epic " + id + " not found"); // sprint9
                    return; // sprint9
                }
                manager.updateEpic(epic);
                sendCreated(exchange);
            }
        } catch (IllegalArgumentException | java.util.NoSuchElementException e) { // sprint9
            // На случай, если менеджер бросит «не найдено», возвращаем 404, а не 500. // sprint9
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
        if (manager.getEpic(id) == null) {
            sendNotFound(exchange, "epic " + id + " not found");
            return;
        }
        manager.removeEpic(id);
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
