package http; // sprint 9

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import manager.TaskManager;

/** GET /prioritized — задачи по приоритету. sprint 9 */
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendServerError(exchange, "Unsupported method");
            return;
        }
        try {
            sendOk(exchange, gson.toJson(manager.getPrioritizedTasks()));
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }
}
