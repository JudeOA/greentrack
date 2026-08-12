package com.greentrack.service;

import com.greentrack.dto.request.CreateStaffRequest;
import com.greentrack.dto.request.RegisterRequest;
import com.greentrack.entity.User;
import com.greentrack.exception.BusinessException;
import com.greentrack.repository.UserRepository;
import com.greentrack.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, authManager);
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    // Public /api/auth/register must never be able to mint a non-CITIZEN account,
    // regardless of what the caller sends — this was a privilege-escalation hole.
    @Test
    void register_alwaysCreatesCitizenAccount() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Ama Darko");
        req.setEmail("ama@greentrack.app");
        req.setPassword("password1");

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(User.Role.CITIZEN);
    }

    @Test
    void createStaff_rejectsCitizenRole() {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setName("Someone");
        req.setEmail("someone@greentrack.app");
        req.setPassword("password1");
        req.setRole(User.Role.CITIZEN);

        assertThatThrownBy(() -> authService.createStaff(req))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createStaff_createsAdminAccountWithRequestedRole() {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setName("New Admin");
        req.setEmail("admin2@greentrack.app");
        req.setPassword("password1");
        req.setRole(User.Role.ADMIN);

        authService.createStaff(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Ama Darko");
        req.setEmail("ama@greentrack.app");
        req.setPassword("password1");
        when(userRepository.existsByEmail("ama@greentrack.app")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any());
    }

    // A deactivated account's refresh token must not silently mint new access tokens.
    @Test
    void refreshToken_rejectsDeactivatedAccount() {
        String token = "refresh-token";
        when(jwtUtil.isRefreshToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("ama@greentrack.app");
        User inactive = User.builder().id(1L).name("Ama").email("ama@greentrack.app")
                .passwordHash("hashed").role(User.Role.CITIZEN).isActive(false).build();
        when(userRepository.findByEmail("ama@greentrack.app")).thenReturn(java.util.Optional.of(inactive));

        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(BusinessException.class);
    }
}
