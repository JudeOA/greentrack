package com.greentrack.dto.response;
import com.greentrack.entity.User;
import lombok.*;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserSummary user;

    @Data @Builder
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private User.Role role;
        private String badgeId;
    }
}