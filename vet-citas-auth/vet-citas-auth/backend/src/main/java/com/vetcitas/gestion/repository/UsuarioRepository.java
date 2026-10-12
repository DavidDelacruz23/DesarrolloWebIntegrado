package com.vetcitas.gestion.repository;

import com.vetcitas.gestion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Repository -> Marca esta interfaz como un componente de acceso a datos.
 * Spring la detecta automáticamente (component scanning) y crea una
 * implementación en tiempo de ejecución, así que NO es necesario escribir
 * el código SQL/JDBC manualmente para las operaciones básicas.
 *
 * Al extender JpaRepository<Usuario, Long> ya obtenemos "gratis" métodos
 * como save(), findById(), findAll(), deleteById(), etc.
 * Usuario  -> tipo de la entidad que maneja este repositorio.
 * Long     -> tipo de dato de la clave primaria (id) de esa entidad.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

 
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
