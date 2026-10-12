package com.vetcitas.gestion.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa a un usuario del sistema
 * (puede ser CLIENTE, VETERINARIO o ADMIN).
 *
 * @Entity   -> Le indica a Spring Data JPA / Hibernate que esta clase
 *              representa una tabla en la base de datos. Cada instancia
 *              de esta clase equivale a una fila de esa tabla.
 * @Table    -> Especifica explícitamente el nombre de la tabla asociada.
 *              Si se omite, JPA usaría el nombre de la clase por defecto.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * @Id                 -> Marca este campo como la clave primaria (PK) de la tabla.
     * @GeneratedValue      -> Indica que el valor lo genera la base de datos
     *                         automáticamente. IDENTITY delega el autoincremento
     *                         al motor de BD (equivalente a AUTO_INCREMENT en MySQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column -> Permite personalizar el mapeo de un atributo a una columna.
     * nullable = false     -> genera la restricción NOT NULL en la tabla.
     * length               -> tamaño máximo del VARCHAR.
     */
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

  
    @Column(nullable = false)
    private String password;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * @PrePersist -> Método de callback del ciclo de vida de JPA.
     * Se ejecuta automáticamente justo ANTES de que la entidad se
     * inserte por primera vez en la base de datos. Lo usamos para
     * fijar la fecha de registro sin tener que hacerlo manualmente
     * cada vez que se crea un Usuario nuevo.
     */
    @PrePersist
    protected void alPersistir() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.rol == null) {
            this.rol = Rol.CLIENTE; 
        }
    }


    public Usuario() {
       
    }

    public Usuario(String nombre, String correo, String telefono, String password, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.password = password;
        this.rol = rol;
    }

 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
