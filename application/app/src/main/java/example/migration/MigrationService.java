package example.migration;

import example.domain.ports.input.MigrationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MigrationService implements MigrationPort {

    @Autowired
    private CassandraMigrationService cassandraMigrationService;

    @Override
    public List<MigrationInfo> getMigrationStatus() {
        return cassandraMigrationService.getMigrationStatus()
                .stream()
                .map(this::convertToPortModel)
                .collect(Collectors.toList());
    }

    @Override
    public void runMigrations() throws Exception {
        cassandraMigrationService.runMigrations();
    }

    private MigrationInfo convertToPortModel(CassandraMigrationService.MigrationInfo serviceInfo) {
        return new MigrationInfo(
                serviceInfo.getVersion() + " - " + serviceInfo.getDescription(),
                serviceInfo.isApplied() ? "completed" : "pending",
                null, // executedAt not available in current implementation
                null  // errorMessage not available in current implementation
        );
    }
}