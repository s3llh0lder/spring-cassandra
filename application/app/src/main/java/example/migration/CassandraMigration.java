package example.migration;

import java.time.LocalDateTime;

public abstract class CassandraMigration {
    private final String version;
    private final String description;
    private final LocalDateTime timestamp;

    public CassandraMigration(String version, String description) {
        this.version = version;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public abstract void migrate(MigrationContext context) throws Exception;

    public void rollback(MigrationContext context) throws Exception {
        throw new UnsupportedOperationException(
                "Rollback not implemented for migration: " + version
        );
    }

    // Getters
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
}