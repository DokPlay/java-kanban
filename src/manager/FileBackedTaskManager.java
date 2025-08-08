package manager;

import model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер с автоматическим сохранением в CSV-файл.
 * Наследуем InMemoryTaskManager и добавляем автосохранение.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    /* ───────────── фабрика ───────────── */

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager m = new FileBackedTaskManager(file);
        m.restore();
        return m;
    }

    /* ───────────── сохранение ───────────── */

    /** Сохраняет все задачи в CSV: id,type,name,status,description,epic */
    private void save() {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write("id,type,name,status,description,epic");
            w.newLine();

            // порядок не критичен, но читается приятнее
            for (Task t : getTasks()) {
                w.write(t.toCsvRow());
                w.newLine();
            }
            for (Epic e : getEpics()) {
                w.write(e.toCsvRow());
                w.newLine();
            }
            for (Subtask s : getSubtasks()) {
                w.write(s.toCsvRow());
                w.newLine();
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Не удалось сохранить файл", ex);
        }
    }

    /* ───────────── восстановление ───────────── */

    /**
     * Читает CSV и восстанавливает состояние.
     * ВАЖНО: не перебиваем зафиксированные в файле ID.
     * Поэтому используем прямые put*-методы из базового класса и выставляем nextId.
     */
    private void restore() {
        if (!file.exists()) {
            return;
        }

        List<Epic> epics     = new ArrayList<>();
        List<Task> tasks     = new ArrayList<>();
        List<Subtask> subtasks = new ArrayList<>();

        try (BufferedReader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = r.readLine(); // заголовок
            if (header == null) {
                return;
            }
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Task t = fromCsv(line);
                switch (t.getType()) {
                    case EPIC -> epics.add((Epic) t);
                    case TASK -> tasks.add(t);
                    case SUBTASK -> subtasks.add((Subtask) t);
                }
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Не удалось прочитать файл", ex);
        }

        // Важно: сначала эпики, затем задачи, затем подзадачи
        for (Epic e : epics) {
            super.putEpicPreserveId(e);
        }
        for (Task t : tasks) {
            super.putTaskPreserveId(t);
        }
        for (Subtask s : subtasks) {
            super.putSubtaskPreserveId(s);
        }
        // ---> СДВИГАЕМ nextId TODO:так же для себя делал,убрал второстепенные замечания!
        int maxId = 0;
        for (Task t : tasks)     maxId = Math.max(maxId, t.getId());
        for (Epic e : epics)     maxId = Math.max(maxId, e.getId());
        for (Subtask s : subtasks) maxId = Math.max(maxId, s.getId());

        super.setNextIdAfterRestore(maxId + 1);
    }


    /* ───────────── CSV утилиты ───────────── */

    private static Task fromCsv(String csv) {
        String[] p = csv.split(",", -1);

        int id = Integer.parseInt(p[0]);
        TaskType type = TaskType.valueOf(p[1]);
        String name = p[2];
        Status status = Status.valueOf(p[3]);
        String description = p[4];

        switch (type) {
            case TASK: {
                Task t = new Task(name, description, status);
                t.setId(id);
                return t;
            }
            case EPIC: {
                Epic e = new Epic(name, description);
                e.setId(id);
                e.setStatus(status);
                return e;
            }
            case SUBTASK: {
                int epicId = Integer.parseInt(p[5]);
                Subtask s = new Subtask(name, description, epicId);
                s.setId(id);
                s.setStatus(status);
                return s;
            }
            default:
                throw new IllegalStateException("Неизвестный тип: " + type);
        }
    }

    /* ───────────── переопределения с автоматическим сохранением ───────────── */

    @Override
    public int addNewTask(Task t) {
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

    @Override
    public void updateTask(Task t) {
        super.updateTask(t);
        save();
    }

    @Override
    public void updateEpic(Epic e) {
        super.updateEpic(e);
        save();
    }

    @Override
    public void updateSubtask(Subtask s) {
        super.updateSubtask(s);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    // Если в интерфейсе есть метод clear, раскомментировать. Это я себе на будущее!
    /*TODO(clear): @Override
     public void clear() {
     super.clear();
     save();
     } */

    /* ───────────── demo ───────────── */

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
