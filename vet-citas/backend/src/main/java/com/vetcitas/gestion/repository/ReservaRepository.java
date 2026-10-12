package com.vetcitas.gestion.repository;

import com.vetcitas.gestion.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
  // La consulta limita los resultados en la base de datos según el actor y permite filtrar por
  // estado.
  @Query(
      "select r from Reserva r where (:admin = true or r.usuario.id = :clienteId or"
          + " r.veterinario.id = :veterinarioId) and (:estado is null or r.estado = :estado) order"
          + " by r.fechaHora")
  List<Reserva> listarVisibles(
      @Param("admin") boolean admin,
      @Param("clienteId") Long clienteId,
      @Param("veterinarioId") Long veterinarioId,
      @Param("estado") EstadoReserva estado);

  // Dos intervalos chocan si uno empieza antes del fin del otro y termina después de su inicio.
  @Query(
      "select count(r) from Reserva r where r.veterinario.id = :vet and r.id <> :id "
          + "and r.estado in :estados and r.fechaHora < :fin and r.fin > :inicio")
  long contarCruces(
      @Param("vet") Long vet,
      @Param("id") Long id,
      @Param("inicio") LocalDateTime inicio,
      @Param("fin") LocalDateTime fin,
      @Param("estados") List<EstadoReserva> estados);

  boolean existsByUsuarioIdOrVeterinarioId(Long usuarioId, Long veterinarioId);

  boolean existsByServicioId(Long servicioId);
}
