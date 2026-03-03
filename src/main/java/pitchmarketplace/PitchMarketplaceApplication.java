package pitchmarketplace;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PitchMarketplaceApplication {

    protected PitchMarketplaceApplication() {
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Minsk"));
        SpringApplication.run(PitchMarketplaceApplication.class, args);
    }
}
