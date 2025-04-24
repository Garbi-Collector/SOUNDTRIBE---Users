package soundtribe.soundtribeusers.dtos.auth;

import soundtribe.soundtribeusers.models.enums.Rol;
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
