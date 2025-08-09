package example.integration;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SpringCassandraApplicationTests extends BaseCassandraIntegrationTest {

	private static final Logger logger = LoggerFactory.getLogger(SpringCassandraApplicationTests.class);

	@Test
	void contextLoads() {
		logger.info("Testing Spring context with Cassandra...");
		assertTrue(cassandra.isRunning(), "Cassandra container should be running");
		logger.info("Cassandra container is running on {}:{}",
				cassandra.getHost(), cassandra.getMappedPort(9042));
	}

}
