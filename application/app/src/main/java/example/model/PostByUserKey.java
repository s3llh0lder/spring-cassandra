package example.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@PrimaryKeyClass
public class PostByUserKey implements Serializable {
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date createdAt;

    @PrimaryKeyColumn(name = "post_id", type = PrimaryKeyType.CLUSTERED)
    private UUID postId;

    // Constructors
    public PostByUserKey() {}

    public PostByUserKey(UUID userId, Date createdAt, UUID postId) {
        this.userId = userId;
        this.createdAt = createdAt;
        this.postId = postId;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PostByUserKey that = (PostByUserKey) obj;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, createdAt, postId);
    }
}