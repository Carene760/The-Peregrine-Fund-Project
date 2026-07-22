package com.example.serveur.config;

import com.example.serveur.model.User;
import com.example.serveur.model.UserApp;
import com.example.serveur.repository.UserAppRepository;
import com.example.serveur.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * One-time startup migration: re-hashes any plaintext password still stored
 * in User_ (web admin accounts) or userapp (mobile field-agent accounts) into
 * BCrypt.
 *
 * This exists because the project has no formal DB migration tool
 * (spring.jpa.hibernate.ddl-auto=update) and pre-production test data was
 * seeded with plaintext passwords. It is safe to run on every startup: a
 * password already in BCrypt format ($2a$/$2b$/$2y$ prefix) is left
 * untouched, so this becomes a no-op once all rows have been migrated once.
 */
@Component
public class PasswordMigrationRunner implements ApplicationRunner {

    // BCrypt hashes always look like $2a$10$..., $2b$10$..., or $2y$10$...
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UserRepository userRepository;
    private final UserAppRepository userAppRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository,
                                    UserAppRepository userAppRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAppRepository = userAppRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateWebUsers();
        migrateMobileUsers();
    }

    private boolean isAlreadyHashed(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value).matches();
    }

    private void migrateWebUsers() {
        List<User> users = userRepository.findAll();
        int migrated = 0;
        for (User user : users) {
            String current = user.getMotDePasse();
            if (current == null || current.isBlank() || isAlreadyHashed(current)) {
                continue;
            }
            user.setMotDePasse(passwordEncoder.encode(current));
            userRepository.save(user);
            migrated++;
        }
        if (migrated > 0) {
            System.out.println("🔐 Migration mots de passe (User_): " + migrated + " compte(s) re-hashé(s) en BCrypt.");
        }
    }

    private void migrateMobileUsers() {
        List<UserApp> userApps = userAppRepository.findAll();
        int migrated = 0;
        for (UserApp userApp : userApps) {
            String current = userApp.getMotDePasse();
            if (current == null || current.isBlank() || isAlreadyHashed(current)) {
                continue;
            }
            userApp.setMotDePasse(passwordEncoder.encode(current));
            userAppRepository.save(userApp);
            migrated++;
        }
        if (migrated > 0) {
            System.out.println("🔐 Migration mots de passe (userapp): " + migrated + " compte(s) re-hashé(s) en BCrypt.");
        }
    }
}
