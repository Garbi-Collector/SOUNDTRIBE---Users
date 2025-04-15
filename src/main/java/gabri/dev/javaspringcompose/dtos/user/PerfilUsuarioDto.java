package gabri.dev.javaspringcompose.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfilUsuarioDto {
    private String username;
    private String fotoUrl;
}
