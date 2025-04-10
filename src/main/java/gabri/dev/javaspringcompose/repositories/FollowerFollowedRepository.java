package gabri.dev.javaspringcompose.repositories;

import gabri.dev.javaspringcompose.entities.FollowerFollowedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerFollowedRepository extends JpaRepository<FollowerFollowedEntity, Long> {
}
