package example.migration;

public class V003_CreatePostTables extends CassandraMigration {

    public V003_CreatePostTables() {
        super("V003", "Create post tables");
    }

    @Override
    public void migrate(MigrationContext context) throws Exception {
        // Create posts_by_id table (main post storage)
        context.execute("""
            CREATE TABLE IF NOT EXISTS posts_by_id (
                post_id UUID PRIMARY KEY,
                user_id UUID,
                title TEXT,
                content TEXT,
                status TEXT,
                tags SET<TEXT>,
                created_at TIMESTAMP,
                updated_at TIMESTAMP
            )
            """);

        // Create posts_by_user table (for efficient user queries)
        context.execute("""
            CREATE TABLE IF NOT EXISTS posts_by_user (
                user_id UUID,
                created_at TIMESTAMP,
                post_id UUID,
                title TEXT,
                content TEXT,
                status TEXT,
                tags SET<TEXT>,
                updated_at TIMESTAMP,
                PRIMARY KEY (user_id, created_at, post_id)
            ) WITH CLUSTERING ORDER BY (created_at DESC, post_id ASC)
            """);

        // Create posts_by_user_status table (for queries by user and status)
        // FIXED: Corrected the clustering order to match the primary key definition
        context.execute("""
            CREATE TABLE IF NOT EXISTS posts_by_user_status (
                user_id UUID,
                status TEXT,
                created_at TIMESTAMP,
                post_id UUID,
                title TEXT,
                content TEXT,
                tags SET<TEXT>,
                updated_at TIMESTAMP,
                PRIMARY KEY (user_id, status, created_at, post_id)
            ) WITH CLUSTERING ORDER BY (status ASC, created_at DESC, post_id ASC)
            """);
    }
}