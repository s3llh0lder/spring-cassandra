package example.domain.model;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.*;

@Table("posts_by_user_status")
public class PostByUserStatus {
    
    @PrimaryKey
    private PostByUserStatusKey key;

    private String title;
    private String content;
    private List<String> tags;
//    @Column("updated_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private OffsetDateTime updatedAt;

    // Constructors
    public PostByUserStatus() {}

    public PostByUserStatus(UUID userId, String status, String title, String content) {
        OffsetDateTime now = OffsetDateTime.now();
        this.key = new PostByUserStatusKey(userId, status, now, UUID.randomUUID());
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>();
        this.updatedAt = OffsetDateTime.now();
    }

    // Create from PostByUser
    public static PostByUserStatus fromPostByUser(PostByUser postByUser) {
        PostByUserStatus postByStatus = new PostByUserStatus();
        postByStatus.key = new PostByUserStatusKey(
                postByUser.getUserId(),
                postByUser.getStatus(),
                postByUser.getCreatedAt(),
                postByUser.getPostId()
        );
        postByStatus.title = postByUser.getTitle();
        postByStatus.content = postByUser.getContent();
        postByStatus.tags = postByUser.getTags() != null ? new ArrayList<>(postByUser.getTags()) : new ArrayList<>();
        postByStatus.updatedAt = postByUser.getUpdatedAt();
        return postByStatus;
    }

    // Getters and Setters
    public PostByUserStatusKey getKey() {
        return key;
    }

    public void setKey(PostByUserStatusKey key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = OffsetDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = OffsetDateTime.now();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public UUID getUserId() {
        return key != null ? key.getUserId() : null;
    }

    public UUID getPostId() {
        return key != null ? key.getPostId() : null;
    }

    public String getStatus() {
        return key != null ? key.getStatus() : null;
    }

    public OffsetDateTime getCreatedAt() {
        return key != null ? key.getCreatedAt() : null;
    }
}