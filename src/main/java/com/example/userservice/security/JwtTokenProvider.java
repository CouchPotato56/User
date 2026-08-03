package com.example.userservice.security;

import com.example.userservice.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
   private final JwtProperties jwtProperties;
   private final Key key;

   public JwtTokenProvider(JwtProperties jwtProperties) {
      this.jwtProperties = jwtProperties;
      this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
   }

   public String generateToken(Authentication authentication) {
      UserDetails principal = (UserDetails) authentication.getPrincipal();
      var authorities = principal.getAuthorities().stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
      var now = new Date();
      var expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMs());

      return Jwts.builder()
            .setSubject(principal.getUsername())
            .claim("roles", authorities)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
   }

   public String getUsernameFromToken(String token) {
      return getClaims(token).getSubject();
   }

   public boolean validateToken(String token) {
      try {
         getClaims(token);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   private Claims getClaims(String token) {
      return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
   }
}
