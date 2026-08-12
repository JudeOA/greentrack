package com.greentrack.service;
import com.greentrack.dto.request.*;
import com.greentrack.dto.response.AuthResponse;
import com.greentrack.entity.User;
import com.greentrack.exception.BusinessException;
import com.greentrack.repository.UserRepository;
import com.greentrack.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        return createUser(req.getName(), req.getEmail(), req.getPassword(), req.getPhone(), User.Role.CITIZEN, null);
    }

    @Transactional
    public AuthResponse createStaff(CreateStaffRequest req) {
        if (req.getRole() == User.Role.CITIZEN) throw new BusinessException("Use /api/auth/register for citizens");
        return createUser(req.getName(), req.getEmail(), req.getPassword(), req.getPhone(), req.getRole(), req.getBadgeId());
    }

    private AuthResponse createUser(String name, String email, String password, String phone, User.Role role, String badgeId) {
        if (userRepository.existsByEmail(email)) throw new BusinessException("Email already registered");
        if (role == User.Role.COLLECTOR && badgeId != null && userRepository.existsByBadgeId(badgeId))
            throw new BusinessException("Badge ID already registered");
        User user = User.builder().name(name).email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role).phone(phone).badgeId(badgeId).isActive(true).build();
        return buildResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        return buildResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) throw new BusinessException("Invalid refresh token");
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("User not found"));
        if (!user.isActive()) throw new BusinessException("Account deactivated. Contact support.");
        return buildResponse(user);
    }

    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        userRepository.findById(userId).ifPresent(u -> { u.setFcmToken(fcmToken); userRepository.save(u); });
    }

    private AuthResponse buildResponse(User u) {
        return AuthResponse.builder()
            .accessToken(jwtUtil.generateToken(u)).refreshToken(jwtUtil.generateRefreshToken(u))
            .tokenType("Bearer")
            .user(AuthResponse.UserSummary.builder().id(u.getId()).name(u.getName())
                .email(u.getEmail()).phone(u.getPhone()).role(u.getRole()).badgeId(u.getBadgeId()).build())
            .build();
    }
}