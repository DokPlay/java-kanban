package manager;

public final class Managers {

    private Managers() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // NEW для истории
    @SuppressWarnings("unused")
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
