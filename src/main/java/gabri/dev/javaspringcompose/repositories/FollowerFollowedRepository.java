package gabri.dev.javaspringcompose.repositories;

import gabri.dev.javaspringcompose.entities.FollowerFollowedEntity;
import gabri.dev.javaspringcompose.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface FollowerFollowedRepository extends JpaRepository<FollowerFollowedEntity, Long> {

    @Query("""
    SELECT f.followed
    FROM FollowerFollowedEntity f
    WHERE f.follower.id = :followerId AND f.followed.rol = 'ARTISTA'
    ORDER BY (
        SELECT COUNT(f2.follower.id)
        FROM FollowerFollowedEntity f2
        WHERE f2.followed.id = f.followed.id
    ) DESC
""")
    List<UserEntity> findTop5FollowedArtistsByFollowerId(@Param("followerId") Long followerId, Pageable pageable);

    boolean existsByFollowerAndFollowed(UserEntity follower, UserEntity followed);
}
