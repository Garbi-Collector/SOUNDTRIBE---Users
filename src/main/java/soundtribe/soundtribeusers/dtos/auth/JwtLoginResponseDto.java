package soundtribe.soundtribeusers.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtLoginResponseDto {
    private String token;
    private String username;
    private String email;
}
