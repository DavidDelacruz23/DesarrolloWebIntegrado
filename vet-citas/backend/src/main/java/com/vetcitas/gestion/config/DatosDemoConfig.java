package com.vetcitas.gestion.config;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.Rol;
import com.vetcitas.gestion.repository.*;
import com.vetcitas.gestion.service.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

/** Solo el perfil demo crea datos de práctica; el inicio normal no agrega cuentas conocidas. */
@Configuration
@Profile("demo")
public class DatosDemoConfig {
  @Bean
  CommandLineRunner datosDemo(
      UsuarioService usuarios,
      UsuarioRepository repo,
      ServicioService servicios,
      ServicioRepository catalogo) {
    return args -> {
      // La misma contraseña de demostración se convierte en un hash BCrypt real para cada cuenta.
      if (repo.findByCorreoIgnoreCase("admin@vetcitas.com").isEmpty())
        usuarios.crear(
            new UsuarioRequest(
                "Ana Torres", "admin@vetcitas.com", "999111222", "Password123", Rol.ADMIN, true));
      if (repo.findByCorreoIgnoreCase("veterinario@vetcitas.com").isEmpty())
        usuarios.crear(
            new UsuarioRequest(
                "Carlos Ramírez",
                "veterinario@vetcitas.com",
                "999333444",
                "Password123",
                Rol.VETERINARIO,
                true));
      if (repo.findByCorreoIgnoreCase("cliente@vetcitas.com").isEmpty())
        usuarios.crear(
            new UsuarioRequest(
                "Lucía Gómez",
                "cliente@vetcitas.com",
                "999555666",
                "Password123",
                Rol.CLIENTE,
                true));
      if (catalogo.count() == 0) {
        servicios.crear(
            new ServicioRequest(
                "Consulta general",
                "Evaluación clínica y orientación para el cuidado de tu mascota.",
                new BigDecimal("60.00"),
                30,
                true));
        servicios.crear(
            new ServicioRequest(
                "Vacunación",
                "Revisión del esquema de vacunas y aplicación según evaluación.",
                new BigDecimal("45.00"),
                30,
                true));
        servicios.crear(
            new ServicioRequest(
                "Control preventivo",
                "Seguimiento del bienestar y revisión periódica de tu mascota.",
                new BigDecimal("50.00"),
                45,
                true));
      }
    };
  }
}
