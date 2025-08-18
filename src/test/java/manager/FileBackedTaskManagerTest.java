package manager;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

  private File tmp;

  @BeforeEach
  void initFile() throws Exception {
    // можно оставить — не мешает
    tmp = File.createTempFile("tasks", ".csv");
    tmp.deleteOnExit();
  }

  @AfterEach
  void cleanup() {
    if (tmp != null) {
      if (!tmp.delete()) {
        // на Windows файл может быть занят — удалим при выходе
        tmp.deleteOnExit();
      }
    }
  }

  @Override
  protected FileBackedTaskManager createManager() {
    if (tmp == null) { // <-- гарантия при вызове из super.setUp()
      try {
        tmp = File.createTempFile("tasks", ".csv");
        tmp.deleteOnExit();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new FileBackedTaskManager(tmp);
  }
}
