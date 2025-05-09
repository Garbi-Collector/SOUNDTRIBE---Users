package soundtribe.soundtribeusers.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soundtribe.soundtribeusers.entities.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findAllByEnabledTrue();

    Optional<Object> findBySlug(String slug);

    boolean existsBySlug(String slug);
}