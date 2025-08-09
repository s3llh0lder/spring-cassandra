package example.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table("posts_by_user_status")
public class PostByUserStatus {
    
    @PrimaryKey
    private PostByUserStatusKey key;

    private String title;
    private String content;
    private Set<String> tags;
    @Column("updated_at")
    private Date updatedAt;

    // Constructors
    public PostByUserStatus() {}

    public PostByUserStatus(UUID userId, String status, String title, String content) {
        Date now = new Date();
        this.key = new PostByUserStatusKey(userId, status, now, UUID.randomUUID());
        this.title = title;
        this.content = content;
        this.tags = new HashSet<>();
        this.updatedAt = now;
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
        postByStatus.tags = postByUser.getTags() != null ? new HashSet<>(postByUser.getTags()) : new HashSet<>();
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
        this.updatedAt = new Date();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = new Date();
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
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

    public Date getCreatedAt() {
        return key != null ? key.getCreatedAt() : null;
    }
}