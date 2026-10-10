
package com.hotel.hms.Config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hotel.hms.Service.JwtAuthenticationFilter;
import com.hotel.hms.Service.JwtService;

@Configuration
public class SecurityConfig {

    // Store the security context for the current request
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }

    // JWT authentication filter
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    // Prevent duplicate servlet registration of the JWT filter
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
            jwtAuthenticationFilterRegistration(
                    JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }

    // CORS configuration for the React frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Main Spring Security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http
            // Enable CORS
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            // Disable CSRF for the stateless REST API
            .csrf(csrf -> csrf.disable())

            // Use JWT without HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // Configure security context storage
            .securityContext(securityContext ->
                securityContext.securityContextRepository(
                    securityContextRepository
                )
            )

            // Return 401 when authentication is missing
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(
                        HttpStatus.UNAUTHORIZED
                    )
                )
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Allow preflight requests
                .requestMatchers(
                    HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Public login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/users/login"
                ).permitAll()

                // Public registration
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/users/register"
                ).permitAll()

                // Public room listing
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/rooms"
                ).permitAll()

                // Public room details
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/rooms/*"
                ).permitAll()

                // Only ADMIN can create rooms
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/rooms"
                ).hasRole("ADMIN")

                // Only ADMIN can update rooms
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/rooms/**"
                ).hasRole("ADMIN")

                // Only ADMIN can delete rooms
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/rooms/**"
                ).hasRole("ADMIN")

                // Only ADMIN can access staff endpoints
                .requestMatchers(
                    "/api/staff/**"
                ).hasRole("ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Run JWT authentication before username/password auth
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
