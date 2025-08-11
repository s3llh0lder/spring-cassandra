package example.integration;

import example.domain.model.PostById;
import example.domain.model.PostByUser;
import example.domain.model.User;
import example.domain.model.UserStats;
import example.domain.repository.PostByIdRepository;
import example.domain.repository.PostByUserRepository;
import example.domain.repository.UserStatsRepository;
import example.domain.ports.input.CreatePostRequest;
import example.domain.ports.input.CreateUserRequest;
import example.domain.ports.input.UpdatePostRequest;
import example.domain.services.PostService;
import example.domain.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class PostServiceIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostByUserRepository postByUserRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    private User testUser;

    @BeforeEach
    void set() {
        postByUserRepository.deleteAll();
        postByIdRepository.deleteAll();
        userStatsRepository.deleteAll();

        // Create a test user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("post.test." + System.currentTimeMillis() + "@example.com");
        testUser = userService.createUser(userRequest);
    }

    @Test
    void createPost_FullWorkflow_Success() {
        // Given
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Integration Test Post");
        request.setContent("This is an integration test post");
        request.setStatus("DRAFT");
        request.setTags(Arrays.asList("example/integration", "test"));

        // When
        PostByUser createdPost = postService.createPost(testUser.getId(), request);

        // Then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo("Integration Test Post");
        assertThat(createdPost.getContent()).isEqualTo("This is an integration test post");
        assertThat(createdPost.getStatus()).isEqualTo("DRAFT");
        assertThat(createdPost.getTags()).containsExactlyInAnyOrder("example/integration", "test");

        // Verify post is saved in posts_by_user table
        List<PostByUser> userPosts = postByUserRepository.findByUserId(testUser.getId());
        assertThat(userPosts).hasSize(1);
        assertThat(userPosts.get(0).getPostId()).isEqualTo(createdPost.getPostId());

        // Verify post is saved in posts_by_id table
        Optional<PostById> postById = postByIdRepository.findById(createdPost.getPostId());
        assertThat(postById).isPresent();
        assertThat(postById.get().getTitle()).isEqualTo("Integration Test Post");

        // Verify user stats are updated
        Optional<UserStats> userStats = userStatsRepository.findById(testUser.getId());
        assertThat(userStats).isPresent();
        assertThat(userStats.get().getTotalPosts()).isEqualTo(1);
        assertThat(userStats.get().getDraftPosts()).isEqualTo(1);
        assertThat(userStats.get().getPublishedPosts()).isEqualTo(0);
    }

    @Test
    void publishPost_FullWorkflow_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Draft Post");
        createRequest.setContent("Content");
        createRequest.setStatus("DRAFT");
        PostByUser draftPost = postService.createPost(testUser.getId(), createRequest);

        // When
        PostByUser publishedPost = postService.publishPost(testUser.getId(), draftPost.getPostId());

        // Then
        assertThat(publishedPost.getStatus()).isEqualTo("PUBLISHED");

        // Verify both tables are updated
        Optional<PostById> postById = postByIdRepository.findById(draftPost.getPostId());
        assertThat(postById).isPresent();
        assertThat(postById.get().getStatus()).isEqualTo("PUBLISHED");

        // Verify user stats are updated
        Optional<UserStats> userStats = userStatsRepository.findById(testUser.getId());
        assertThat(userStats).isPresent();
        assertThat(userStats.get().getTotalPosts()).isEqualTo(1);
        assertThat(userStats.get().getDraftPosts()).isEqualTo(0);
        assertThat(userStats.get().getPublishedPosts()).isEqualTo(1);
    }

    @Test
    void deletePost_FullWorkflow_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Post to Delete");
        createRequest.setContent("Content");
        createRequest.setStatus("PUBLISHED");
        PostByUser createdPost = postService.createPost(testUser.getId(), createRequest);

        // When
        postService.deletePost(testUser.getId(), createdPost.getPostId());

        // Then
        // Verify post is removed from posts_by_user table
        List<PostByUser> userPosts = postByUserRepository.findByUserId(testUser.getId());
        assertThat(userPosts).isEmpty();

        // Verify post is removed from posts_by_id table
        Optional<PostById> postById = postByIdRepository.findById(createdPost.getPostId());
        assertThat(postById).isEmpty();

        // Verify user stats are updated
        Optional<UserStats> userStats = userStatsRepository.findById(testUser.getId());
        assertThat(userStats).isPresent();
        assertThat(userStats.get().getTotalPosts()).isEqualTo(0);
        assertThat(userStats.get().getPublishedPosts()).isEqualTo(0);
    }

    @Test
    void getUserPostsByStatus_Success() {
        // Given
        CreatePostRequest draftRequest = new CreatePostRequest();
        draftRequest.setTitle("Draft");
        draftRequest.setContent("Draft content");
        draftRequest.setStatus("DRAFT");

        CreatePostRequest publishedRequest = new CreatePostRequest();
        publishedRequest.setTitle("Published");
        publishedRequest.setContent("Published content");
        publishedRequest.setStatus("PUBLISHED");

        postService.createPost(testUser.getId(), draftRequest);
        postService.createPost(testUser.getId(), publishedRequest);

        // When
        List<PostByUser> draftPosts = postService.getUserPostsByStatus(testUser.getId(), "DRAFT");
        List<PostByUser> publishedPosts = postService.getUserPostsByStatus(testUser.getId(), "PUBLISHED");

        // Then
        assertThat(draftPosts).hasSize(1);
        assertThat(draftPosts.get(0).getTitle()).isEqualTo("Draft");

        assertThat(publishedPosts).hasSize(1);
        assertThat(publishedPosts.get(0).getTitle()).isEqualTo("Published");
    }

    @Test
    void updatePost_FullWorkflow_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Original Title");
        createRequest.setContent("Original content");
        createRequest.setStatus("DRAFT");
        PostByUser createdPost = postService.createPost(testUser.getId(), createRequest);

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");
        updateRequest.setStatus("PUBLISHED");
        updateRequest.setTags(Arrays.asList("updated", "example/integration"));

        // When
        PostByUser updatedPost = postService.updatePost(testUser.getId(), createdPost.getPostId(), updateRequest);

        // Then
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedPost.getContent()).isEqualTo("Updated content");
        assertThat(updatedPost.getStatus()).isEqualTo("PUBLISHED");
        assertThat(updatedPost.getTags()).containsExactlyInAnyOrder("updated", "example/integration");

        // Verify both tables are updated
        Optional<PostById> postById = postByIdRepository.findById(createdPost.getPostId());
        assertThat(postById).isPresent();
        assertThat(postById.get().getTitle()).isEqualTo("Updated Title");
        assertThat(postById.get().getStatus()).isEqualTo("PUBLISHED");

        // Verify user stats reflect status change
        Optional<UserStats> userStats = userStatsRepository.findById(testUser.getId());
        assertThat(userStats).isPresent();
        assertThat(userStats.get().getTotalPosts()).isEqualTo(1);
        assertThat(userStats.get().getDraftPosts()).isEqualTo(0);
        assertThat(userStats.get().getPublishedPosts()).isEqualTo(1);
    }
}