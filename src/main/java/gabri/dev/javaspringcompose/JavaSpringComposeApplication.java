package gabri.dev.javaspringcompose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase Main.
 */
@SpringBootApplication
@EnableAsync
public class JavaSpringComposeApplication {
    /**
     * Main program.
     * @param args application args
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaSpringComposeApplication.class, args);
    }
}
