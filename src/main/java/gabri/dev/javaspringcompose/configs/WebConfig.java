package gabri.dev.javaspringcompose.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * WebConfig class to configure global
 * settings for the Spring Boot application.
 * This class implements {@link WebMvcConfigurer}
 * to customize the default configuration
 * for Cross-Origin Resource Sharing (CORS).
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mapping for all endpoints
     * in the application.
     * @param registry the {@link CorsRegistry}
     *                 used to register the CORS configuration.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
