package example.migration;

import com.datastax.oss.driver.api.core.CqlSession;
import java.util.List;

public class MigrationContext {
    private final CqlSession session;
    private final String keyspace;

    public MigrationContext(CqlSession session, String keyspace) {
        this.session = session;
        this.keyspace = keyspace;
    }

    public CqlSession getSession() {
        return session;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void execute(String cql) {
        session.execute(cql);
    }

    public void execute(String cql, Object... parameters) {
        session.execute(cql, parameters);
    }

    public void executeStatements(List<String> statements) {
        for (String statement : statements) {
            if (statement.trim().startsWith("USE ")) {
                // Skip USE statements - we're already connected to the keyspace
                continue;
            }
            execute(statement);
        }
    }
}