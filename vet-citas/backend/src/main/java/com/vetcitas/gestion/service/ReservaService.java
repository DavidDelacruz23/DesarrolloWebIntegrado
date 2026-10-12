package com.vetcitas.gestion.service;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.*;
import com.vetcitas.gestion.exception.ReglaNegocioException;
import com.vetcitas.gestion.repository.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de citas: propietario, profesional, disponibilidad, fechas y cambios de estado. */
@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class ReservaService {
  private final ReservaRepository reservas;
  private final UsuarioRepository usuarios;
  private final ServicioRepository servicios;

  public ReservaService(
      ReservaRepository reservas, UsuarioRepository usuarios, ServicioRepository servicios) {
    this.reservas = reservas;
    this.usuarios = usuarios;
    this.servicios = servicios;
  }

  @Transactional(readOnly = true)
  public List<ReservaResponse> listar(UsuarioPrincipal actor, EstadoReserva estado) {
    return reservas
        .listarVisibles(
            actor.rol() == Rol.ADMIN,
            actor.rol() == Rol.CLIENTE ? actor.id() : 0L,
            actor.rol() == Rol.VETERINARIO ? actor.id() : 0L,
            estado)
        .stream()
        .map(this::dto)
        .toList();
  }

  @Transactional(readOnly = true)
  public ReservaResponse obtener(Long id, UsuarioPrincipal actor) {
    var r = buscar(id);
    comprobarAcceso(r, actor);
    return dto(r);
  }

  public ReservaResponse crear(ReservaRequest request, UsuarioPrincipal actor) {
    if (actor.rol() == Rol.VETERINARIO)
      throw new AccessDeniedException("Solo clientes y administradores reservan citas");
    var r = new Reserva();
    aplicar(r, request, actor);
    return dto(reservas.save(r));
  }

  public ReservaResponse actualizar(Long id, ReservaRequest request, UsuarioPrincipal actor) {
    var r = buscar(id);
    comprobarAcceso(r, actor);
    if (actor.rol() == Rol.VETERINARIO)
      throw new AccessDeniedException("El veterinario solo cambia el estado de sus citas");
    comprobarEditable(r);
    aplicar(r, request, actor);
    r.setEstado(EstadoReserva.PENDIENTE);
    return dto(reservas.save(r));
  }

  public ReservaResponse cambiarEstado(Long id, EstadoReserva siguiente, UsuarioPrincipal actor) {
    var r = buscar(id);
    comprobarAcceso(r, actor);
    if (actor.rol() == Rol.CLIENTE && siguiente != EstadoReserva.CANCELADA)
      throw new AccessDeniedException("El cliente solo puede cancelar");
    if (r.getEstado() == siguiente)
      return dto(r); // Repetir el mismo cambio no produce efectos adicionales.
    if (r.getEstado() == EstadoReserva.ATENDIDA || r.getEstado() == EstadoReserva.CANCELADA)
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "Una cita finalizada no puede reabrirse");
    if (actor.rol() == Rol.CLIENTE && !r.getFechaHora().isAfter(ahora()))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "La cita ya comenzó; contacta con la clínica");
    boolean permitido =
        siguiente == EstadoReserva.CANCELADA
            || (r.getEstado() == EstadoReserva.PENDIENTE && siguiente == EstadoReserva.CONFIRMADA)
            || (r.getEstado() == EstadoReserva.CONFIRMADA && siguiente == EstadoReserva.ATENDIDA);
    if (!permitido)
      throw new ReglaNegocioException(HttpStatus.CONFLICT, "Cambio de estado no permitido");
    if (siguiente == EstadoReserva.ATENDIDA && r.getFechaHora().isAfter(ahora()))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "No se puede atender una cita antes de su hora de inicio");
    r.setEstado(siguiente);
    return dto(reservas.save(r));
  }

  public void eliminar(Long id, UsuarioPrincipal actor) {
    var r = buscar(id);
    comprobarAcceso(r, actor);
    if (actor.rol() == Rol.VETERINARIO)
      throw new AccessDeniedException("El veterinario no elimina citas");
    if (actor.rol() == Rol.CLIENTE && r.getEstado() != EstadoReserva.CANCELADA)
      throw new ReglaNegocioException(HttpStatus.CONFLICT, "Cancela la cita antes de eliminarla");
    reservas.delete(r);
  }

  private void aplicar(Reserva r, ReservaRequest q, UsuarioPrincipal actor) {
    // El cliente solo puede reservar para sí mismo, incluso si modifica usuarioId en el JSON.
    Long clienteId = actor.rol() == Rol.CLIENTE ? actor.id() : q.usuarioId();
    if (actor.rol() == Rol.CLIENTE && q.usuarioId() != null && !q.usuarioId().equals(actor.id()))
      throw new AccessDeniedException("Cliente incorrecto");
    if (clienteId == null)
      throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Selecciona un cliente");
    var cliente =
        usuarios
            .findById(clienteId)
            .orElseThrow(
                () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    // Este bloqueo y la comprobación de cruces están en la MISMA transacción.
    var vet =
        usuarios
            .bloquearPorId(q.veterinarioId())
            .orElseThrow(
                () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));
    var servicio =
        servicios
            .findById(q.servicioId())
            .orElseThrow(
                () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    if (cliente.getRol() != Rol.CLIENTE
        || !cliente.isActivo()
        || vet.getRol() != Rol.VETERINARIO
        || !vet.isActivo()
        || !servicio.isActivo())
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "El cliente, veterinario o servicio no está disponible");
    if (!q.fechaHora().isAfter(ahora()))
      throw new ReglaNegocioException(
          HttpStatus.BAD_REQUEST, "La cita debe ser futura (hora de Lima)");
    // Si no se cambia el servicio, reprogramar conserva el precio y la duración acordados.
    boolean mismoServicio = r.getId() != null && r.getServicio().getId().equals(servicio.getId());
    long minutos =
        mismoServicio
            ? Duration.between(r.getFechaHora(), r.getFin()).toMinutes()
            : servicio.getDuracionMinutos();
    LocalDateTime fin = q.fechaHora().plusMinutes(minutos);
    if (reservas.contarCruces(
            vet.getId(),
            r.getId() == null ? 0L : r.getId(),
            q.fechaHora(),
            fin,
            List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        > 0)
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "El veterinario ya tiene una cita en ese horario");
    r.setUsuario(cliente);
    r.setVeterinario(vet);
    r.setServicio(servicio);
    r.setMascota(q.mascota().trim());
    r.setMotivo(q.motivo().trim());
    r.setFechaHora(q.fechaHora());
    r.setFin(fin);
    if (!mismoServicio) r.setPrecio(servicio.getPrecio());
  }

  private void comprobarAcceso(Reserva r, UsuarioPrincipal actor) {
    boolean permitido =
        actor.rol() == Rol.ADMIN
            || (actor.rol() == Rol.CLIENTE && r.getUsuario().getId().equals(actor.id()))
            || (actor.rol() == Rol.VETERINARIO && r.getVeterinario().getId().equals(actor.id()));
    if (!permitido) throw new AccessDeniedException("Esta reserva no te pertenece");
  }

  private void comprobarEditable(Reserva r) {
    if (r.getEstado() == EstadoReserva.CANCELADA
        || r.getEstado() == EstadoReserva.ATENDIDA
        || !r.getFechaHora().isAfter(ahora()))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "Solo puedes reprogramar citas futuras pendientes o confirmadas");
  }

  private LocalDateTime ahora() {
    return LocalDateTime.now(ZoneId.of("America/Lima"));
  }

  private Reserva buscar(Long id) {
    return reservas
        .findById(id)
        .orElseThrow(
            () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
  }

  private ReservaResponse dto(Reserva r) {
    // El mapeo se hace dentro de la transacción para cargar las relaciones LAZY de forma segura.
    return new ReservaResponse(
        r.getId(),
        r.getUsuario().getId(),
        r.getUsuario().getNombre(),
        r.getVeterinario().getId(),
        r.getVeterinario().getNombre(),
        r.getServicio().getId(),
        r.getServicio().getNombre(),
        r.getMascota(),
        r.getMotivo(),
        r.getFechaHora(),
        r.getFin(),
        r.getPrecio(),
        r.getEstado());
  }
}
