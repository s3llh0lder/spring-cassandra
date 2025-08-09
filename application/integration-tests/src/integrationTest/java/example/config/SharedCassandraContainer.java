package example.config;

import org.testcontainers.cassandra.CassandraContainer;

import java.time.Duration;

public class SharedCassandraContainer {

    private static final String CASSANDRA_IMAGE = "cassandra:4.0";
    private static CassandraContainer cassandraContainer;

    private SharedCassandraContainer() {
        // Private constructor
    }

    public static synchronized CassandraContainer getInstance() {
        if (cassandraContainer == null) {
            System.out.println("Creating new Cassandra container...");

            cassandraContainer = new CassandraContainer(CASSANDRA_IMAGE)
                    .withExposedPorts(9042)
                    .withStartupTimeout(Duration.ofMinutes(5))
                    .withInitScript("init.cql")
                    .withStartupAttempts(3);

            System.out.println("Starting Cassandra container...");
            cassandraContainer.start();

            System.out.println("Container started successfully!");
            System.out.println("Host: " + cassandraContainer.getHost());
            System.out.println("Port: " + cassandraContainer.getMappedPort(9042));
            System.out.println("Local Datacenter: " + cassandraContainer.getLocalDatacenter());
            System.out.println("Is Running: " + cassandraContainer.isRunning());
        }
        return cassandraContainer;
    }

    public static void stop() {
        if (cassandraContainer != null && cassandraContainer.isRunning()) {
            cassandraContainer.stop();
            cassandraContainer = null;
        }
    }
}