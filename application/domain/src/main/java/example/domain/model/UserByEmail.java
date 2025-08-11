package example.domain.model;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Table("users_by_email")
public class UserByEmail {
    @PrimaryKey
    private String email;

    @Column("user_id")
    private UUID userId;
    private String name;

//    @Column("created_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Date createdAt;

//    @Column("updated_at")
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Date updatedAt;

    // Constructors
    public UserByEmail() {}

    public UserByEmail(String email, UUID userId, String name) {
        this.email = email;
        this.userId = userId;
        this.name = name;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Factory method to create from User
    public static UserByEmail fromUser(User user) {
        return new UserByEmail(user.getEmail(), user.getId(), user.getName());
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}