package com.rishi.student_management.config;

import com.rishi.student_management.exception.CustomAccessDeniedHandler;
import com.rishi.student_management.exception.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import com.rishi.student_management.filter.JwtAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity // Enable method-level security annotations like @PreAuthorize and @PostAuthorize
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**","/",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    // Role - Based Authorization
//                .authorizeHttpRequests(auth -> auth
//
//                        .requestMatchers("/error").permitAll()
//
//                        // GET -> ADMIN & USER
//                        .requestMatchers(HttpMethod.GET,
//                                "/studentmgmt/**")
//                        .hasAnyRole("ADMIN","USER")
//
//                        // POST -> ADMIN
//                        .requestMatchers(HttpMethod.POST,
//                                "/studentmgmt")
//                        .hasRole("ADMIN")
//
//                        // PUT -> ADMIN
//                        .requestMatchers(HttpMethod.PUT,
//                                "/studentmgmt/**")
//                        .hasRole("ADMIN")
//
//                        // DELETE -> ADMIN
//                        .requestMatchers(HttpMethod.DELETE,
//                                "/studentmgmt/**")
//                        .hasRole("ADMIN")
//
//                        .anyRequest().authenticated()
//                )


    //In Memory User Authentication
//    @Bean
//    public UserDetailsService userDetailsService() {
//
//        UserDetails admin =
//                User.builder()
//                        .username("admin")
//                        .password(passwordEncoder().encode("admin123"))
//                        .roles("ADMIN")
//                        .build();
//
//        return new InMemoryUserDetailsManager(admin);
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();

    }

}
