package example.domain.ports.input;

import example.domain.model.User;
import example.domain.model.UserStats;

public class UserWithStats {
    private User user;
    private UserStats stats;

    public UserWithStats(User user, UserStats stats) {
        this.user = user;
        this.stats = stats;
    }

    // getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public UserStats getStats() { return stats; }
    public void setStats(UserStats stats) { this.stats = stats; }
}