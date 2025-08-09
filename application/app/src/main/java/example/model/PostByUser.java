package example.model;


import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Table("posts_by_user")
public class PostByUser {
    @PrimaryKey
    private PostByUserKey key;

    private String title;
    private String content;
    private String status;
    private Set<String> tags;
    private Date createdAt;
    private Date updatedAt;

    // Constructors
    public PostByUser() {}

    public PostByUser(UUID userId, String title, String content) {
        Date now = new Date();
        this.key = new PostByUserKey(userId, now, UUID.randomUUID());
        this.title = title;
        this.content = content;
        this.status = "DRAFT";
        this.tags = new HashSet<>();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters and Setters
    public PostByUserKey getKey() { return key; }
    public void setKey(PostByUserKey key) { this.key = key; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = new Date();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public UUID getUserId() { return key != null ? key.getUserId() : null; }
    public UUID getPostId() { return key != null ? key.getPostId() : null; }
}