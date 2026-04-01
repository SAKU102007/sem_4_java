package pitchmarketplace;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Football Pitch Marketplace API",
                version = "1.0.0",
                description = "REST API for users, pitches, bookings, open games, equipment offers and demo endpoints."
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local development server")
)
public class PitchMarketplaceApplication {

    protected PitchMarketplaceApplication() {
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Minsk"));
        SpringApplication.run(PitchMarketplaceApplication.class, args);
    }
}
