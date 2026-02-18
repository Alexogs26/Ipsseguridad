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

import java.util.Collection;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role role = new Role();
                role.setName("ROLE_USER");
                return roleRepository.save(role);
            });

            if (!userRepository.existsByEmail("ipsalarmasyseguridad@gmail.com")) {
                User admin = new User();
                admin.setUsername("Admin Principal");
                admin.setEmail("ipsalarmasyseguridad@gmail.com");
                admin.setPassword(passwordEncoder.encode("123"));
                admin.setRoles(Collections.singletonList(adminRole));
                userRepository.save(admin);
            }
        };
    }
}
