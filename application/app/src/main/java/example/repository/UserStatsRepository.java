package example.repository;

import example.model.UserStats;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserStatsRepository extends CassandraRepository<UserStats, UUID> {
}