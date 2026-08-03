package com.example.userservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, unique = true)
   private String username;

   @Column(nullable = false, unique = true)
   private String email;

   @Column(nullable = false)
   private String password;

   @ElementCollection(fetch = FetchType.EAGER)
   @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
   @Enumerated(EnumType.STRING)
   @Column(name = "role")
   private Set<Role> roles = new HashSet<>();

   @Column(nullable = false)
   private boolean active = true;

   @Column(nullable = false)
   private boolean deleted = false;

   @Column(nullable = false, updatable = false)
   private Instant createdAt = Instant.now();

   @Column(nullable = false)
   private Instant updatedAt = Instant.now();

   @Column
   private Instant deletedAt;

   public User() {
   }

   @PreUpdate
   public void preUpdate() {
      this.updatedAt = Instant.now();
   }

   // Getters and setters

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

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public Set<Role> getRoles() {
      return roles;
   }

   public void setRoles(Set<Role> roles) {
      this.roles = roles;
   }

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public boolean isDeleted() {
      return deleted;
   }

   public void setDeleted(boolean deleted) {
      this.deleted = deleted;
   }

   public Instant getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(Instant createdAt) {
      this.createdAt = createdAt;
   }

   public Instant getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(Instant updatedAt) {
      this.updatedAt = updatedAt;
   }

   public Instant getDeletedAt() {
      return deletedAt;
   }

   public void setDeletedAt(Instant deletedAt) {
      this.deletedAt = deletedAt;
   }
}
