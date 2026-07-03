package com.substring.foodies.security;


import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
// We can authorize our methods individually with this annotation.
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
//                .cors(Customizer.withDefaults())
                .csrf(e->e.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request->
                        request
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/favicon.ico",
                                        "/actuator/health"
                                ).permitAll()

                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/user/change-credential/**").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/food/**", "/api/restaurant/**").hasAnyRole("ADMIN", "RESTAURANT_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/food/**", "/api/restaurant/**").hasAnyRole("ADMIN", "RESTAURANT_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/food/**", "/api/restaurant/**").hasAnyRole("ADMIN", "RESTAURANT_ADMIN")
                                .requestMatchers("/api/food-categories").hasAnyRole("ADMIN", "RESTAURANT_ADMIN")
                                .requestMatchers("/api/food-subcategories").hasAnyRole("ADMIN", "RESTAURANT_ADMIN")
                                .requestMatchers("/api/addresses/admin**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                );

        http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exception-> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Access Denied\"}");
                }));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 🔥 CORS CONFIGURATION (Fixes your issue completely)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));   // allows null origin from file://
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
//  This @Bean method is returning an AuthenticationManager object.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception
    {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

}
