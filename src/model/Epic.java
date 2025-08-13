package model;

import java.time.Duration;            // NEW (sprint-8)
import java.time.LocalDateTime;       // NEW (sprint-8)
import java.util.ArrayList;
import java.util.List;

/**
 * Эпик объединяет подзадачи.
 * CHANGED (sprint-8):
 * - duration и startTime/ endTime — расчётные поля (как status);
 * - добавлены сеттеры-пакетные для пересчёта менеджером.
 */
@SuppressWarnings("DuplicatedCode")
public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();

    // NEW (sprint-8) — расчётные поля
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    /* ---------- подзадачи ---------- */

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    /* ---------- переопределения расчётных полей ---------- */

    // стало (public):
    public void setCalculatedDuration(Duration duration) {
        this.duration = duration;
    }

    public void setCalculatedStart(LocalDateTime start) {
        this.startTime = start;
    }

    public void setCalculatedEnd(LocalDateTime end) {
        this.endTime = end;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toCsvRow() {
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        String st = startTime == null ? "" : startTime.format(CSV_TIME_FMT);
        return String.join(
                ",",
                String.valueOf(id),
                getType().name(),
                escape(title),
                status.name(),
                escape(description),
                dur,
                st,
                "" // epic
        );
    }

    private static String escape(String s) {
        return s == null ? "" : s;
    }
}
