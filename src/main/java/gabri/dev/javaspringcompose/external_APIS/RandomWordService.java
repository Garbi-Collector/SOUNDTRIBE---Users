package gabri.dev.javaspringcompose.external_APIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.regex.Pattern;

@Service
public class RandomWordService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://random-word-api.herokuapp.com/word?lang=es";

    // Expresión regular para verificar que la palabra no contenga caracteres no válidos (como emojis, símbolos, etc.)
    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$");

    public String obtenerPalabraAleatoria() {
        String palabra = "default";

        // Reintentar hasta encontrar una palabra válida
        while (palabra.equals("default")) {
            String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .queryParam("number", 1)
                    .toUriString();

            String[] palabras = restTemplate.getForObject(url, String[].class);

            // Verificar si la palabra es válida
            assert palabras != null;
            for (String palabraTemporal : palabras) {
                // Verificar que tenga entre 2 y 7 caracteres, que no contenga espacios y que sea alfabética
                if (palabraTemporal.length() >= 2 && palabraTemporal.length() <= 7
                        && !palabraTemporal.contains(" ")
                        && VALID_CHARACTERS_PATTERN.matcher(palabraTemporal).matches()) {
                    palabra = palabraTemporal;
                    break;
                }
            }
        }

        return palabra;  // Retorna la palabra válida
    }
}
