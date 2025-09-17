package com.example.booking.config;

import com.example.booking.model.ResourceEntity;
import com.example.booking.model.Role;
import com.example.booking.model.User;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceRepository resourceRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            User u = new User();
            u.setUsername("user");
            u.setPassword(passwordEncoder.encode("user123"));

            u.setRoles(Set.of(Role.ROLE_USER));
            userRepository.save(u);
        }


        if (resourceRepository.count() == 0) {
            ResourceEntity r1 = new ResourceEntity();
            r1.setName("Conference Room A");
            r1.setType("Room");
            r1.setDescription("Large conference room, capacity 12");
            r1.setCapacity(12);
            r1.setActive(true);
            resourceRepository.save(r1);

            ResourceEntity r2 = new ResourceEntity();
            r2.setName("Projector X200");
            r2.setType("Equipment");
            r2.setDescription("Portable HD Projector");
            r2.setCapacity(1);
            r2.setActive(true);
            resourceRepository.save(r2);
        }

        System.out.println(" Seeded default users/resources (if absent).");
    }
}
