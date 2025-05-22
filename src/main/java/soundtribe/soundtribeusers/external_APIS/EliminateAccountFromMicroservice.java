package soundtribe.soundtribeusers.external_APIS;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class EliminateAccountFromMicroservice {

    private final RestTemplate restTemplate;

    @Value("${music.back.url}")
    private String musicurl;

    @Value("${notification.back.url}")
    private String notiurl;

    @Value("${donation.back.url}")
    private String donationurl;


    public void eliminarDonacionesDelUsuario(String jwtToken) {
        String url = donationurl + "/eliminate-donor";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("✅ Donaciones eliminadas correctamente para el usuario.");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar donaciones: " + e.getMessage());
        }
    }

    public void eliminarNotificacionesDelUsuario(String jwtToken) {
        String url = notiurl + "/eliminate-notification";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("✅ Notificaciones eliminadas correctamente para el usuario.");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar notificaciones: " + e.getMessage());
        }
    }

    public void eliminarMusicaDelUsuario(String jwtToken) {
        String url = musicurl + "/eliminate-music";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("✅ Música eliminada correctamente para el usuario.");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar música: " + e.getMessage());
        }
    }



}
