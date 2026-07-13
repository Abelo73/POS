package com.novapos.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novapos.shared.security.JwtAuthenticationFilter;
import com.novapos.shared.security.JwtTokenProvider;
import com.novapos.shared.security.NovaPosPermissionEvaluator;
import com.novapos.shared.web.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new NovaPosPermissionEvaluator();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator evaluator) {
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider,
                                           ObjectMapper mapper) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            var error = new ErrorResponse("UNAUTHORIZED", "Authentication required.", Map.of(),
                                    UUID.randomUUID().toString().substring(0, 8), Instant.now());
                            mapper.writeValue(res.getOutputStream(), error);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            var error = new ErrorResponse("FORBIDDEN", "Access denied.", Map.of(),
                                    UUID.randomUUID().toString().substring(0, 8), Instant.now());
                            mapper.writeValue(res.getOutputStream(), error);
                        }));

        return http.build();
    }
}
