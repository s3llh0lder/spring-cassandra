package example.service;

import example.domain.model.*;
import example.domain.repository.*;
import example.domain.services.PostService;
import example.domain.exceptions.PostNotFoundException;
import example.domain.exceptions.UserNotFoundException;
import example.domain.ports.input.CreatePostRequest;
import example.domain.ports.input.UpdatePostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostByUserRepository postByUserRepository;

    @Mock
    private PostByIdRepository postByIdRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private PostByUserStatusRepository postByUserStatusRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private CreatePostRequest createPostRequest;
    private UpdatePostRequest updatePostRequest;
    private PostByUser testPostByUser;
    private PostById testPostById;
    private PostByUserStatus testPostByUserStatus;
    private UserStats testUserStats;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john.doe@example.com");
        testUser.setId(UUID.randomUUID());

        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post");
        createPostRequest.setContent("This is a test post content");
        createPostRequest.setStatus("DRAFT");
        createPostRequest.setTags(Arrays.asList("test", "demo"));

        updatePostRequest = new UpdatePostRequest();
        updatePostRequest.setTitle("Updated Post");
        updatePostRequest.setContent("Updated content");
        updatePostRequest.setStatus("PUBLISHED");

        testPostByUser = new PostByUser(testUser.getId(), "Test Post", "Test content");
        testPostById = PostById.fromPostByUser(testPostByUser);
        testPostByUserStatus = PostByUserStatus.fromPostByUser(testPostByUser);
        testUserStats = new UserStats(testUser.getId());
    }

    @Test
    void createPost_Success() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postByUserRepository.save(any(PostByUser.class))).thenReturn(testPostByUser);
        when(postByIdRepository.save(any(PostById.class))).thenReturn(testPostById);
        when(postByUserStatusRepository.save(any(PostByUserStatus.class))).thenReturn(testPostByUserStatus);
        when(userStatsRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(testUserStats);

        // When
        PostByUser result = postService.createPost(testUser.getId(), createPostRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Post");
        assertThat(result.getContent()).isEqualTo("Test content");

        verify(userRepository).findById(testUser.getId());
        verify(postByUserRepository).save(any(PostByUser.class));
        verify(postByIdRepository).save(any(PostById.class));
        verify(postByUserStatusRepository).save(any(PostByUserStatus.class));
        verify(userStatsRepository).findById(testUser.getId());
        verify(userStatsRepository).save(any(UserStats.class));
    }

    @Test
    void createPost_UserNotFound_ThrowsException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(nonExistentUserId, createPostRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + nonExistentUserId);

        verify(userRepository).findById(nonExistentUserId);
        verify(postByUserRepository, never()).save(any());
        verify(postByIdRepository, never()).save(any());
        verify(postByUserStatusRepository, never()).save(any());
    }

    @Test
    void updatePost_Success_StatusNotChanged() {
        // Given
        UUID postId = testPostByUser.getPostId();
        List<PostByUser> userPosts = Arrays.asList(testPostByUser);
        
        UpdatePostRequest requestNoStatusChange = new UpdatePostRequest();
        requestNoStatusChange.setTitle("Updated Title");
        requestNoStatusChange.setContent("Updated Content");
        // Keep same status as testPostByUser (DRAFT)
        requestNoStatusChange.setStatus("DRAFT");

        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(userPosts);
        when(postByUserRepository.save(any(PostByUser.class))).thenReturn(testPostByUser);
        when(postByIdRepository.save(any(PostById.class))).thenReturn(testPostById);
        when(postByUserStatusRepository.save(any(PostByUserStatus.class))).thenReturn(testPostByUserStatus);

        // When
        PostByUser result = postService.updatePost(testUser.getId(), postId, requestNoStatusChange);

        // Then
        assertThat(result).isNotNull();
        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository).save(any(PostByUser.class));
        verify(postByIdRepository).save(any(PostById.class));
        verify(postByUserStatusRepository).save(any(PostByUserStatus.class));
        verify(postByUserStatusRepository, never()).deleteById(any(PostByUserStatusKey.class));
    }

    @Test
    void updatePost_Success_StatusChanged() {
        // Given
        UUID postId = testPostByUser.getPostId();
        List<PostByUser> userPosts = Arrays.asList(testPostByUser);

        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(userPosts);
        when(postByUserRepository.save(any(PostByUser.class))).thenReturn(testPostByUser);
        when(postByIdRepository.save(any(PostById.class))).thenReturn(testPostById);
        when(postByUserStatusRepository.save(any(PostByUserStatus.class))).thenReturn(testPostByUserStatus);
        when(userStatsRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(testUserStats);

        // When
        PostByUser result = postService.updatePost(testUser.getId(), postId, updatePostRequest);

        // Then
        assertThat(result).isNotNull();
        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository).save(any(PostByUser.class));
        verify(postByIdRepository).save(any(PostById.class));
        verify(postByUserStatusRepository).deleteById(any(PostByUserStatusKey.class));
        verify(postByUserStatusRepository).save(any(PostByUserStatus.class));
        verify(userStatsRepository, times(2)).findById(testUser.getId());
        verify(userStatsRepository, times(2)).save(any(UserStats.class));
    }

    @Test
    void updatePost_PostNotFound_ThrowsException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(testUser.getId(), nonExistentPostId, updatePostRequest))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("Post not found: " + nonExistentPostId);

        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository, never()).save(any());
    }

    @Test
    void deletePost_Success() {
        // Given
        UUID postId = testPostByUser.getPostId();
        List<PostByUser> userPosts = Arrays.asList(testPostByUser);

        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(userPosts);
        when(userStatsRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(testUserStats);

        // When
        postService.deletePost(testUser.getId(), postId);

        // Then
        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository).deleteById(testPostByUser.getKey());
        verify(postByIdRepository).deleteById(postId);
        verify(postByUserStatusRepository).deleteById(any(PostByUserStatusKey.class));
        verify(userStatsRepository).findById(testUser.getId());
        verify(userStatsRepository).save(any(UserStats.class));
    }

    @Test
    void deletePost_PostNotFound_ThrowsException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(testUser.getId(), nonExistentPostId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("Post not found: " + nonExistentPostId);

        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository, never()).deleteById(any());
        verify(postByIdRepository, never()).deleteById(any());
        verify(postByUserStatusRepository, never()).deleteById(any());
    }

    @Test
    void getUserPosts_WithLimit_Success() {
        // Given
        List<PostByUser> posts = Arrays.asList(testPostByUser);
        when(postByUserRepository.findByUserIdWithLimit(testUser.getId(), 10)).thenReturn(posts);

        // When
        List<PostByUser> result = postService.getUserPosts(testUser.getId(), 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testPostByUser);
        verify(postByUserRepository).findByUserIdWithLimit(testUser.getId(), 10);
    }

    @Test
    void getUserPosts_WithoutLimit_Success() {
        // Given
        List<PostByUser> posts = Arrays.asList(testPostByUser);
        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(posts);

        // When
        List<PostByUser> result = postService.getUserPosts(testUser.getId(), 0);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testPostByUser);
        verify(postByUserRepository).findByUserId(testUser.getId());
    }

    @Test
    void getUserPostsByStatus_Success() {
        // Given
        List<PostByUserStatus> statusPosts = Arrays.asList(testPostByUserStatus);
        when(postByUserStatusRepository.findByUserIdAndStatus(testUser.getId(), "DRAFT")).thenReturn(statusPosts);

        // When
        List<PostByUser> result = postService.getUserPostsByStatus(testUser.getId(), "DRAFT");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(testPostByUser.getTitle());
        assertThat(result.get(0).getContent()).isEqualTo(testPostByUser.getContent());
        assertThat(result.get(0).getStatus()).isEqualTo(testPostByUser.getStatus());
        verify(postByUserStatusRepository).findByUserIdAndStatus(testUser.getId(), "DRAFT");
    }

    @Test
    void getPostById_Success() {
        // Given
        UUID postId = testPostById.getPostId();
        when(postByIdRepository.findById(postId)).thenReturn(Optional.of(testPostById));

        // When
        Optional<PostById> result = postService.getPostById(postId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testPostById);
        verify(postByIdRepository).findById(postId);
    }

    @Test
    void publishPost_Success() {
        // Given
        UUID postId = testPostByUser.getPostId();
        List<PostByUser> userPosts = Arrays.asList(testPostByUser);

        when(postByUserRepository.findByUserId(testUser.getId())).thenReturn(userPosts);
        when(postByUserRepository.save(any(PostByUser.class))).thenReturn(testPostByUser);
        when(postByIdRepository.save(any(PostById.class))).thenReturn(testPostById);
        when(postByUserStatusRepository.save(any(PostByUserStatus.class))).thenReturn(testPostByUserStatus);
        when(userStatsRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(testUserStats);

        // When
        PostByUser result = postService.publishPost(testUser.getId(), postId);

        // Then
        assertThat(result).isNotNull();
        verify(postByUserRepository).findByUserId(testUser.getId());
        verify(postByUserRepository).save(any(PostByUser.class));
        verify(postByIdRepository).save(any(PostById.class));
        verify(postByUserStatusRepository).deleteById(any(PostByUserStatusKey.class));
        verify(postByUserStatusRepository).save(any(PostByUserStatus.class));
        verify(userStatsRepository, times(2)).findById(testUser.getId());
        verify(userStatsRepository, times(2)).save(any(UserStats.class));
    }

    @Test
    void getUserPostsByStatus_EmptyList_Success() {
        // Given
        when(postByUserStatusRepository.findByUserIdAndStatus(testUser.getId(), "PUBLISHED"))
                .thenReturn(Collections.emptyList());

        // When
        List<PostByUser> result = postService.getUserPostsByStatus(testUser.getId(), "PUBLISHED");

        // Then
        assertThat(result).isEmpty();
        verify(postByUserStatusRepository).findByUserIdAndStatus(testUser.getId(), "PUBLISHED");
    }

    @Test
    void getUserPostsByStatus_MultipleResults_Success() {
        // Given
        PostByUserStatus status1 = PostByUserStatus.fromPostByUser(testPostByUser);
        PostByUser testPostByUser2 = new PostByUser(testUser.getId(), "Test Post 2", "Test content 2");
        PostByUserStatus status2 = PostByUserStatus.fromPostByUser(testPostByUser2);
        
        List<PostByUserStatus> statusPosts = Arrays.asList(status1, status2);
        when(postByUserStatusRepository.findByUserIdAndStatus(testUser.getId(), "DRAFT")).thenReturn(statusPosts);

        // When
        List<PostByUser> result = postService.getUserPostsByStatus(testUser.getId(), "DRAFT");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Post");
        assertThat(result.get(1).getTitle()).isEqualTo("Test Post 2");
        verify(postByUserStatusRepository).findByUserIdAndStatus(testUser.getId(), "DRAFT");
    }
}