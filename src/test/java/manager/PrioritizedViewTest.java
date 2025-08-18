package manager;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import model.Epic;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrioritizedViewTest {

  private InMemoryTaskManager manager;

  @BeforeEach
  void setUp() {
    manager = new InMemoryTaskManager();
  }

  @Test
  void prioritized_excludesEpicsAndNullStart() {
    int epicId = manager.addNewEpic(new Epic("E", "d"));

    Task noStart = new Task("noStart", "");
    noStart.setDuration(Duration.ofMinutes(15));
    manager.addNewTask(noStart);

    Task withStart = new Task("withStart", "");
    withStart.setDuration(Duration.ofMinutes(10));
    withStart.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
    manager.addNewTask(withStart);

    List<Task> pr = manager.getPrioritizedTasks();
    assertEquals(
        1, pr.size(), "В приоритизации должны быть только задачи с startTime (без эпиков)");
    assertEquals("withStart", pr.get(0).getTitle());
  }
}
