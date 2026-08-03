package com.example.userservice.exception;

import java.time.Instant;
import java.util.List;

public class ApiErrorResponse {
   private Instant timestamp;
   private int status;
   private String error;
   private List<String> messages;
   private String path;

   public ApiErrorResponse() {
      this.timestamp = Instant.now();
   }

   public ApiErrorResponse(int status, String error, List<String> messages, String path) {
      this.timestamp = Instant.now();
      this.status = status;
      this.error = error;
      this.messages = messages;
      this.path = path;
   }

   public Instant getTimestamp() {
      return timestamp;
   }

   public int getStatus() {
      return status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

   public List<String> getMessages() {
      return messages;
   }

   public void setMessages(List<String> messages) {
      this.messages = messages;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }
}
