package example.domain.model;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.*;


@Table("posts_by_id")
public class PostById {
    @PrimaryKey
    @Column("post_id")
    private UUID postId;

    @Column("user_id")
    private UUID userId;
    private String title;
    private String content;
    private String status;
    private Set<String> tags;

    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private OffsetDateTime createdAt;

    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private OffsetDateTime updatedAt;

    // Constructors
    public PostById() {}

    public PostById(UUID postId, UUID userId, String title, String content) {
        Date now = new Date();
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = "DRAFT";
        this.tags = new HashSet<>();
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Factory method to create from PostByUser
    public static PostById fromPostByUser(PostByUser postByUser) {
        PostById postById = new PostById();
        postById.setPostId(postByUser.getPostId());
        postById.setUserId(postByUser.getUserId());
        postById.setTitle(postByUser.getTitle());
        postById.setContent(postByUser.getContent());
        postById.setStatus(postByUser.getStatus());
        postById.setTags(new HashSet<>(postByUser.getTags() != null ? postByUser.getTags() : Collections.emptySet()));
        postById.setCreatedAt(postByUser.getCreatedAt());
        postById.setUpdatedAt(postByUser.getUpdatedAt());
        return postById;
    }

    // Getters and Setters
    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = OffsetDateTime.now();
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = OffsetDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}