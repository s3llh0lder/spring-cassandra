package example.config;

import example.model.PostByUser;
import example.model.User;
import example.model.UserStats;

import static org.assertj.core.api.Assertions.assertThat;

public class CassandraAssertions {

    public static void assertUserEquals(User expected, User actual) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
    }

    public static void assertPostEquals(PostByUser expected, PostByUser actual) {
        assertThat(actual.getKey()).isEqualTo(expected.getKey());
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getContent()).isEqualTo(expected.getContent());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getTags()).isEqualTo(expected.getTags());
    }

    public static void assertUserStatsEquals(UserStats expected, UserStats actual) {
        assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
        assertThat(actual.getTotalPosts()).isEqualTo(expected.getTotalPosts());
        assertThat(actual.getPublishedPosts()).isEqualTo(expected.getPublishedPosts());
        assertThat(actual.getDraftPosts()).isEqualTo(expected.getDraftPosts());
    }
}