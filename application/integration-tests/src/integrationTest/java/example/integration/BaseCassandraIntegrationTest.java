package example.integration;

import example.config.SharedCassandraContainer;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public abstract class BaseCassandraIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(BaseCassandraIntegrationTest.class);

    protected static final CassandraContainer cassandra = SharedCassandraContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.debug("=== CONFIGURING SPRING PROPERTIES ===");

        String host = cassandra.getHost();
        Integer port = cassandra.getMappedPort(9042);
        String datacenter = cassandra.getLocalDatacenter();

        log.debug("Container Host: {}", host);
        log.debug("Container Port: {}", port);
        log.debug("Container Datacenter: {}", datacenter);
        log.debug("Container Running: {}", cassandra.isRunning());

        // CRITICAL: Use the correct property names for Spring Boot
        registry.add("spring.cassandra.contact-points", () -> {
            log.debug("Setting contact-points to: {}:{}", host, port);
            return host + ":" + port;
        });

        registry.add("spring.cassandra.local-datacenter", () -> {
            log.debug("Setting datacenter to: {}", datacenter);
            return datacenter;
        });

        registry.add("spring.cassandra.keyspace-name", () -> "spring_cassandra");
        registry.add("spring.cassandra.schema-action", () -> "recreate");

        // Alternative property names (try both)
        registry.add("spring.data.cassandra.contact-points", () -> host + ":" + port);
        registry.add("spring.data.cassandra.local-datacenter", () -> datacenter);
        registry.add("spring.data.cassandra.keyspace-name", () -> "spring_cassandra");
        registry.add("spring.data.cassandra.schema-action", () -> "recreate");

        // Connection timeouts
        registry.add("spring.cassandra.connection.init-query-timeout", () -> "30s");
        registry.add("spring.cassandra.connection.connect-timeout", () -> "30s");
        registry.add("spring.cassandra.request.timeout", () -> "30s");

        registry.add("spring.data.cassandra.connection.init-query-timeout", () -> "30s");
        registry.add("spring.data.cassandra.connection.connect-timeout", () -> "30s");
        registry.add("spring.data.cassandra.request.timeout", () -> "30s");

        log.debug("=== PROPERTIES CONFIGURED ===");
    }

    @BeforeAll
    static void setUpCassandra() {
        log.debug("=== SETUP VERIFICATION ===");
        assertTrue(cassandra.isRunning(), "Cassandra container should be running");

        log.debug("Final verification:");
        log.debug("Host: {}", cassandra.getHost());
        log.debug("Port: {}", cassandra.getMappedPort(9042));
        log.debug("Datacenter: {}", cassandra.getLocalDatacenter());
        log.debug("Container ID: {}", cassandra.getContainerId());

        // Test container connectivity
        try {
            Thread.sleep(2000); // Give container extra time
            log.debug("Container logs (last 10 lines):");
            log.debug(cassandra.getLogs().lines()
                    .skip(Math.max(0, cassandra.getLogs().lines().count() - 10))
                    .reduce("", String::concat));
        } catch (Exception e) {
            log.error("Error getting logs: {}", e.getMessage());
        }
    }
}