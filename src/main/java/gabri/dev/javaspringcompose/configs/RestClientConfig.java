package gabri.dev.javaspringcompose.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Class that configures a singleton RestClient.
 */
@Configuration
public class RestClientConfig {

    /**
     * Singleton of a restclient.
     * @return RestTemplate singleton.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
