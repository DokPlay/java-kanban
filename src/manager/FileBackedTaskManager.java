package manager;

import java.util.List;
import java.util.ArrayList;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Менеджер с автосохранением в CSV-файл. */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    /*───────────── фабрика ─────────────*/
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager m = new FileBackedTaskManager(file);
        m.restore();
        return m;
    }

    /*───────────── сохранение ─────────────*/
    private void save() {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(),
                StandardCharsets.UTF_8)) {
            w.write("id,type,name,status,description,epic");
            w.newLine();
            for (Task t : getTasks())       { w.write(toCsv(t)); w.newLine(); }
            for (Epic e : getEpics())       { w.write(toCsv(e)); w.newLine(); }
            for (Subtask s : getSubtasks()) { w.write(toCsv(s)); w.newLine(); }
        } catch (IOException ex) {
            throw new ManagerSaveException("Не удалось сохранить файл", ex);
        }
    }

    private void restore() {
        if (!file.exists()) return;

        List<Epic>    epics     = new ArrayList<>();
        List<Task>    tasks     = new ArrayList<>();
        List<Subtask> subtasks  = new ArrayList<>();

        try (BufferedReader r = Files.newBufferedReader(file.toPath(),
                StandardCharsets.UTF_8)) {
            r.readLine();              // пропускаем заголовок
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                Task t = fromCsv(line);
                switch (t.getType()) {
                    case EPIC    -> epics.add((Epic) t);
                    case TASK    -> tasks.add(t);
                    case SUBTASK -> subtasks.add((Subtask) t);
                }
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Не удалось прочитать файл", ex);
        }

        /* порядок важен! */
        for (Epic    e : epics)    super.addNewEpic(e);
        for (Task    t : tasks)    super.addNewTask(t);
        for (Subtask s : subtasks) super.addNewSubtask(s);
    }


    /*───────────── CSV ─────────────*/
    private static String toCsv(Task t) {
        String epicId = "";
        String type   = t.getType().name();   // ← у Subtask вернёт SUBTASK

        if (t instanceof Subtask s) {
            epicId = String.valueOf(s.getEpicId());
        }
        return String.join(",",
                String.valueOf(t.getId()),
                type,
                t.getTitle(),
                t.getStatus().name(),
                t.getDescription(),
                epicId
        );
    }

    private static Task fromCsv(String csv) {
        String[] p = csv.split(",", -1);
        int      id     = Integer.parseInt(p[0]);
        TaskType type   = TaskType.valueOf(p[1]);
        String   name   = p[2];
        Status   status = Status.valueOf(p[3]);
        String   descr  = p[4];

        return switch (type) {
            case TASK -> {
                Task t = new Task(name, descr, status);
                t.setId(id);
                yield t;
            }
            case EPIC -> {
                Epic e = new Epic(name, descr);
                e.setId(id);
                e.setStatus(status);
                yield e;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(p[5]);
                Subtask s  = new Subtask(name, descr, epicId);
                s.setId(id);
                s.setStatus(status);
                yield s;
            }
        };
    }

    /*───────────── переопределения ─────────────*/
    @Override
    public int addNewTask(Task t) {                     // возвращаем id, как в родителе
        int id = super.addNewTask(t);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic e) {
        int id = super.addNewEpic(e);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(Subtask s) {
        int id = super.addNewSubtask(s);
        save();
        return id;
    }

    @Override public void updateTask(Task t)        { super.updateTask(t);       save(); }
    @Override public void updateEpic(Epic e)        { super.updateEpic(e);       save(); }
    @Override public void updateSubtask(Subtask s)  { super.updateSubtask(s);    save(); }

    @Override public void removeTask(int id)        { super.removeTask(id);      save(); }
    @Override public void removeEpic(int id)        { super.removeEpic(id);      save(); }
    @Override public void removeSubtask(int id)     { super.removeSubtask(id);   save(); }

    //@Override public void clear()                   { super.clear();             save(); }  думаю пока не нужен

    /*───────────── demo ─────────────*/
    public static void main(String[] args) {
        FileBackedTaskManager m = new FileBackedTaskManager(new File("tasks.csv"));

        Epic epic = new Epic("Спринт-7", "Файл-менеджер");
        m.addNewEpic(epic);
        m.addNewSubtask(new Subtask("save()", "реализовать", epic.getId()));
        m.addNewTask(new Task("Читать ТЗ", "вникнуть", Status.IN_PROGRESS));

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(new File("tasks.csv"));
        System.out.println("♻ восстановлено задач: " + restored.getTasks().size());
    }
}
