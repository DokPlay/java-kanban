package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import util.CsvUtils;

/**
 * Эпик объединяет подзадачи.
 */
public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    // TODO(review sprint-8): пересчёт status/duration/start/end за один проход по сабтаскам.
    public void recalcFromSubtasks(List<Subtask> subs) {
        if (subs == null || subs.isEmpty()) {
            this.status = Status.NEW;
            this.duration = null;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        long totalMinutes = 0L;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        for (Subtask s : subs) {
            Status st = s.getStatus();
            if (st != Status.NEW) {
                allNew = false;
            }
            if (st != Status.DONE) {
                allDone = false;
            }

            Duration d = s.getDuration();
            if (d != null) {
                totalMinutes += d.toMinutes();
            }

            LocalDateTime stTime = s.getStartTime();
            if (stTime != null && (minStart == null || stTime.isBefore(minStart))) {
                minStart = stTime;
            }
            LocalDateTime enTime = s.getEndTime();
            if (enTime != null && (maxEnd == null || enTime.isAfter(maxEnd))) {
                maxEnd = enTime;
            }
        }

        if (allNew) {
            this.status = Status.NEW;
        } else if (allDone) {
            this.status = Status.DONE;
        } else {
            this.status = Status.IN_PROGRESS;
        }

        this.duration = (totalMinutes == 0) ? null : Duration.ofMinutes(totalMinutes);
        this.startTime = minStart;
        this.endTime = maxEnd;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // TODO(review sprint-8): CSV-escape вынесен в util.CsvUtils (убрано дублирование).
    @Override
    public String toCsvRow() {
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        String st = startTime == null ? "" : startTime.format(CSV_TIME_FMT);
        return String.join(
                ",",
                String.valueOf(id),
                getType().name(),
                CsvUtils.escape(title),
                status.name(),
                CsvUtils.escape(description),
                dur,
                st,
                "" // epic
        );
    }
}
