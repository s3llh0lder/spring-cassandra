package example.migration;

public class V004_CreateIndexes extends CassandraMigration {

    public V004_CreateIndexes() {
        super("V004", "Create secondary indexes");
    }

    @Override
    public void migrate(MigrationContext context) throws Exception {
        // Create index on posts_by_id.user_id for reverse lookups
        context.execute("""
            CREATE INDEX IF NOT EXISTS posts_by_id_user_id_idx ON posts_by_id (user_id)
            """);

        // Create index on posts_by_id.status for status-based queries
        context.execute("""
            CREATE INDEX IF NOT EXISTS posts_by_id_status_idx ON posts_by_id (status)
            """);

        // Create index on users_by_email.user_id for reverse lookups
        context.execute("""
            CREATE INDEX IF NOT EXISTS users_by_email_user_id_idx ON users_by_email (user_id)
            """);
    }
}