package com.seguranca.redes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite tudo
            )
            .csrf(csrf -> csrf.disable()) // Desativa proteção CSRF (somente se necessário)
            .formLogin(login -> login.disable()) // Desativa formulário de login
            .httpBasic(basic -> basic.disable()); // Desativa autenticação básica

        return http.build();
    }
}
