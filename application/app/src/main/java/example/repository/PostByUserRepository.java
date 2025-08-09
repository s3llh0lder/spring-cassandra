package example.repository;

import example.model.PostByUser;
import example.model.PostByUserKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostByUserRepository extends CassandraRepository<PostByUser, PostByUserKey> {

    @Query("SELECT * FROM posts_by_user WHERE user_id = ?0")
    List<PostByUser> findByUserId(UUID userId);

    @Query("SELECT * FROM posts_by_user WHERE user_id = ?0 LIMIT ?1")
    List<PostByUser> findByUserIdWithLimit(UUID userId, int limit);

//    @Query("SELECT * FROM posts_by_user WHERE user_id = ?0 AND status = ?1")
//    List<PostByUser> findByUserIdAndStatus(UUID userId, String status);
}