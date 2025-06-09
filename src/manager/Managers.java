package manager;
//Упрощает инициализацию в точке входа (Main) и тестах.
//Используется для создания реализаций {TaskManager} и {HistoryManager}.
public class Managers {
    //Возвращает реализацию менеджера задач, работающую в памяти.
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
//Возвращает реализацию менеджера истории просмотров, работающую в памяти.
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}