package gabri.dev.javaspringcompose.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Doc configuration class.
 */
@Configuration
public class SpringDocConfig {

    /**
     * The app name mapped from application config.
     */
    @Value("${app.url}") private String url;

    /**
     * The developer name mapped from application config.
     */
    @Value("${app.dev-name}")private String devName;

    /**
     * The developer email mapped from application config.
     */
    @Value("${app.dev-email}")private String devEmail;

    /**
     * The open api bean.
     * @param appName the name of this app to be added in Open API
     * @param appDescription the description to be added in Open API
     * @param appVersion the version to be added in Open API
     * @return the open api configuration.
     */
    @Bean
    public OpenAPI openApi(@Value("${app.name}") String appName,
                            @Value("${app.desc}") String appDescription,
                            @Value("${app.version}") String appVersion) {
        Info info = new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescription)
                .contact(
                        new Contact()
                                .name(devName)
                                .email(devEmail));

        Server server = new Server()
                .url(url)
                .description(appDescription);

        return new OpenAPI()
                .components(new Components())
                .info(info)
                .addServersItem(server);
    }

    /**
     * The Model Resolver bean.
     * @param objectMapper The object mapper
     * @return the modelResolver to use in Open API
     */
    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}
