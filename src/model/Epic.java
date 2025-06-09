package model;

import java.util.ArrayList;
import java.util.List;
//Класс Epic представляет эпик — задачу, содержащую список подзадач.
// * Наследуется от Task и содержит список идентификаторов всех подзадач.
public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }
//Защищает subtaskIds от внешнего изменения — инкапсуляция.
//Возвращает копию списка идентификаторов подзадач.
  //   * Это нужно для соблюдения принципа инкапсуляции —
    //        * чтобы внешний код не мог напрямую изменить внутренний список.
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds); // ✅ ВОТ ТАК инкапсуляция соблюдена
    }

//Добавляет идентификатор подзадачи к эпику.
//* Проверяет, что эпик не добавляет сам себя как подзадачу.
    public void addSubtaskId(int id) {
        if (id == this.id) {
            throw new IllegalArgumentException("Эпик не может быть собственным сабтаском");
        }
        subtaskIds.add(id);
    }
}