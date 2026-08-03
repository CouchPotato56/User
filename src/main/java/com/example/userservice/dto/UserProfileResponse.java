package com.example.userservice.dto;

public class UserProfileResponse {
   private Long id;
   private String username;
   private String email;
   private boolean active;

   public UserProfileResponse() {
   }

   public UserProfileResponse(Long id, String username, String email, boolean active) {
      this.id = id;
      this.username = username;
      this.email = email;
      this.active = active;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
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

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }
}
