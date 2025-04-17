package gabri.dev.javaspringcompose.dtos.userExperience;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDescription {
    String username;
    String description;
    String rol;
    String urlimage;
    LocalDateTime createdAt;
    List<UserGet> ArtistasSeguidos;
}
