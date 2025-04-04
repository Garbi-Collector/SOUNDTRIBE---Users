package gabri.dev.javaspringcompose.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dto dummy para la respuesta de la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyResponseDto {
    /**
     * id.
     */
    private UUID id;
    /**
     * nombre.
     */
    private String nombre;
}
