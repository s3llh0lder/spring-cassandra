package example.domain.ports.input;

import java.util.List;

public interface MigrationPort {
    
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