package example.domain.repository;

import example.domain.model.UserByEmail;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserByEmailRepository extends CassandraRepository<UserByEmail, String> {
    // Email is the primary key, so this is efficient
}