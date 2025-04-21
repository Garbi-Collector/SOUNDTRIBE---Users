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
    private Long id;
    private String username;
    private String description;
    private String rol;
    private String urlimage;
    private String slug;
    private LocalDateTime createdAt;
    private Long followersCount;
    private Long followedsCount;
    private List<UserGet> ArtistasSeguidos;
}
