package gabri.dev.javaspringcompose.repositories;

import gabri.dev.javaspringcompose.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsernameOrEmail(String emailOrUsername, String emailOrUsername1);

    List<UserEntity> findAllByEnabledTrue();
}
