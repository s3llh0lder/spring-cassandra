package example.repository;

import example.model.PostByUserStatus;
import example.model.PostByUserStatusKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostByUserStatusRepository extends CassandraRepository<PostByUserStatus, PostByUserStatusKey> {

    @Query("SELECT * FROM posts_by_user_status WHERE user_id = ?0 AND status = ?1")
    List<PostByUserStatus> findByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT * FROM posts_by_user_status WHERE user_id = ?0 AND status = ?1 LIMIT ?2")
    List<PostByUserStatus> findByUserIdAndStatusWithLimit(UUID userId, String status, int limit);

    @Query("SELECT * FROM posts_by_user_status WHERE user_id = ?0")
    List<PostByUserStatus> findByUserId(UUID userId);
}