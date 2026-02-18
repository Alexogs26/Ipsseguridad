package ag.ipsseguridad.config;

import ag.ipsseguridad.model.Role;
import ag.ipsseguridad.model.User;
import ag.ipsseguridad.repository.RoleRepository;
import ag.ipsseguridad.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
           Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
               return roleRepository.save(new Role(null, "ROLE_ADMIN"));
           });

           User admin = userRepository.findByEmail(adminEmail).orElseGet(null);

           if (admin == null) {
               admin = new User();
               admin.setUsername("Admin Principal");
               admin.setEmail(adminEmail);
               admin.setPassword(passwordEncoder.encode(adminPassword));
               admin.setRoles(Collections.singletonList(adminRole));
               userRepository.save(admin);
           } else {
               admin.setRoles(Collections.singletonList(adminRole));
               admin.setPassword(passwordEncoder.encode(adminPassword));
               userRepository.save(admin);
           }
        };
    }
}
