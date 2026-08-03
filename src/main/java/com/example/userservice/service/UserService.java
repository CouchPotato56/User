package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.entity.RefreshToken;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.exception.UnauthorizedException;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtProperties;
import com.example.userservice.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
   private final UserRepository userRepository;
   private final RefreshTokenRepository refreshTokenRepository;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;
   private final JwtTokenProvider jwtTokenProvider;
   private final JwtProperties jwtProperties;

   public UserService(UserRepository userRepository,
         RefreshTokenRepository refreshTokenRepository,
         PasswordEncoder passwordEncoder,
         AuthenticationManager authenticationManager,
         JwtTokenProvider jwtTokenProvider,
         JwtProperties jwtProperties) {
      this.userRepository = userRepository;
      this.refreshTokenRepository = refreshTokenRepository;
      this.passwordEncoder = passwordEncoder;
      this.authenticationManager = authenticationManager;
      this.jwtTokenProvider = jwtTokenProvider;
      this.jwtProperties = jwtProperties;
   }

   public User registerUser(RegistrationRequest request) {
      if (userRepository.existsByUsername(request.getUsername())) {
         throw new UnauthorizedException("Username already taken");
      }
      if (userRepository.existsByEmail(request.getEmail())) {
         throw new UnauthorizedException("Email already in use");
      }

      User user = new User();
      user.setUsername(request.getUsername());
      user.setEmail(request.getEmail());
      user.setPassword(passwordEncoder.encode(request.getPassword()));
      user.setRoles(Collections.singleton(Role.ROLE_USER));
      user.setActive(true);
      user.setDeleted(false);

      return userRepository.save(user);
   }

   public TokenResponse authenticate(LoginRequest request) {
      Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

      String token = jwtTokenProvider.generateToken(authentication);
      User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      RefreshToken refreshToken = createRefreshToken(user);
      return new TokenResponse(token, refreshToken.getToken());
   }

   public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
      RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

      if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
         refreshTokenRepository.delete(refreshToken);
         throw new UnauthorizedException("Refresh token expired");
      }

      Authentication authentication = new UsernamePasswordAuthenticationToken(
            refreshToken.getUser().getUsername(),
            null,
            Collections.singletonList(
                  new org.springframework.security.core.authority.SimpleGrantedAuthority(Role.ROLE_USER.name())));

      String token = jwtTokenProvider.generateToken(authentication);
      return new TokenResponse(token, refreshToken.getToken());
   }

   public void logout(String refreshTokenValue) {
      refreshTokenRepository.findByToken(refreshTokenValue)
            .ifPresent(refreshTokenRepository::delete);
   }

   public void forgotPassword(ForgotPasswordRequest request) {
      User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      // In a production grade service this would send email with reset token.
   }

   public void resetPassword(ResetPasswordRequest request) {
      String token = request.getToken();
      // Simplified flow: token is username in this sample, do not use in production.
      User user = userRepository.findByUsername(token)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

      user.setPassword(passwordEncoder.encode(request.getNewPassword()));
      userRepository.save(user);
   }

   public UserProfileResponse getProfile(String username) {
      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.isActive());
   }

   public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
         throw new UnauthorizedException("Email already in use");
      }
      if (!request.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
         throw new UnauthorizedException("Username already taken");
      }

      user.setUsername(request.getUsername());
      user.setEmail(request.getEmail());
      return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.isActive());
   }

   public void changePassword(String username, ChangePasswordRequest request) {
      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
         throw new UnauthorizedException("Invalid current password");
      }

      user.setPassword(passwordEncoder.encode(request.getNewPassword()));
   }

   public void deleteAccount(String username) {
      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      user.setDeleted(true);
      user.setActive(false);
      user.setDeletedAt(Instant.now());
   }

   private RefreshToken createRefreshToken(User user) {
      RefreshToken refreshToken = new RefreshToken();
      refreshToken.setToken(UUID.randomUUID().toString());
      refreshToken.setUser(user);
      refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()));
      return refreshTokenRepository.save(refreshToken);
   }
}
