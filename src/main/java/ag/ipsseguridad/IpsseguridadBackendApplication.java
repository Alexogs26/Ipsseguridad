package ag.ipsseguridad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IpsseguridadBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpsseguridadBackendApplication.class, args);
	}

}
