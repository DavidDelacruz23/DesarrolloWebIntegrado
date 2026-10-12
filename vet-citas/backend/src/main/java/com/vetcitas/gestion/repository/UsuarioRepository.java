package com.vetcitas.gestion.repository;

import com.vetcitas.gestion.entity.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** JpaRepository ya proporciona save, findById, findAll y delete. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  // Spring interpreta el nombre y genera la consulta, ignorando mayúsculas en el correo.
  Optional<Usuario> findByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

  List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(Rol rol);

  // Bloqueamos la fila del veterinario durante una reserva para evitar solapamientos simultáneos.
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Usuario u where u.id = :id")
  Optional<Usuario> bloquearPorId(@Param("id") Long id);
}
