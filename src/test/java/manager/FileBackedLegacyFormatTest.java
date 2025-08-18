package manager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

class FileBackedLegacyFormatTest {

  @Test
  void legacySprint7Format_isAccepted() throws Exception {
    File tmp = File.createTempFile("tasks", ".csv");
    tmp.deleteOnExit();
    String legacy = "id,type,name,status,description,epic\n" + "1,TASK,t,NEW,d,\n";
    Files.writeString(tmp.toPath(), legacy, StandardCharsets.UTF_8);

    FileBackedTaskManager m = FileBackedTaskManager.loadFromFile(tmp);
    assertEquals(1, m.getTasks().size());
    assertEquals("t", m.getTasks().get(0).getTitle());
  }
}
