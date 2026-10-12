package com.vetcitas.gestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Configuration  Le indica a Spring que esta clase contiene
 * definiciones de @Bean, es decir, objetos que Spring debe crear
 * y gestionar dentro de su contenedor de Inversión de Control (IoC),
 * para luego poder inyectarlos (@Autowired / inyección por constructor)
 * en cualquier otra clase que los necesite (como UsuarioService).
 *
 * @EnableWebSecurity  Habilita explícitamente la configuración web de
 * Spring Security y nos permite definir nuestra propia SecurityFilterChain,
 * en vez de dejar que Spring Boot aplique su configuración automática
 * por defecto (que es la que generaba el formulario "Please sign in" con
 * usuario "user" y una contraseña aleatoria impresa en consola).
 *

 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @Bean -> Método "fábrica": todo lo que retorna queda registrado
     * en el contenedor de Spring. A partir de aquí, cualquier clase
     * puede pedir un PasswordEncoder por inyección de dependencias y
     * Spring le entregará ESTA misma instancia (BCrypt).
     *
     * BCrypt es un algoritmo de hash diseñado específicamente para
     * contraseñas: es "lento" a propósito (para dificultar ataques de
     * fuerza bruta) y genera un "salt" aleatorio distinto en cada
     * ejecución, por lo que dos usuarios con la misma contraseña
     * tendrán hashes completamente diferentes en la base de datos.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            
            .csrf(csrf -> csrf.disable())

           
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            
            .formLogin(form -> form.disable())

         
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
