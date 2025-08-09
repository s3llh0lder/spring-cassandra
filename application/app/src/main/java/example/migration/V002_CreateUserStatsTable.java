package example.migration;

public class V002_CreateUserStatsTable extends CassandraMigration {

    public V002_CreateUserStatsTable() {
        super("V002", "Create user_stats table");
    }

    @Override
    public void migrate(MigrationContext context) throws Exception {
        // Create user stats table
        context.execute("""
            CREATE TABLE IF NOT EXISTS user_stats (
                user_id UUID PRIMARY KEY,
                total_posts INT,
                published_posts INT,
                draft_posts INT,
                last_post_date TIMESTAMP,
                updated_at TIMESTAMP
            )
            """);
    }
}