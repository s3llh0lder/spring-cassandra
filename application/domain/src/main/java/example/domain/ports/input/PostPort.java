package example.domain.ports.input;

import example.domain.model.PostById;
import example.domain.model.PostByUser;
import example.domain.exceptions.PostNotFoundException;
import example.domain.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostPort {
    
    /**
     * Create a new post for a user
     * @param userId User ID
     * @param request Post creation request
     * @return Created post
     * @throws UserNotFoundException if user not found
     */
    PostByUser createPost(UUID userId, CreatePostRequest request) throws UserNotFoundException;
    
    /**
     * Update an existing post
     * @param userId User ID
     * @param postId Post ID
     * @param request Update request
     * @return Updated post
     * @throws PostNotFoundException if post not found
     */
    PostByUser updatePost(UUID userId, UUID postId, UpdatePostRequest request) throws PostNotFoundException;
    
    /**
     * Delete a post
     * @param userId User ID
     * @param postId Post ID
     * @throws PostNotFoundException if post not found
     */
    void deletePost(UUID userId, UUID postId) throws PostNotFoundException;
    
    /**
     * Get user posts with limit
     * @param userId User ID
     * @param limit Maximum number of posts
     * @return List of user posts
     */
    List<PostByUser> getUserPosts(UUID userId, int limit);
    
    /**
     * Get user posts by status
     * @param userId User ID
     * @param status Post status
     * @return List of user posts with status
     */
    List<PostByUser> getUserPostsByStatus(UUID userId, String status);
    
    /**
     * Get post by ID
     * @param postId Post ID
     * @return Optional post
     */
    Optional<PostById> getPostById(UUID postId);
    
    /**
     * Publish a draft post
     * @param userId User ID
     * @param postId Post ID
     * @return Published post
     * @throws PostNotFoundException if post not found
     */
    PostByUser publishPost(UUID userId, UUID postId) throws PostNotFoundException;
    
//    /**
//     * Post creation request
//     */
//    public static class CreatePostRequest {
//        private String title;
//        private String content;
//
//        public CreatePostRequest() {}
//
//        public CreatePostRequest(String title, String content) {
//            this.title = title;
//            this.content = content;
//        }
//
//        public String getTitle() { return title; }
//        public void setTitle(String title) { this.title = title; }
//
//        public String getContent() { return content; }
//        public void setContent(String content) { this.content = content; }
//    }
//
//    /**
//     * Post update request
//     */
//    public static class UpdatePostRequest {
//        private String title;
//        private String content;
//
//        public UpdatePostRequest() {}
//
//        public UpdatePostRequest(String title, String content) {
//            this.title = title;
//            this.content = content;
//        }
//
//        public String getTitle() { return title; }
//        public void setTitle(String title) { this.title = title; }
//
//        public String getContent() { return content; }
//        public void setContent(String content) { this.content = content; }
//    }
}
