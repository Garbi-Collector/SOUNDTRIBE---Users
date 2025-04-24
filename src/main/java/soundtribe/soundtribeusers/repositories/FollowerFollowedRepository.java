package soundtribe.soundtribeusers.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import soundtribe.soundtribeusers.entities.FollowerFollowedEntity;
import soundtribe.soundtribeusers.entities.UserEntity;

import java.util.List;
import java.util.Optional;

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

    Long countByFollowed(UserEntity user);
    Long countByFollower(UserEntity user);


    Optional<FollowerFollowedEntity> findByFollowerAndFollowed(UserEntity follower, UserEntity followed);

}
