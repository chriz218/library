package posmy.interview.boot.security.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import posmy.interview.boot.security.jwt.entity.Jwt;

import java.util.List;
import java.util.Optional;

@Repository
public interface JwtRepository extends JpaRepository<Jwt, String> {
    Optional<Jwt> findById(String id);

    @Query(value = "SELECT * FROM JWT j WHERE j.USERNAME = :username", nativeQuery = true)
    List<Jwt> findJwtsByUsername(@Param("username") String username);
}
