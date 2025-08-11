package example.domain.repository;

import example.domain.model.PostById;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostByIdRepository extends CassandraRepository<PostById, UUID> {

    @Query("SELECT * FROM posts_by_id WHERE user_id = ?0")
    List<PostById> findByUserId(UUID userId);
}