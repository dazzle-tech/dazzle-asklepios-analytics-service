package com.dazzle.asklepios.config;

import com.dazzle.asklepios.security.AuthoritiesConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final String contentSecurityPolicy =
            "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/setup/api/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/admin/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/**")).authenticated()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/appointment/**")).authenticated()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/v3/api-docs/**")).authenticated()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/management/health")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/management/health/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/management/info")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/management/prometheus")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.pathPattern("/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));

        return http.build();
    }
}