package example.controller;

import example.migration.CassandraMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    @Autowired
    private CassandraMigrationService migrationService;

    @GetMapping("/status")
    public List<CassandraMigrationService.MigrationInfo> getMigrationStatus() {
        return migrationService.getMigrationStatus();
    }

    @PostMapping("/run")
    public String runMigrations() {
        try {
            migrationService.runMigrations();
            return "Migrations completed successfully";
        } catch (Exception e) {
            return "Migration failed: " + e.getMessage();
        }
    }
}