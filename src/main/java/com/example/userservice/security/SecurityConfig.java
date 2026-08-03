package com.example.userservice.security;

import com.example.userservice.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
   private final JwtTokenProvider jwtTokenProvider;
   private final CustomUserDetailsService userDetailsService;
   private final JwtAuthenticationFilter jwtAuthenticationFilter;
   private final JwtAuthenticationEntryPoint authenticationEntryPoint;

   public SecurityConfig(JwtTokenProvider jwtTokenProvider,
         CustomUserDetailsService userDetailsService,
         JwtAuthenticationFilter jwtAuthenticationFilter,
         JwtAuthenticationEntryPoint authenticationEntryPoint) {
      this.jwtTokenProvider = jwtTokenProvider;
      this.userDetailsService = userDetailsService;
      this.jwtAuthenticationFilter = jwtAuthenticationFilter;
      this.authenticationEntryPoint = authenticationEntryPoint;
   }

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
            .csrf().disable()
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authorize -> authorize
                  .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh-token",
                        "/api/auth/forgot-password", "/api/auth/reset-password")
                  .permitAll()
                  .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                  .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                  .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN")
                  .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic().disable();

      return http.build();
   }

   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
      return configuration.getAuthenticationManager();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }
}
