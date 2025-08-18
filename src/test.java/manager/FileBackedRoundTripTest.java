package manager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileBackedRoundTripTest {

  private File tmp;

  @BeforeEach
  void init() throws Exception {
    tmp = File.createTempFile("tasks", ".csv");
    tmp.deleteOnExit();
  }

  @AfterEach
  void cleanup() {
    if (tmp != null && !tmp.delete()) {
      tmp.deleteOnExit();
    }
  }

  @Test
  void saveAndLoad_doesNotThrow() {
    assertDoesNotThrow(
        () -> {
          FileBackedTaskManager m1 = new FileBackedTaskManager(tmp);
          Task t = new Task("t", "d");
          t.setDuration(Duration.ofMinutes(30));
          t.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
          m1.addNewTask(t); // триггерит save()

          FileBackedTaskManager m2 = FileBackedTaskManager.loadFromFile(tmp);
          assertEquals(1, m2.getTasks().size());
          assertEquals("t", m2.getTasks().get(0).getTitle());
        });
  }

  @Test
  void malformedCsv_throwsManagerSaveException() throws Exception {
    // некорректная строка (ни 6, ни 8 колонок)
    String bad =
        "id,type,name,status,description,durationMinutes,startTime,epic\n"
            + "1,TASK,t,NEW,d,10,2025-01-01 10:00,,"; // лишняя запятая => 9 колонок
    Files.writeString(tmp.toPath(), bad, StandardCharsets.UTF_8);

    assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmp));
  }
}
