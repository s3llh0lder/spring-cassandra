package example.integration;

import example.model.PostById;
import example.model.PostByUser;
import example.model.PostByUserStatus;
import example.repository.PostByIdRepository;
import example.repository.PostByUserRepository;
import example.repository.PostByUserStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class PostRepositoryIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private PostByUserRepository postByUserRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;
    
    @Autowired
    private PostByUserStatusRepository postByUserStatusRepository;

    private UUID userId;
    private PostByUser testPostByUser;
    private PostById testPostById;

    @BeforeEach
    void set() {
        postByUserRepository.deleteAll();
        postByIdRepository.deleteAll();
        postByUserStatusRepository.deleteAll();

        userId = UUID.randomUUID();
        testPostByUser = new PostByUser(userId, "Test Post", "Test content");
        testPostById = PostById.fromPostByUser(testPostByUser);
    }

    @Test
    void saveAndFindPostByUser_Success() {
        // When
        PostByUser savedPost = postByUserRepository.save(testPostByUser);
        List<PostByUser> foundPosts = postByUserRepository.findByUserId(userId);

        // Then
        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.get(0).getTitle()).isEqualTo("Test Post");
        assertThat(foundPosts.get(0).getContent()).isEqualTo("Test content");
        assertThat(foundPosts.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void saveAndFindPostById_Success() {
        // When
        PostById savedPost = postByIdRepository.save(testPostById);
        Optional<PostById> foundPost = postByIdRepository.findById(savedPost.getPostId());

        // Then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Post");
        assertThat(foundPost.get().getContent()).isEqualTo("Test content");
        assertThat(foundPost.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void findPostsByUserWithLimit_Success() {
        // Given
        PostByUser post1 = new PostByUser(userId, "Post 1", "Content 1");
        PostByUser post2 = new PostByUser(userId, "Post 2", "Content 2");
        PostByUser post3 = new PostByUser(userId, "Post 3", "Content 3");

        postByUserRepository.save(post1);
        postByUserRepository.save(post2);
        postByUserRepository.save(post3);

        // When
        List<PostByUser> foundPosts = postByUserRepository.findByUserIdWithLimit(userId, 2);

        // Then
        assertThat(foundPosts).hasSize(2);
        // Posts should be ordered by creation time descending
        assertThat(foundPosts.get(0).getCreatedAt()).isAfterOrEqualTo(foundPosts.get(1).getCreatedAt());
    }

    @Test
    void findPostsByUserAndStatus_Success() {
        // Given
        PostByUser draftPost = new PostByUser(userId, "Draft Post", "Draft content");
        draftPost.setStatus("DRAFT");

        PostByUser publishedPost = new PostByUser(userId, "Published Post", "Published content");
        publishedPost.setStatus("PUBLISHED");

        // Save to main table
        postByUserRepository.save(draftPost);
        postByUserRepository.save(publishedPost);

        // Save to status table
        PostByUserStatus draftPostStatus = PostByUserStatus.fromPostByUser(draftPost);
        PostByUserStatus publishedPostStatus = PostByUserStatus.fromPostByUser(publishedPost);
        postByUserStatusRepository.save(draftPostStatus);
        postByUserStatusRepository.save(publishedPostStatus);

        // When
        List<PostByUserStatus> draftPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "DRAFT");
        List<PostByUserStatus> publishedPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "PUBLISHED");

        // Then
        assertThat(draftPosts).hasSize(1);
        assertThat(draftPosts.get(0).getTitle()).isEqualTo("Draft Post");

        assertThat(publishedPosts).hasSize(1);
        assertThat(publishedPosts.get(0).getTitle()).isEqualTo("Published Post");
    }

    @Test
    void findPostsByUserStatusWithLimit_Success() {
        // Given
        PostByUser post1 = new PostByUser(userId, "Draft Post 1", "Content 1");
        post1.setStatus("DRAFT");
        PostByUser post2 = new PostByUser(userId, "Draft Post 2", "Content 2");
        post2.setStatus("DRAFT");
        PostByUser post3 = new PostByUser(userId, "Draft Post 3", "Content 3");
        post3.setStatus("DRAFT");

        // Save to status table
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(post1));
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(post2));
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(post3));

        // When
        List<PostByUserStatus> foundPosts = postByUserStatusRepository.findByUserIdAndStatusWithLimit(userId, "DRAFT", 2);

        // Then
        assertThat(foundPosts).hasSize(2);
        // Posts should be ordered by creation time descending within the status
        assertThat(foundPosts.get(0).getCreatedAt()).isAfterOrEqualTo(foundPosts.get(1).getCreatedAt());
    }

    @Test
    void findPostsByUserAcrossAllStatuses_Success() {
        // Given
        PostByUser draftPost = new PostByUser(userId, "Draft Post", "Draft content");
        draftPost.setStatus("DRAFT");
        PostByUser publishedPost = new PostByUser(userId, "Published Post", "Published content");
        publishedPost.setStatus("PUBLISHED");

        // Save to status table
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(draftPost));
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(publishedPost));

        // When - Query each status separately (this is the correct Cassandra pattern)
        List<PostByUserStatus> draftPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "DRAFT");
        List<PostByUserStatus> publishedPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "PUBLISHED");

        // Combine results
        List<PostByUserStatus> allUserPosts = new ArrayList<>();
        allUserPosts.addAll(draftPosts);
        allUserPosts.addAll(publishedPosts);

        // Then
        assertThat(allUserPosts).hasSize(2);
        // Verify we have posts from both statuses
        boolean hasDraft = allUserPosts.stream().anyMatch(p -> "DRAFT".equals(p.getStatus()));
        boolean hasPublished = allUserPosts.stream().anyMatch(p -> "PUBLISHED".equals(p.getStatus()));
        assertThat(hasDraft).isTrue();
        assertThat(hasPublished).isTrue();
    }

    @Test
    void updatePost_Success() {
        // Given
        PostByUser savedPost = postByUserRepository.save(testPostByUser);

        // When
        savedPost.setTitle("Updated Title");
        savedPost.setContent("Updated Content");
        savedPost.setStatus("PUBLISHED");
        PostByUser updatedPost = postByUserRepository.save(savedPost);

        // Then
        List<PostByUser> foundPosts = postByUserRepository.findByUserId(userId);
        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.get(0).getTitle()).isEqualTo("Updated Title");
        assertThat(foundPosts.get(0).getContent()).isEqualTo("Updated Content");
        assertThat(foundPosts.get(0).getStatus()).isEqualTo("PUBLISHED");
        assertThat(foundPosts.get(0).getUpdatedAt()).isAfter(foundPosts.get(0).getCreatedAt());
    }

    @Test
    void deletePost_Success() {
        // Given
        PostByUser savedPost = postByUserRepository.save(testPostByUser);

        // When
        postByUserRepository.deleteById(savedPost.getKey());

        // Then
        List<PostByUser> foundPosts = postByUserRepository.findByUserId(userId);
        assertThat(foundPosts).isEmpty();
    }

    @Test
    void deletePostByUserStatus_Success() {
        // Given
        PostByUser post = new PostByUser(userId, "Test Post", "Test content");
        post.setStatus("DRAFT");
        PostByUserStatus statusPost = PostByUserStatus.fromPostByUser(post);
        PostByUserStatus savedStatusPost = postByUserStatusRepository.save(statusPost);

        // When
        postByUserStatusRepository.deleteById(savedStatusPost.getKey());

        // Then
        List<PostByUserStatus> foundPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "DRAFT");
        assertThat(foundPosts).isEmpty();
    }

    @Test
    void findPostsByUserAndMultipleStatuses_Success() {
        // Given
        PostByUser draftPost = new PostByUser(userId, "Draft Post", "Draft content");
        draftPost.setStatus("DRAFT");
        PostByUser publishedPost = new PostByUser(userId, "Published Post", "Published content");
        publishedPost.setStatus("PUBLISHED");
        PostByUser archivedPost = new PostByUser(userId, "Archived Post", "Archived content");
        archivedPost.setStatus("ARCHIVED");

        // Save to status table
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(draftPost));
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(publishedPost));
        postByUserStatusRepository.save(PostByUserStatus.fromPostByUser(archivedPost));

        // When - Query specific statuses (efficient queries)
        List<PostByUserStatus> draftPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "DRAFT");
        List<PostByUserStatus> publishedPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "PUBLISHED");
        List<PostByUserStatus> archivedPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, "ARCHIVED");

        // Then
        assertThat(draftPosts).hasSize(1);
        assertThat(draftPosts.get(0).getTitle()).isEqualTo("Draft Post");
        
        assertThat(publishedPosts).hasSize(1);
        assertThat(publishedPosts.get(0).getTitle()).isEqualTo("Published Post");
        
        assertThat(archivedPosts).hasSize(1);
        assertThat(archivedPosts.get(0).getTitle()).isEqualTo("Archived Post");
    }
}