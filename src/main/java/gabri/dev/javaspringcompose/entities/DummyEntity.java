package gabri.dev.javaspringcompose.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * clase dummy de ejemplo dummy.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyEntity {

    /**
     * para guardar el id, de importancia en la DB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * el nombre de mi dummy.
     */
    @Column(name = "nombre", nullable = false)
    private String nombre;
}
