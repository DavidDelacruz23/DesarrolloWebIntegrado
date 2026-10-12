package com.vetcitas.gestion.config;

import com.vetcitas.gestion.repository.UsuarioRepository;
import com.vetcitas.gestion.security.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.context.*;
import org.springframework.security.web.csrf.*;

/**
 * La seguridad del servidor sigue funcionando aunque alguien modifique el JavaScript del navegador.
 */
@Configuration
public class SecurityConfig {
  // @Bean registra un objeto que Spring podrá inyectar por constructor donde haga falta.
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityContextRepository contextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  CsrfTokenRepository csrfRepository() {
    return new HttpSessionCsrfTokenRepository();
  }

  @Bean
  UserDetailsService userDetailsService(UsuarioRepository usuarios) {
    return correo ->
        UsuarioPrincipal.de(
            usuarios
                .findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas")));
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
    // Spring compara la contraseña enviada con el hash BCrypt guardado en MySQL.
    var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      UsuarioRepository usuarios,
      SecurityContextRepository contexts,
      CsrfTokenRepository csrf)
      throws Exception {
    http.securityContext(c -> c.securityContextRepository(contexts))
        // Cada POST/PUT/PATCH/DELETE exige un token CSRF asociado a la sesión.
        .csrf(c -> c.csrfTokenRepository(csrf))
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable())
        .logout(l -> l.disable())
        .requestCache(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/registro.html",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/error",
                        "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/api/auth/csrf", "/api/auth/login", "/api/auth/registro")
                    .permitAll()
                    .requestMatchers("/panel-admin.html", "/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/panel-veterinario.html")
                    .hasRole("VETERINARIO")
                    .requestMatchers("/panel-cliente.html")
                    .hasRole("CLIENTE")
                    .requestMatchers("/api/usuarios/me")
                    .authenticated()
                    .requestMatchers("/api/usuarios/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/servicios/**", "/api/veterinarios")
                    .authenticated()
                    .requestMatchers("/api/servicios/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          if (!req.getRequestURI().startsWith("/api/")
                              && req.getRequestURI().endsWith(".html")) {
                            res.sendRedirect("/login.html");
                            return;
                          }
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          res.getWriter().write("{\"mensaje\":\"Inicia sesión para continuar\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/json;charset=UTF-8");
                          res.getWriter()
                              .write(
                                  "{\"mensaje\":\"Acceso denegado o token CSRF inválido. Actualiza"
                                      + " la página\"}");
                        }))
        .addFilterAfter(new ActualizarSesionFilter(usuarios), SecurityContextHolderFilter.class);
    return http.build();
  }
}
