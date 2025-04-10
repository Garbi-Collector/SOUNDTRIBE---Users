package gabri.dev.javaspringcompose.repositories;

import gabri.dev.javaspringcompose.entities.FotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepository extends JpaRepository<FotoEntity, Long> {
}
