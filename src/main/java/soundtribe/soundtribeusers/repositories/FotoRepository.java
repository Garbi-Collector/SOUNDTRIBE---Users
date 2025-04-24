package soundtribe.soundtribeusers.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soundtribe.soundtribeusers.entities.FotoEntity;

import java.util.Optional;

@Repository
public interface FotoRepository extends JpaRepository<FotoEntity, Long> {
    Optional<FotoEntity> findByFileName(String fileName);

    boolean existsByFileName(String imageName);
}
