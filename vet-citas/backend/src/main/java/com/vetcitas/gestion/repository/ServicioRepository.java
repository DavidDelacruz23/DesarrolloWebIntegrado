package com.vetcitas.gestion.repository;

import com.vetcitas.gestion.entity.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Repositorio: es la única capa que escribe consultas de persistencia. */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
  // JPQL usa los nombres de la entidad y sus atributos, no los nombres de columnas SQL.
  @Query(
      "select s from Servicio s where lower(s.nombre) like lower(concat('%', :nombre, '%')) "
          + "and (:todos = true or s.activo = true) order by s.nombre")
  List<Servicio> buscarPorNombre(@Param("nombre") String nombre, @Param("todos") boolean todos);
}
