package example.model;


import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.UUID;


@Table("users")
public class User {
    @PrimaryKey
    private UUID id;

    private String name;
    private String email;
    @Column("created_at")
    private Date createdAt;
    @Column("updated_at")
    private Date updatedAt;

    // Constructors
    public User() {
        this.id = UUID.randomUUID();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public User(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updatedAt = new Date();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
