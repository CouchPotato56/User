package com.example.userservice.service;

import com.example.userservice.dto.ChangePasswordRequest;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegistrationRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.entity.RefreshToken;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtProperties;
import com.example.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
   @Mock
   private UserRepository userRepository;

   @Mock
   private RefreshTokenRepository refreshTokenRepository;

   @Mock
   private AuthenticationManager authenticationManager;

   @Mock
   private JwtTokenProvider jwtTokenProvider;

   @Mock
   private JwtProperties jwtProperties;

   @Captor
   private ArgumentCaptor<User> userCaptor;

   private PasswordEncoder passwordEncoder;

   @InjectMocks
   private UserService userService;

   @BeforeEach
   void setUp() {
      passwordEncoder = new BCryptPasswordEncoder();
      userService = new UserService(userRepository, refreshTokenRepository, passwordEncoder, authenticationManager,
            jwtTokenProvider, jwtProperties);
   }

   @Test
   void registerUser_shouldSaveNewUser() {
      RegistrationRequest request = new RegistrationRequest();
      request.setUsername("testuser");
      request.setEmail("test@example.com");
      request.setPassword("Password123");

      when(userRepository.existsByUsername("testuser")).thenReturn(false);
      when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      User created = userService.registerUser(request);

      verify(userRepository).save(userCaptor.capture());
      User saved = userCaptor.getValue();

      assertThat(saved.getUsername()).isEqualTo("testuser");
      assertThat(saved.getEmail()).isEqualTo("test@example.com");
      assertThat(passwordEncoder.matches("Password123", saved.getPassword())).isTrue();
      assertThat(saved.getRoles()).containsExactly(Role.ROLE_USER);
      assertThat(created.isActive()).isTrue();
   }

   @Test
   void authenticate_shouldReturnTokenResponse() {
      LoginRequest request = new LoginRequest();
      request.setUsername("testuser");
      request.setPassword("Password123");

      User user = new User();
      user.setUsername("testuser");
      user.setPassword(passwordEncoder.encode("Password123"));
      user.setEmail("test@example.com");
      user.setRoles(Collections.singleton(Role.ROLE_USER));
      user.setActive(true);

      Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "Password123");

      when(authenticationManager.authenticate(any())).thenReturn(authentication);
      when(jwtTokenProvider.generateToken(authentication)).thenReturn("access-token-abc");
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(jwtProperties.getRefreshExpirationMs()).thenReturn(3600000L);

      var response = userService.authenticate(request);

      assertThat(response.getAccessToken()).isEqualTo("access-token-abc");
      assertThat(response.getRefreshToken()).isNotBlank();
   }

   @Test
   void getProfile_shouldReturnUserProfile() {
      User user = new User();
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setActive(true);

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

      UserProfileResponse profile = userService.getProfile("testuser");

      assertThat(profile.getUsername()).isEqualTo("testuser");
      assertThat(profile.getEmail()).isEqualTo("test@example.com");
      assertThat(profile.isActive()).isTrue();
   }

   @Test
   void getProfile_shouldThrowWhenUserNotFound() {
      when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getProfile("missing"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
   }

   @Test
   void changePassword_shouldUpdatePassword() {
      User user = new User();
      user.setUsername("testuser");
      user.setPassword(passwordEncoder.encode("OldPassword1"));

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setCurrentPassword("OldPassword1");
      request.setNewPassword("NewPassword2");

      userService.changePassword("testuser", request);

      assertThat(passwordEncoder.matches("NewPassword2", user.getPassword())).isTrue();
   }
}
