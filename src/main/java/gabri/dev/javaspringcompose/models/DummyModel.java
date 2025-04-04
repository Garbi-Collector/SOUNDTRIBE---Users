package gabri.dev.javaspringcompose.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
/**
 * clase dummy de ejemplo dummy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyModel {

    /**
     * para guardar el id, de importancia en la DB.
     */
    private UUID id;
    /**
     * el nombre de mi dummy.
     */
    private String nombre;
}
