package com.training.demo_train_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers("/actuator/health").permitAll()

                        // Anyone having USER or ADMIN role can view trains
                        .requestMatchers(HttpMethod.GET, "/trains/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Only ADMIN can create trains
                        .requestMatchers(HttpMethod.POST, "/trains/**")
                        .hasRole("ADMIN")

                        // Only ADMIN can update trains
                        .requestMatchers(HttpMethod.PUT, "/trains/**")
                        .hasRole("ADMIN")

                        // Only ADMIN can delete trains
                        .requestMatchers(HttpMethod.DELETE, "/trains/**")
                        .hasRole("ADMIN")

                        // Any other request must be authenticated
                        .anyRequest()
                        .authenticated()
                )

                // Authentication Type
                .httpBasic(Customizer.withDefaults())

                // Disable HTML Login Page
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("vivek")
                .password("{noop}password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}