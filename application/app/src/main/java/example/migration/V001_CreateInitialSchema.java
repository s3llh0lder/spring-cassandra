package example.migration;

public class V001_CreateInitialSchema extends CassandraMigration {

    public V001_CreateInitialSchema() {
        super("V001", "Create users and users_by_email tables");
    }

    @Override
    public void migrate(MigrationContext context) throws Exception {
        context.execute("""
            CREATE KEYSPACE IF NOT EXISTS spring_cassandra
            WITH REPLICATION = {
                'class': 'SimpleStrategy',
                'replication_factor': 1
            }
            """);

        // Switch to the keyspace
        context.execute("USE spring_cassandra");

        // Create main users table
        context.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                name TEXT,
                email TEXT,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
            """);

        // Create users_by_email table for efficient email lookups
        context.execute("""
            CREATE TABLE IF NOT EXISTS users_by_email (
                email TEXT PRIMARY KEY,
                user_id UUID,
                name TEXT,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
            """);
    }
}