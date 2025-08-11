package example.migration;

import java.time.Instant;
import java.util.List;

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

    public static interface MigrationPort {

        /**
         * Get the status of all migrations
         * @return List of migration information
         */
        List<MigrationInfo> getMigrationStatus();

        /**
         * Run pending migrations
         * @throws Exception if migration fails
         */
        void runMigrations() throws Exception;

        /**
         * Migration information data structure
         */
        public static class MigrationInfo {
            private final String name;
            private final String status;
            private final java.time.LocalDateTime executedAt;
            private final String errorMessage;

            public MigrationInfo(String name, String status, java.time.LocalDateTime executedAt, String errorMessage) {
                this.name = name;
                this.status = status;
                this.executedAt = executedAt;
                this.errorMessage = errorMessage;
            }

            public String getName() {
                return name;
            }

            public String getStatus() {
                return status;
            }

            public java.time.LocalDateTime getExecutedAt() {
                return executedAt;
            }

            public String getErrorMessage() {
                return errorMessage;
            }
        }
    }
}