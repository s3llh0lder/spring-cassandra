package example.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Table("user_stats")
public class UserStats {
    @PrimaryKey
    @Column("user_id")
    private UUID userId;

    @Column("total_posts")
    private int totalPosts;

    @Column("published_posts")
    private int publishedPosts;

    @Column("draft_posts")
    private int draftPosts;

    @Column("last_post_date")
    private Date lastPostDate;

    @Column("updated_at")
    private Date updatedAt;

    // Constructors
    public UserStats() {
    }

    public UserStats(UUID userId) {
        this.userId = userId;
        this.totalPosts = 0;
        this.publishedPosts = 0;
        this.draftPosts = 0;
        this.updatedAt = new Date();
    }

    // Methods to update stats
    public void incrementPost(String status) {
        this.totalPosts++;
        if ("PUBLISHED".equals(status)) {
            this.publishedPosts++;
        } else if ("DRAFT".equals(status)) {
            this.draftPosts++;
        }
        this.lastPostDate = new Date();
        this.updatedAt = new Date();
    }

    public void decrementPost(String status) {
        this.totalPosts = Math.max(0, this.totalPosts - 1);
        if ("PUBLISHED".equals(status)) {
            this.publishedPosts = Math.max(0, this.publishedPosts - 1);
        } else if ("DRAFT".equals(status)) {
            this.draftPosts = Math.max(0, this.draftPosts - 1);
        }
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getPublishedPosts() {
        return publishedPosts;
    }

    public void setPublishedPosts(int publishedPosts) {
        this.publishedPosts = publishedPosts;
    }

    public int getDraftPosts() {
        return draftPosts;
    }

    public void setDraftPosts(int draftPosts) {
        this.draftPosts = draftPosts;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public void setLastPostDate(Date lastPostDate) {
        this.lastPostDate = lastPostDate;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}