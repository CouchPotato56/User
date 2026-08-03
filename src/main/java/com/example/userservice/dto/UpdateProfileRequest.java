package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
   @NotBlank(message = "Username is required")
   @Size(min = 3, max = 50)
   private String username;

   @NotBlank(message = "Email is required")
   @Email(message = "Email should be valid")
   private String email;

   public UpdateProfileRequest() {
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }
}
