package com.vetcitas.gestion;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.*;
import com.vetcitas.gestion.repository.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import com.vetcitas.gestion.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Pruebas de integración: recorren HTTP, filtros de seguridad, controladores, servicios y base de
 * datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository usuarios;
  @Autowired ServicioRepository servicios;
  @Autowired ReservaRepository reservas;
  @Autowired UsuarioService usuarioService;
  @Autowired ReservaService reservaService;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
  Long adminId, clienteId, otroId, vetId, servicioId;

  // Cada prueba parte de registros nuevos, sin depender de resultados de pruebas anteriores.
  @BeforeEach
  void preparar() {
    reservas.deleteAll();
    servicios.deleteAll();
    usuarios.deleteAll();
    adminId = crearUsuario("Admin", "admin@test.com", Rol.ADMIN);
    clienteId = crearUsuario("Cliente", "cliente@test.com", Rol.CLIENTE);
    otroId = crearUsuario("Otro", "otro@test.com", Rol.CLIENTE);
    vetId = crearUsuario("Veterinario", "vet@test.com", Rol.VETERINARIO);
    var s = new Servicio();
    s.setNombre("Consulta general");
    s.setDescripcion("Evaluación veterinaria");
    s.setPrecio(new BigDecimal("60.00"));
    s.setDuracionMinutos(30);
    servicioId = servicios.save(s).getId();
  }

  Long crearUsuario(String nombre, String correo, Rol rol) {
    return usuarioService
        .crear(new UsuarioRequest(nombre, correo, "999111222", "Password123", rol, true))
        .id();
  }

  // Se obtiene un token real, sin saltarse el filtro CSRF con mocks de autenticación.
  MockHttpServletRequestBuilder segura(
      MockHttpServletRequestBuilder builder, MockHttpSession session) throws Exception {
    var token =
        json.readTree(
            mvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    return builder
        .session(session)
        .header(token.get("headerName").asText(), token.get("token").asText());
  }

  MockHttpSession login(String correo) throws Exception {
    var session = new MockHttpSession();
    mvc.perform(
            segura(post("/api/auth/login"), session)
                .contentType("application/json")
                .content(
                    json.writeValueAsString(Map.of("correo", correo, "password", "Password123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("password").doesNotExist());
    return session;
  }

  ResultActions enviar(String metodo, String url, Object body, MockHttpSession session)
      throws Exception {
    var builder =
        switch (metodo) {
          case "PUT" -> put(url);
          case "PATCH" -> patch(url);
          case "DELETE" -> delete(url);
          default -> post(url);
        };
    return mvc.perform(
        segura(builder, session)
            .contentType("application/json")
            .content(json.writeValueAsString(body)));
  }

  Map<String, Object> datosReserva(LocalDateTime fecha) {
    return Map.of(
        "usuarioId",
        clienteId,
        "veterinarioId",
        vetId,
        "servicioId",
        servicioId,
        "mascota",
        "Luna",
        "motivo",
        "Control",
        "fechaHora",
        fecha.toString());
  }

  LocalDateTime futuro() {
    return LocalDateTime.now(ZoneId.of("America/Lima"))
        .plusDays(2)
        .withHour(10)
        .withMinute(0)
        .withSecond(0)
        .withNano(0);
  }

  Long crearReserva(MockHttpSession session, LocalDateTime fecha) throws Exception {
    var resultado =
        enviar("POST", "/api/reservas", datosReserva(fecha), session)
            .andExpect(status().isCreated())
            .andReturn();
    return json.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
  }

  @Test
  void registroGuardaHashYFuerzaRolCliente() throws Exception {
    var session = new MockHttpSession();
    enviar(
            "POST",
            "/api/auth/registro",
            Map.of(
                "nombre",
                "Nuevo",
                "correo",
                "NUEVO@test.com",
                "telefono",
                "123456789",
                "password",
                "Password123",
                "rol",
                "ADMIN"),
            session)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("rol").value("CLIENTE"))
        .andExpect(jsonPath("password").doesNotExist());
    var u = usuarios.findByCorreoIgnoreCase("nuevo@test.com").orElseThrow();
    assertThat(u.getPassword()).startsWith("$2");
    assertThat(encoder.matches("Password123", u.getPassword())).isTrue();
    enviar(
            "POST",
            "/api/auth/registro",
            Map.of(
                "nombre",
                "Otro",
                "correo",
                "nuevo@test.com",
                "telefono",
                "123456789",
                "password",
                "Password123"),
            session)
        .andExpect(status().isConflict());
  }

  @Test
  void loginRotaSesionYLogoutRevocaAcceso() throws Exception {
    var session = new MockHttpSession();
    String anterior = session.getId();
    enviar(
            "POST",
            "/api/auth/login",
            Map.of("correo", "CLIENTE@test.com", "password", "Password123"),
            session)
        .andExpect(status().isOk());
    assertThat(session.getId()).isNotEqualTo(anterior);
    mvc.perform(get("/api/auth/sesion").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("id").value(clienteId));
    enviar("POST", "/api/auth/logout", Map.of(), session).andExpect(status().isOk());
    assertThat(session.isInvalid()).isTrue();
    mvc.perform(get("/api/reservas")).andExpect(status().isUnauthorized());
  }

  @Test
  void rechazaCredencialesYExigeCsrf() throws Exception {
    var session = new MockHttpSession();
    enviar(
            "POST",
            "/api/auth/login",
            Map.of("correo", "cliente@test.com", "password", "incorrecta"),
            session)
        .andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/servicios")
                .session(login("admin@test.com"))
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void rolesProtegenApiYPaginas() throws Exception {
    var cliente = login("cliente@test.com");
    mvc.perform(get("/api/usuarios").session(cliente)).andExpect(status().isForbidden());
    mvc.perform(get("/panel-admin.html").session(cliente)).andExpect(status().isForbidden());
    mvc.perform(get("/panel-admin.html")).andExpect(status().is3xxRedirection());
    enviar("POST", "/api/servicios", Map.of(), cliente).andExpect(status().isForbidden());
    mvc.perform(get("/api/servicios").session(cliente)).andExpect(status().isOk());
    mvc.perform(get("/v3/api-docs").session(cliente))
        .andExpect(status().isOk())
        .andExpect(jsonPath("paths['/api/reservas']").exists());
  }

  @Test
  void crudDeServiciosYBusquedaJpql() throws Exception {
    var admin = login("admin@test.com");
    var datos = new ServicioRequest("Vacunación", "Vacunas", new BigDecimal("45.50"), 30, true);
    var result =
        enviar("POST", "/api/servicios", datos, admin)
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn();
    long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/servicios?nombre=VACUN").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    enviar(
            "PUT",
            "/api/servicios/" + id,
            new ServicioRequest("Vacunación editada", "Cambio", new BigDecimal("50.00"), 45, false),
            admin)
        .andExpect(status().isOk());
    mvc.perform(get("/api/servicios/" + id).session(login("cliente@test.com")))
        .andExpect(status().isNotFound());
    enviar("DELETE", "/api/servicios/" + id, Map.of(), admin).andExpect(status().isNoContent());
    mvc.perform(get("/api/servicios/" + id).session(admin)).andExpect(status().isNotFound());
  }

  @Test
  void crudUsuariosYPerfilSinEscalarPermisos() throws Exception {
    var admin = login("admin@test.com");
    var datos =
        new UsuarioRequest(
            "Temporal", "temporal@test.com", "123456789", "Password123", Rol.CLIENTE, true);
    var result =
        enviar("POST", "/api/usuarios", datos, admin)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("password").doesNotExist())
            .andReturn();
    long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    enviar(
            "PUT",
            "/api/usuarios/" + id,
            new UsuarioRequest("Editado", "temporal@test.com", "987654321", "", Rol.CLIENTE, true),
            admin)
        .andExpect(status().isOk())
        .andExpect(jsonPath("nombre").value("Editado"));
    assertThat(encoder.matches("Password123", usuarios.findById(id).orElseThrow().getPassword()))
        .isTrue();
    var cliente = login("cliente@test.com");
    enviar(
            "PUT",
            "/api/usuarios/me",
            Map.of(
                "nombre",
                "Mi perfil",
                "correo",
                "cliente@test.com",
                "telefono",
                "123456789",
                "rol",
                "ADMIN"),
            cliente)
        .andExpect(status().isOk())
        .andExpect(jsonPath("rol").value("CLIENTE"));
    enviar("DELETE", "/api/usuarios/" + id, Map.of(), admin).andExpect(status().isNoContent());
    enviar("DELETE", "/api/usuarios/" + adminId, Map.of(), admin).andExpect(status().isConflict());
  }

  @Test
  void crudReservasYPrivacidadEntreClientes() throws Exception {
    var cliente = login("cliente@test.com");
    var otro = login("otro@test.com");
    Long id = crearReserva(cliente, futuro());
    mvc.perform(get("/api/reservas").session(otro)).andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/api/reservas/" + id).session(otro)).andExpect(status().isForbidden());
    enviar("PUT", "/api/reservas/" + id, datosReserva(futuro().plusHours(1)), otro)
        .andExpect(status().isForbidden());
    enviar("PUT", "/api/reservas/" + id, datosReserva(futuro().plusHours(1)), cliente)
        .andExpect(status().isOk());
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.CONFIRMADA),
            cliente)
        .andExpect(status().isForbidden());
    enviar("DELETE", "/api/reservas/" + id, Map.of(), cliente).andExpect(status().isConflict());
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.CANCELADA),
            cliente)
        .andExpect(status().isOk());
    enviar("DELETE", "/api/reservas/" + id, Map.of(), cliente).andExpect(status().isNoContent());
  }

  @Test
  void veterinarioSoloVeSusCitasYControlaEstados() throws Exception {
    var cliente = login("cliente@test.com");
    var vet = login("vet@test.com");
    Long id = crearReserva(cliente, futuro());
    mvc.perform(get("/api/reservas").session(vet)).andExpect(jsonPath("$.length()").value(1));
    enviar("POST", "/api/reservas", datosReserva(futuro().plusHours(2)), vet)
        .andExpect(status().isForbidden());
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.CONFIRMADA),
            vet)
        .andExpect(status().isOk());
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.ATENDIDA),
            vet)
        .andExpect(status().isConflict());
    // Simular una cita que ya comenzó permite comprobar el paso a ATENDIDA sin esperar días.
    var r = reservas.findById(id).orElseThrow();
    r.setFechaHora(futuro().minusDays(3));
    r.setFin(r.getFechaHora().plusMinutes(30));
    reservas.save(r);
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.ATENDIDA),
            vet)
        .andExpect(status().isOk());
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.CANCELADA),
            vet)
        .andExpect(status().isConflict());
    crearUsuario("Vet 2", "vet2@test.com", Rol.VETERINARIO);
    mvc.perform(get("/api/reservas/" + id).session(login("vet2@test.com")))
        .andExpect(status().isForbidden());
  }

  @Test
  void evitaCrucesYAceptaCitasConsecutivas() throws Exception {
    var cliente = login("cliente@test.com");
    Long id = crearReserva(cliente, futuro());
    enviar("POST", "/api/reservas", datosReserva(futuro().plusMinutes(15)), cliente)
        .andExpect(status().isConflict());
    crearReserva(cliente, futuro().plusMinutes(30));
    enviar(
            "PATCH",
            "/api/reservas/" + id + "/estado",
            new EstadoRequest(EstadoReserva.CANCELADA),
            cliente)
        .andExpect(status().isOk());
    crearReserva(cliente, futuro());
  }

  @Test
  void dosReservasSimultaneasNoDuplicanHorario() throws Exception {
    var actor = UsuarioPrincipal.de(usuarios.findById(clienteId).orElseThrow());
    var datos = new ReservaRequest(clienteId, vetId, servicioId, "Luna", "Control", futuro());
    var pool = Executors.newFixedThreadPool(2);
    var salida = new CountDownLatch(1);
    Callable<Boolean> tarea =
        () -> {
          salida.await();
          try {
            reservaService.crear(datos, actor);
            return true;
          } catch (com.vetcitas.gestion.exception.ReglaNegocioException e) {
            assertThat(e.getStatus().value()).isEqualTo(409);
            return false;
          }
        };
    try {
      var uno = pool.submit(tarea);
      var dos = pool.submit(tarea);
      salida.countDown();
      assertThat(List.of(uno.get(15, TimeUnit.SECONDS), dos.get(15, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(true, false);
      assertThat(reservas.count()).isEqualTo(1);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void relacionesImpidenBorradosYPrecioHistoricoSeConserva() throws Exception {
    var admin = login("admin@test.com");
    var cliente = login("cliente@test.com");
    Long id = crearReserva(cliente, futuro());
    enviar("DELETE", "/api/servicios/" + servicioId, Map.of(), admin)
        .andExpect(status().isConflict());
    enviar("DELETE", "/api/usuarios/" + clienteId, Map.of(), admin)
        .andExpect(status().isConflict());
    enviar(
            "PUT",
            "/api/servicios/" + servicioId,
            new ServicioRequest("Consulta", "Nuevo precio", new BigDecimal("90.00"), 60, true),
            admin)
        .andExpect(status().isOk());
    enviar("PUT", "/api/reservas/" + id, datosReserva(futuro().plusHours(2)), cliente)
        .andExpect(status().isOk())
        .andExpect(jsonPath("precio").value(60.0));
    var r = reservas.findById(id).orElseThrow();
    assertThat(Duration.between(r.getFechaHora(), r.getFin()).toMinutes()).isEqualTo(30);
  }

  @Test
  void validacionFechasIdsYDatosIncorrectos() throws Exception {
    var admin = login("admin@test.com");
    var cliente = login("cliente@test.com");
    enviar(
            "POST",
            "/api/servicios",
            Map.of(
                "nombre",
                "",
                "descripcion",
                "",
                "precio",
                -1,
                "duracionMinutos",
                1,
                "activo",
                true),
            admin)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errores.nombre").exists());
    enviar("POST", "/api/reservas", datosReserva(futuro().minusDays(4)), cliente)
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/reservas/abc").session(cliente)).andExpect(status().isBadRequest());
    mvc.perform(get("/api/servicios/9999999").session(cliente)).andExpect(status().isNotFound());
    var falsificada = new HashMap<>(datosReserva(futuro()));
    falsificada.put("usuarioId", otroId);
    enviar("POST", "/api/reservas", falsificada, cliente).andExpect(status().isForbidden());
  }

  @Test
  void desactivarOReasignarRolAfectaSesionExistente() throws Exception {
    var admin = login("admin@test.com");
    var cliente = login("cliente@test.com");
    enviar(
            "PUT",
            "/api/usuarios/" + clienteId,
            new UsuarioRequest("Cliente", "cliente@test.com", "999111222", "", Rol.CLIENTE, false),
            admin)
        .andExpect(status().isOk());
    mvc.perform(get("/api/reservas").session(cliente)).andExpect(status().isUnauthorized());
    var vet = login("vet@test.com");
    enviar(
            "PUT",
            "/api/usuarios/" + vetId,
            new UsuarioRequest("Veterinario", "vet@test.com", "999111222", "", Rol.CLIENTE, true),
            admin)
        .andExpect(status().isOk());
    mvc.perform(get("/panel-veterinario.html").session(vet)).andExpect(status().isForbidden());
    mvc.perform(get("/api/auth/sesion").session(vet)).andExpect(jsonPath("rol").value("CLIENTE"));
  }

  @Test
  void cambiarPasswordRevocaSesionYEvitaLimiteUtf8DeBcrypt() throws Exception {
    var admin = login("admin@test.com");
    var cliente = login("cliente@test.com");
    enviar(
            "PUT",
            "/api/usuarios/" + clienteId,
            new UsuarioRequest(
                "Cliente", "cliente@test.com", "999111222", "OtraPassword123", Rol.CLIENTE, true),
            admin)
        .andExpect(status().isOk());
    mvc.perform(get("/api/reservas").session(cliente)).andExpect(status().isUnauthorized());
    enviar(
            "POST",
            "/api/auth/registro",
            new RegistroRequest("Nuevo", "nuevo@test.com", "999111222", "é".repeat(40)),
            new MockHttpSession())
        .andExpect(status().isBadRequest());
  }
}
