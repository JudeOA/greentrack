package com.greentrack.config;

import com.greentrack.entity.User;
import com.greentrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures the three demo accounts exist with a known password ("Admin@2026").
 * Runs on every startup. Because it uses the SAME PasswordEncoder that login
 * uses, the password is guaranteed to match — no hand-written bcrypt hashes.
 * If an account already exists (e.g. with a bad placeholder hash), its password
 * and role are corrected so the demo logins always work.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Admin@2026";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            upsert("GreenTrack Admin", "admin@greentrack.app", User.Role.ADMIN,     "+233200000001", null);
            upsert("Kofi Mensah",      "kofi@greentrack.app",  User.Role.COLLECTOR, "+233200000002", "GT-COL-001");
            upsert("Ama Darko",        "ama@greentrack.app",   User.Role.CITIZEN,   "+233200000003", null);
            log.info("Demo accounts ensured (admin/collector/citizen, password '{}').", DEMO_PASSWORD);
        } catch (Exception e) {
            log.warn("DataSeeder failed: {}", e.getMessage(), e);
        }
    }

    private void upsert(String name, String email, User.Role role, String phone, String badgeId) {
        User existing = userRepository.findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst().orElse(null);

        String hash = passwordEncoder.encode(DEMO_PASSWORD);

        if (existing == null) {
            User u = User.builder()
                    .name(name).email(email).role(role).phone(phone).badgeId(badgeId)
                    .passwordHash(hash).isActive(true)
                    .build();
            userRepository.save(u);
            log.info("Seeded new user {}", email);
        } else {
            existing.setPasswordHash(hash);
            existing.setRole(role);
            existing.setActive(true);
            if (badgeId != null) existing.setBadgeId(badgeId);
            userRepository.save(existing);
            log.info("Reset demo user {}", email);
        }
    }
}
