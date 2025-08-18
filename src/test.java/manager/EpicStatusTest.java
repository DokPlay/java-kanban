package manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

public class EpicStatusTest {

  // Проверяем правила статуса эпика на InMemory
  @Test
  void epicStatusRules_inMemory() {
    assertEpicStatusRules(new InMemoryTaskManager());
  }

  // И то же самое на FileBacked
  @Test
  void epicStatusRules_fileBacked() throws Exception {
    File tmp = File.createTempFile("tasks", ".csv");
    tmp.deleteOnExit();
    assertEpicStatusRules(new FileBackedTaskManager(tmp));
  }

  // Общая проверка правил из ТЗ:
  // a) без подзадач → NEW
  // b) все подзадачи NEW → NEW
  // c) все подзадачи DONE → DONE
  // d) NEW + DONE → IN_PROGRESS
  // e) есть хотя бы одна IN_PROGRESS → IN_PROGRESS
  private void assertEpicStatusRules(TaskManager manager) {
    int epicId = manager.addNewEpic(new Epic("E", "desc"));

    // a) без подзадач
    assertEquals(Status.NEW, manager.getEpic(epicId).getStatus());

    int s1 = manager.addNewSubtask(new Subtask("s1", "", epicId));
    int s2 = manager.addNewSubtask(new Subtask("s2", "", epicId));

    // b) все NEW
    assertEquals(Status.NEW, manager.getEpic(epicId).getStatus());

    // c) все DONE
    var u1 = manager.getSubtask(s1);
    u1.setStatus(Status.DONE);
    manager.updateSubtask(u1);

    var u2 = manager.getSubtask(s2);
    u2.setStatus(Status.DONE);
    manager.updateSubtask(u2);

    assertEquals(Status.DONE, manager.getEpic(epicId).getStatus());

    // d) NEW + DONE → IN_PROGRESS
    u1.setStatus(Status.NEW);
    manager.updateSubtask(u1);
    assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());

    // e) есть хотя бы одна IN_PROGRESS → IN_PROGRESS
    u2.setStatus(Status.IN_PROGRESS);
    manager.updateSubtask(u2);
    assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
  }
}
