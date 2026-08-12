package com.greentrack.service;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class FcmService {
    @Async
    public void sendPush(String token, String title, String body) {
        try {
            Message msg = Message.builder().setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putData("click_action","FLUTTER_NOTIFICATION_CLICK").build();
            FirebaseMessaging.getInstance().send(msg);
            log.info("FCM push sent");
        } catch (Exception e) { log.error("FCM push failed: {}", e.getMessage()); }
    }
}