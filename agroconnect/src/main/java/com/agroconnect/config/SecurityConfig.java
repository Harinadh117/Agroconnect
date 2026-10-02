package com.agroconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/index.html", "/", "/css/**", "/js/**").permitAll()

                // FARMER
                .requestMatchers(HttpMethod.POST, "/api/farm/jobs").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.POST, "/api/farm/land").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.PUT, "/api/farm/applications/*/select").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.PUT, "/api/farm/applications/*/reject").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.PUT, "/api/farm/land-requests/*/approve").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.PUT, "/api/farm/land-requests/*/reject").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.GET, "/api/farm/my-jobs").hasAuthority("FARMER")
                .requestMatchers(HttpMethod.GET, "/api/farm/my-land").hasAuthority("FARMER")

                // WORKER
                .requestMatchers(HttpMethod.POST, "/api/farm/jobs/*/apply").hasAuthority("WORKER")
                .requestMatchers(HttpMethod.PUT, "/api/farm/worker/availability").hasAuthority("WORKER")

                // KAVULU
                .requestMatchers(HttpMethod.POST, "/api/farm/land/*/request").hasAuthority("KAVULU")

                // ADMIN
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
            );

        return http.build();
    }
}
