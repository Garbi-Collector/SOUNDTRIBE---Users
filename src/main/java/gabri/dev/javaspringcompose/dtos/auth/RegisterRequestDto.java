package gabri.dev.javaspringcompose.dtos.auth;

import gabri.dev.javaspringcompose.models.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    private String email;
    private String username;
    private String password;
    private Rol rol;
}
