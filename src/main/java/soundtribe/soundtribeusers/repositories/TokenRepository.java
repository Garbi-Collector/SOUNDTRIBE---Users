package soundtribe.soundtribeusers.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soundtribe.soundtribeusers.entities.TokenEntity;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity,Long> {
    Optional<TokenEntity> findByToken(String token);
}
