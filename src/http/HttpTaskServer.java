package http; // sprint 9

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import manager.Managers;
import manager.TaskManager;

public class HttpTaskServer {

    public static final int DEFAULT_PORT = 8080; // sprint9

    private HttpServer server;                   // sprint9: был final → теперь ленивое создание
    private final TaskManager manager;
    private final Gson gson;
    private final int port;                      // sprint9: порт хранится в поле

    public HttpTaskServer(TaskManager manager) { // для прод-режима
        this(manager, DEFAULT_PORT);               // sprint9
    }

    public HttpTaskServer(TaskManager manager, int port) { // sprint9: для тестов/кастомного порта
        this.manager = manager;
        this.port = port;
        this.gson = buildGson();
    }

    public void start() throws IOException {
        if (server != null) {
            return; // уже запущен
        }
        server = HttpServer.create(new InetSocketAddress(port), 0); // sprint9
        // регистрируем контексты при запуске (а не в конструкторе)
        server.createContext("/tasks", new TasksHandler(this.manager, this.gson));
        server.createContext("/subtasks", new SubtasksHandler(this.manager, this.gson));
        server.createContext("/epics", new EpicsHandler(this.manager, this.gson));
        server.createContext("/history", new HistoryHandler(this.manager, this.gson));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager, this.gson));

        server.start();
        System.out.println("HTTP Task Server started on port " + getPort()); // sprint9
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null; // sprint9
            System.out.println("HTTP Task Server stopped");
        }
    }

    // sprint9: фактический порт (если был 0, ОС подставила свободный)
    public int getPort() {
        return server == null ? port : server.getAddress().getPort();
    }

    public static Gson getGson() {
        return buildGson();
    }

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>) (src, t, ctx) ->
                                new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonDeserializer<LocalDateTime>) (json, t, ctx) ->
                                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .registerTypeAdapter(Duration.class,
                        (com.google.gson.JsonSerializer<Duration>) (src, t, ctx) ->
                                new com.google.gson.JsonPrimitive(src.toMillis()))
                .registerTypeAdapter(Duration.class,
                        (com.google.gson.JsonDeserializer<Duration>) (json, t, ctx) ->
                                Duration.ofMillis(json.getAsLong()))
                .create();
    }

    /** Запуск сервера из консоли. */
    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        System.out.println("HTTP Task Server started");
        System.out.println("manager = " + manager.getClass().getName());
        System.out.println("working dir = " + new java.io.File(".").getAbsolutePath());
        new HttpTaskServer(manager, DEFAULT_PORT).start(); // sprint9
    }
}
