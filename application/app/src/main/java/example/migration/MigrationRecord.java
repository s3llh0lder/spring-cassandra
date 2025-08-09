package example.migration;

import java.time.Instant;

public class MigrationRecord {
    private String version;
    private String description;
    private Instant appliedAt;
    private String appliedBy;
    private boolean success;
    private String errorMessage;
    private long executionTimeMs;

    public MigrationRecord(String version, String description) {
        this.version = version;
        this.description = description;
        this.appliedAt = Instant.now();
        this.appliedBy = System.getProperty("user.name", "unknown");
        this.success = false;
        this.errorMessage = null;
        this.executionTimeMs = 0;
    }

    // Getters and setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public String getAppliedBy() { return appliedBy; }
    public void setAppliedBy(String appliedBy) { this.appliedBy = appliedBy; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}