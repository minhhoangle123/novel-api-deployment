package com.crawldata.back_end.configurations;

import org.springframework.boot.web.server.WebServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig  {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected UserDetailsService userDetailsService() {
        UserDetails user1 = User
                .withUsername("admin")
                .password("$2a$10$se94lJO5AJnP/xZwyqXVnOjOGCsx6n5sLsMf0pRfCgboe8aEwiaQS")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1);
    }
    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/plugin/novel/upload", "/plugin/export/upload"))
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/plugin/novel", "/plugin/export").authenticated()
                                .anyRequest().permitAll()
                )
                .formLogin(
                        login -> login.loginPage("/login")
                                .usernameParameter("u").passwordParameter("p")
                                .permitAll()
                )
                .logout(logout -> logout.permitAll());
        return http.build();
    }
}
