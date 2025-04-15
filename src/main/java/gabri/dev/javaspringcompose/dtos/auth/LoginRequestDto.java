package gabri.dev.javaspringcompose.dtos.auth;

import gabri.dev.javaspringcompose.models.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    private String emailOrUsername;
    private String password;
}
