package example.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.net.InetSocketAddress;

@TestConfiguration
public class CassandraTestConfig {

    @Bean
    @Primary
    public CassandraTemplate cassandraTemplate() {
        return new CassandraTemplate(cassandraSession());
    }

    @Bean
    @Primary
    public CqlSession cassandraSession() {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("localhost", 9042))
                .withLocalDatacenter("datacenter1")
                .withKeyspace("test_keyspace")
                .build();
    }
}