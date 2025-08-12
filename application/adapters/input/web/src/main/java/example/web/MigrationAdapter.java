package example.web;


import example.domain.ports.input.MigrationPort;
import example.spring_cassandra.api.controller.MigrationsApi;
import example.spring_cassandra.api.model.MigrationInfoDto;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1")
public class MigrationAdapter implements MigrationsApi {

    @Autowired
    private MigrationPort migrationPort;


    @Override
    public ResponseEntity<List<MigrationInfoDto>> getMigrationStatus() {
        try {
            List<MigrationPort.MigrationInfo> migrationInfos = migrationPort.getMigrationStatus();
            List<MigrationInfoDto> dtos = migrationInfos.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> runMigrations() {
        try {
            migrationPort.runMigrations();
            return ResponseEntity.ok("Migrations completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Migration failed: " + e.getMessage());
        }
    }

    private MigrationInfoDto convertToDto(MigrationPort.MigrationInfo migrationInfo) {
        MigrationInfoDto dto = new MigrationInfoDto();
        dto.setName(migrationInfo.getName());
        dto.setStatus(MigrationInfoDto.StatusEnum.fromValue(migrationInfo.getStatus()));
        
        if (migrationInfo.getExecutedAt() != null) {
            dto.setExecutedAt(JsonNullable.of(migrationInfo.getExecutedAt().atOffset(ZoneOffset.UTC)));
        }
        
        if (migrationInfo.getErrorMessage() != null) {
            dto.setErrorMessage(JsonNullable.of(migrationInfo.getErrorMessage()));
        }
        
        return dto;
    }
}