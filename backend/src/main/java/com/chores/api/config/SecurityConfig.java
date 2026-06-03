package com.chores.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {

    try {
      // disable if in local dev mode
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .csrf(AbstractHttpConfigurer::disable);
      return http.build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to configure security filter chain", e);
    }
  }
}
