package soundtribe.soundtribeusers.external_APIS;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import soundtribe.soundtribeusers.dtos.notis.NotificationPost;
import soundtribe.soundtribeusers.dtos.notis.NotificationType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    // URL base configurable (por si más adelante va a Docker o cambia de puerto)
    @Value("${external.notification-service.url}")
    private String baseUrl;

    public void enviarNotificacion(String jwtToken, NotificationPost noti) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken); // Agrega el Bearer Token automáticamente

        HttpEntity<NotificationPost> request = new HttpEntity<>(noti, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al enviar la notificación: " + response.getBody());
        }
    }
}
