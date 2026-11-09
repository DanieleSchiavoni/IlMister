package com.ilmister.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmister.security.FiltroJwt;
import com.ilmister.security.GestoreErroriSicurezza;
import com.ilmister.security.JwtService;

/**
 * La sicurezza dell'API: senza sessioni (ogni richiesta porta il suo token JWT), password con BCrypt.
 * Le rotte di login e registrazione sono libere, quelle di amministrazione solo per gli ADMIN, tutto il resto
 * richiede un utente autenticato. La proprietà dei dati (ogni utente vede solo le sue carriere) la
 * controllano i servizi.
 */
@Configuration
@EnableWebSecurity
public class ConfigurazioneSicurezza {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain catenaDiSicurezza(HttpSecurity http, JwtService jwtService, ObjectMapper objectMapper)
            throws Exception {
        GestoreErroriSicurezza gestoreErrori = new GestoreErroriSicurezza(objectMapper);
        http
                // Nessun cookie di sessione: il CSRF non ha nulla da proteggere
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessioni -> sessioni.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(rotte -> rotte
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/ping").permitAll()
                        .requestMatchers("/api/sviluppo/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errori -> errori
                        .authenticationEntryPoint(gestoreErrori)
                        .accessDeniedHandler(gestoreErrori))
                // Non è un @Component: altrimenti Spring Boot lo registrerebbe due volte
                .addFilterBefore(new FiltroJwt(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origini-consentite}") String origini) {
        CorsConfiguration configurazione = new CorsConfiguration();
        configurazione.setAllowedOrigins(Arrays.stream(origini.split(",")).map(String::trim)
                .filter(origine -> !origine.isEmpty()).toList());
        configurazione.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configurazione.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configurazione.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource sorgente = new UrlBasedCorsConfigurationSource();
        sorgente.registerCorsConfiguration("/api/**", configurazione);
        return sorgente;
    }
}
