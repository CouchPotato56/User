package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
   private final UserService userService;

   public AuthController(UserService userService) {
      this.userService = userService;
   }

   @PostMapping("/register")
   public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegistrationRequest request) {
      return ResponseEntity.ok(userService.getProfile(userService.registerUser(request).getUsername()));
   }

   @PostMapping("/login")
   public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
      return ResponseEntity.ok(userService.authenticate(request));
   }

   @PostMapping("/refresh-token")
   public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
      return ResponseEntity.ok(userService.refreshAccessToken(request));
   }

   @PostMapping("/logout")
   public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
      userService.logout(request.getRefreshToken());
      return ResponseEntity.noContent().build();
   }

   @PostMapping("/forgot-password")
   public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
      userService.forgotPassword(request);
      return ResponseEntity.noContent().build();
   }

   @PostMapping("/reset-password")
   public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
      userService.resetPassword(request);
      return ResponseEntity.noContent().build();
   }
}
