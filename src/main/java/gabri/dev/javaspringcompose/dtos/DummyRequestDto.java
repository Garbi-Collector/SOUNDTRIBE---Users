package gabri.dev.javaspringcompose.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto dummy para la llamada a API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyRequestDto {
    /**
     * el nombre del dummy.
     */
    private String nombre;
}
