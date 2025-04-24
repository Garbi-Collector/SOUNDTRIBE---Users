package soundtribe.soundtribeusers.external_APIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Random;
import java.util.regex.Pattern;

@Service
public class RandomWordService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private Random random = new Random();

    private static final String API_URL = "https://random-word-api.herokuapp.com/word?lang=es";

    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$");

    public String obtenerPalabraAleatoria() {
        try {
            String palabra = "default";

            // Intentar obtener una palabra válida desde la API
            while (palabra.equals("default")) {
                String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                        .queryParam("number", 1)
                        .toUriString();

                String[] palabras = restTemplate.getForObject(url, String[].class);

                // Si la respuesta es nula o vacía, salimos del bucle para usar el backup local
                if (palabras == null || palabras.length == 0) break;

                for (String palabraTemporal : palabras) {
                    if (palabraTemporal.length() >= 2 && palabraTemporal.length() <= 7
                            && !palabraTemporal.contains(" ")
                            && VALID_CHARACTERS_PATTERN.matcher(palabraTemporal).matches()) {
                        palabra = palabraTemporal;
                        break;
                    }
                }

                // Si ninguna palabra válida fue encontrada, cortamos el ciclo
                break;
            }

            // Si aún es "default", es que no conseguimos una válida
            if (palabra.equals("default")) {
                return obtenerDesdeRandom(); // backup del repo local
            }

            return palabra;
        } catch (Exception e) {
            // Si algo falla (API caída, red, etc.), usamos backup
            return obtenerDesdeRandom();
        }
    }

    public String obtenerDesdeRandom() {
        // Longitud aleatoria entre 2 y 7
        int longitud = random.nextInt(6) + 2;

        StringBuilder palabra = new StringBuilder(longitud);

        for (int i = 0; i < longitud; i++) {
            int indice = random.nextInt(ALFABETO.length());
            palabra.append(ALFABETO.charAt(indice));
        }

        return palabra.toString();
    }

}
