package soundtribe.soundtribeusers.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEmail {
    private String destinatario;
    private String asunto;
    private String mensaje;
}
