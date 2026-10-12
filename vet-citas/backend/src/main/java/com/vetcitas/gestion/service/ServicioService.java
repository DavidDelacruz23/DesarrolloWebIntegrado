package com.vetcitas.gestion.service;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.*;
import com.vetcitas.gestion.exception.ReglaNegocioException;
import com.vetcitas.gestion.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD del catálogo. Una transacción confirma todos sus cambios juntos o los deshace si hay error.
 */
@Service
@Transactional
public class ServicioService {
  private final ServicioRepository servicios;
  private final ReservaRepository reservas;

  public ServicioService(ServicioRepository servicios, ReservaRepository reservas) {
    this.servicios = servicios;
    this.reservas = reservas;
  }

  @Transactional(readOnly = true)
  public List<ServicioResponse> listar(String nombre, boolean todos) {
    return servicios.buscarPorNombre(nombre, todos).stream().map(this::dto).toList();
  }

  @Transactional(readOnly = true)
  public ServicioResponse obtener(Long id, boolean admin) {
    var s = buscar(id);
    if (!s.isActivo() && !admin)
      throw new ReglaNegocioException(HttpStatus.NOT_FOUND, "Servicio no disponible");
    return dto(s);
  }

  public ServicioResponse crear(ServicioRequest r) {
    var s = new Servicio();
    aplicar(s, r);
    return dto(servicios.save(s));
  }

  public ServicioResponse actualizar(Long id, ServicioRequest r) {
    var s = buscar(id);
    aplicar(s, r);
    return dto(servicios.save(s));
  }

  public void eliminar(Long id) {
    var s = buscar(id);
    if (reservas.existsByServicioId(id))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT,
          "El servicio tiene reservas. Desactívalo para conservar el historial");
    servicios.delete(s);
  }

  // Los métodos privados reutilizan mapeos y búsquedas sin repetirlos en cada operación.
  private Servicio buscar(Long id) {
    return servicios
        .findById(id)
        .orElseThrow(
            () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
  }

  private void aplicar(Servicio s, ServicioRequest r) {
    s.setNombre(r.nombre().trim());
    s.setDescripcion(r.descripcion().trim());
    s.setPrecio(r.precio());
    s.setDuracionMinutos(r.duracionMinutos());
    s.setActivo(r.activo());
  }

  private ServicioResponse dto(Servicio s) {
    return new ServicioResponse(
        s.getId(),
        s.getNombre(),
        s.getDescripcion(),
        s.getPrecio(),
        s.getDuracionMinutos(),
        s.isActivo());
  }
}
