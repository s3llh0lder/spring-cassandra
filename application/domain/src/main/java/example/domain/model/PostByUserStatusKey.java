package example.domain.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@PrimaryKeyClass
public class PostByUserStatusKey implements Serializable {

    // Partition key - only user_id
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    // Clustering keys - status, created_at, post_id (in that order)
    @PrimaryKeyColumn(name = "status", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private String status;

    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private OffsetDateTime createdAt;

    @PrimaryKeyColumn(name = "post_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID postId;

    // Constructors
    public PostByUserStatusKey() {}

    public PostByUserStatusKey(UUID userId, String status, OffsetDateTime createdAt, UUID postId) {
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.postId = postId;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostByUserStatusKey that = (PostByUserStatusKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, status, createdAt, postId);
    }

    @Override
    public String toString() {
        return "PostByUserStatusKey{" +
                "userId=" + userId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", postId=" + postId +
                '}';
    }
}