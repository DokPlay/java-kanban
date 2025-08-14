package manager;

import model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    @Test
    void emptyHistory_ok() {
        InMemoryHistoryManager h = new InMemoryHistoryManager();
        assertTrue(h.getHistory().isEmpty());
    }

    @Test
    void noDuplicates_limit10() {
        InMemoryHistoryManager h = new InMemoryHistoryManager();
        for (int i = 0; i < 12; i++) {
            Task t = new Task("T" + i, "", Duration.ofMinutes(1), LocalDateTime.now());
            t.setId(i + 1);
            h.add(t);
            h.add(t);
        }
        assertEquals(10, h.getHistory().size());
    }

    @Test
    void remove_edges() {
        InMemoryHistoryManager h = new InMemoryHistoryManager();

        Task a = new Task("A", "", Duration.ofMinutes(1), LocalDateTime.now());
        a.setId(1);

        Task b = new Task("B", "", Duration.ofMinutes(1), LocalDateTime.now());
        b.setId(2);

        Task c = new Task("C", "", Duration.ofMinutes(1), LocalDateTime.now());
        c.setId(3);

        h.add(a);
        h.add(b);
        h.add(c);

        h.remove(1); // начало
        h.remove(2); // середина (после удаления 1 останутся [b, c])
        h.remove(3); // конец

        assertTrue(h.getHistory().isEmpty());
    }
}
