package com.example.userservice.controller;

import com.example.userservice.dto.ChangePasswordRequest;
import com.example.userservice.dto.UpdateProfileRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
   private final UserService userService;

   public UserController(UserService userService) {
      this.userService = userService;
   }

   @GetMapping("/me")
   public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
      return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
   }

   @PutMapping("/me")
   public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
         @Valid @RequestBody UpdateProfileRequest request) {
      return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
   }

   @PutMapping("/me/change-password")
   public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails,
         @Valid @RequestBody ChangePasswordRequest request) {
      userService.changePassword(userDetails.getUsername(), request);
      return ResponseEntity.noContent().build();
   }

   @DeleteMapping("/me")
   public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
      userService.deleteAccount(userDetails.getUsername());
      return ResponseEntity.noContent().build();
   }
}
