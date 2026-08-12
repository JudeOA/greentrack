package com.greentrack.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.*;

@Configuration @Slf4j
public class FirebaseConfig {
    @Value("${app.firebase.credentials-path}") private String credentialsPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream svc = new FileInputStream(credentialsPath);
                FirebaseOptions opts = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(svc)).build();
                FirebaseApp.initializeApp(opts);
                log.info("Firebase initialised");
            }
        } catch (IOException e) {
            log.warn("Firebase credentials not found — push notifications disabled");
        }
    }
}