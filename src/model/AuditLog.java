package model;

import java.time.LocalDateTime;

public class AuditLog {
    private final long logId;
    private final String action;
    private final String targetId;
    private final LocalDateTime timestamp;

    public AuditLog(long logId, String action, String targetId, LocalDateTime timestamp) {
        this.logId = logId;
        this.action = action;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    public long getLogId() {
        return logId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetId() {
        return targetId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}