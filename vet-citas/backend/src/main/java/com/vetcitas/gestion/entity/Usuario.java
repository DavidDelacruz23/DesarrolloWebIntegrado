package com.vetcitas.gestion.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Representa una fila de la tabla usuarios. JPA convierte estos objetos en registros SQL. */
@Entity
@Table(name = "usuarios")
public class Usuario {
  // MySQL genera el identificador automáticamente.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Nombre visible del usuario.
  @Column(nullable = false, length = 120)
  private String nombre;

  // No pueden existir dos cuentas con el mismo correo.
  @Column(nullable = false, unique = true, length = 150)
  private String correo;

  // Teléfono de contacto.
  @Column(nullable = false, length = 20)
  private String telefono;

  // Aquí se guarda el hash BCrypt, nunca la contraseña original.
  @Column(nullable = false)
  private String password;

  // El rol determina los permisos en el servidor.
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Rol rol = Rol.CLIENTE;

  // Desactivar una cuenta impide usarla sin borrar su historial.
  @Column(nullable = false)
  private boolean activo = true;

  // Fecha de creación del registro.
  @Column(name = "fecha_registro", nullable = false, updatable = false)
  private LocalDateTime fechaRegistro;

  // Un cliente puede tener muchas reservas; no se borran en cascada.
  @OneToMany(mappedBy = "usuario")
  private List<Reserva> reservas = new ArrayList<>();

  // Un veterinario puede atender muchas reservas.
  @OneToMany(mappedBy = "veterinario")
  private List<Reserva> citasAsignadas = new ArrayList<>();

  // Se ejecuta justo antes del INSERT y evita depender del reloj del navegador.
  @PrePersist
  void alPersistir() {
    fechaRegistro = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
  }

  // JPA necesita un constructor sin argumentos para reconstruir objetos desde MySQL.
  public Usuario() {}

  /** Permite leer id sin acceder directamente al atributo privado. */
  public Long getId() {
    return id;
  }

  /** Asigna id; las reglas de negocio se validan en el servicio. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Permite leer nombre sin acceder directamente al atributo privado. */
  public String getNombre() {
    return nombre;
  }

  /** Asigna nombre; las reglas de negocio se validan en el servicio. */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /** Permite leer correo sin acceder directamente al atributo privado. */
  public String getCorreo() {
    return correo;
  }

  /** Asigna correo; las reglas de negocio se validan en el servicio. */
  public void setCorreo(String correo) {
    this.correo = correo;
  }

  /** Permite leer telefono sin acceder directamente al atributo privado. */
  public String getTelefono() {
    return telefono;
  }

  /** Asigna telefono; las reglas de negocio se validan en el servicio. */
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  /** Permite leer password sin acceder directamente al atributo privado. */
  public String getPassword() {
    return password;
  }

  /** Asigna password; las reglas de negocio se validan en el servicio. */
  public void setPassword(String password) {
    this.password = password;
  }

  /** Permite leer rol sin acceder directamente al atributo privado. */
  public Rol getRol() {
    return rol;
  }

  /** Asigna rol; las reglas de negocio se validan en el servicio. */
  public void setRol(Rol rol) {
    this.rol = rol;
  }

  /** Permite leer activo sin acceder directamente al atributo privado. */
  public boolean isActivo() {
    return activo;
  }

  /** Asigna activo; las reglas de negocio se validan en el servicio. */
  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  /** Permite leer fechaRegistro sin acceder directamente al atributo privado. */
  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  /** Asigna fechaRegistro; las reglas de negocio se validan en el servicio. */
  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  /** Permite leer reservas sin acceder directamente al atributo privado. */
  public List<Reserva> getReservas() {
    return reservas;
  }

  /** Asigna reservas; las reglas de negocio se validan en el servicio. */
  public void setReservas(List<Reserva> reservas) {
    this.reservas = reservas;
  }

  /** Permite leer citasAsignadas sin acceder directamente al atributo privado. */
  public List<Reserva> getCitasAsignadas() {
    return citasAsignadas;
  }

  /** Asigna citasAsignadas; las reglas de negocio se validan en el servicio. */
  public void setCitasAsignadas(List<Reserva> citasAsignadas) {
    this.citasAsignadas = citasAsignadas;
  }
}
