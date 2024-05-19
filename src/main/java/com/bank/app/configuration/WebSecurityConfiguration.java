package com.bank.app.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {
    // private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfiguration.class);

    // @Autowired
    // private JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        // http = http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http = http.csrf(csrf -> csrf.disable());

        // Set session management to stateless
        http = http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Set unauthorized requests exception handler
        // http = http.exceptionHandling(handling -> handling.authenticationEntryPoint((request, response, ex) -> {
        //     // logger.error("Unauthorized request - {}", ex.getMessage());
        //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        // }));

        // Set permissions on endpoints
        // http.authorizeHttpRequests((requests) -> requests
        //         .requestMatchers("/api/auth/**").permitAll()
        //         .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        //         // .requestMatchers(String.format("%s/**", restApiDocPath)).permitAll()
        //         // .requestMatchers(String.format("%s/**", swaggerPath)).permitAll()
        //         .requestMatchers("/uploads/**").permitAll()
        //         .anyRequest().authenticated());

        // Add JWT token filter
        // http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
