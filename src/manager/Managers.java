package manager;

import java.io.File;

/**
 * Фабрики менеджеров.
 */
public final class Managers {

    private Managers() {}

    /**
     * sprint9: по умолчанию используем файловый менеджер, чтобы данные
     * сохранялись в tasks.csv в рабочей директории запуска.
     */
    public static TaskManager getDefault() { // sprint9
        return new FileBackedTaskManager(new File("tasks.csv")); // sprint9
    }

    /**
     * sprint9: явная фабрика для InMemory — удобно для тестов.
     */
    public static TaskManager getInMemoryTaskManager() { // sprint9
        return new InMemoryTaskManager();                 // sprint9
    }

    /**
     * sprint9: файловый менеджер с настраиваемым путём (если  хранить в data/tasks.csv и т.п.).
     */
    public static TaskManager getFileBackedTaskManager(File file) { // sprint9
        return new FileBackedTaskManager(file);                      // sprint9
    }

    // Оставляем как было: менеджер истории по умолчанию — in-memory.
    @SuppressWarnings("unused")
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
