package com.vetcitas.gestion.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa una fila de la tabla reservas. JPA convierte estos objetos en registros SQL. */
@Entity
@Table(name = "reservas")
public class Reserva {
  // Identificador de la cita.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Cliente propietario: muchos registros apuntan a un usuario.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  // Profesional que atenderá la cita.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "veterinario_id", nullable = false)
  private Usuario veterinario;

  // Servicio elegido; la clave foránea mantiene la integridad.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "servicio_id", nullable = false)
  private Servicio servicio;

  // Nombre de la mascota para este avance del proyecto.
  @Column(nullable = false, length = 100)
  private String mascota;

  // Motivo de la visita.
  @Column(nullable = false, length = 500)
  private String motivo;

  // Inicio de la cita, expresado en hora de Lima.
  @Column(nullable = false)
  private LocalDateTime fechaHora;

  // Fin calculado al reservar; no cambia al editar el catálogo.
  @Column(nullable = false)
  private LocalDateTime fin;

  // Precio acordado al reservar, conservado como dato histórico.
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal precio;

  // Estado controlado por reglas, no por un texto libre.
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoReserva estado = EstadoReserva.PENDIENTE;

  // Detecta dos modificaciones simultáneas a la misma reserva.
  @Version private Long version;

  // JPA necesita un constructor sin argumentos para reconstruir objetos desde MySQL.
  public Reserva() {}

  /** Permite leer id sin acceder directamente al atributo privado. */
  public Long getId() {
    return id;
  }

  /** Asigna id; las reglas de negocio se validan en el servicio. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Permite leer usuario sin acceder directamente al atributo privado. */
  public Usuario getUsuario() {
    return usuario;
  }

  /** Asigna usuario; las reglas de negocio se validan en el servicio. */
  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  /** Permite leer veterinario sin acceder directamente al atributo privado. */
  public Usuario getVeterinario() {
    return veterinario;
  }

  /** Asigna veterinario; las reglas de negocio se validan en el servicio. */
  public void setVeterinario(Usuario veterinario) {
    this.veterinario = veterinario;
  }

  /** Permite leer servicio sin acceder directamente al atributo privado. */
  public Servicio getServicio() {
    return servicio;
  }

  /** Asigna servicio; las reglas de negocio se validan en el servicio. */
  public void setServicio(Servicio servicio) {
    this.servicio = servicio;
  }

  /** Permite leer mascota sin acceder directamente al atributo privado. */
  public String getMascota() {
    return mascota;
  }

  /** Asigna mascota; las reglas de negocio se validan en el servicio. */
  public void setMascota(String mascota) {
    this.mascota = mascota;
  }

  /** Permite leer motivo sin acceder directamente al atributo privado. */
  public String getMotivo() {
    return motivo;
  }

  /** Asigna motivo; las reglas de negocio se validan en el servicio. */
  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

  /** Permite leer fechaHora sin acceder directamente al atributo privado. */
  public LocalDateTime getFechaHora() {
    return fechaHora;
  }

  /** Asigna fechaHora; las reglas de negocio se validan en el servicio. */
  public void setFechaHora(LocalDateTime fechaHora) {
    this.fechaHora = fechaHora;
  }

  /** Permite leer fin sin acceder directamente al atributo privado. */
  public LocalDateTime getFin() {
    return fin;
  }

  /** Asigna fin; las reglas de negocio se validan en el servicio. */
  public void setFin(LocalDateTime fin) {
    this.fin = fin;
  }

  /** Permite leer precio sin acceder directamente al atributo privado. */
  public BigDecimal getPrecio() {
    return precio;
  }

  /** Asigna precio; las reglas de negocio se validan en el servicio. */
  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  /** Permite leer estado sin acceder directamente al atributo privado. */
  public EstadoReserva getEstado() {
    return estado;
  }

  /** Asigna estado; las reglas de negocio se validan en el servicio. */
  public void setEstado(EstadoReserva estado) {
    this.estado = estado;
  }

  /** Permite leer version sin acceder directamente al atributo privado. */
  public Long getVersion() {
    return version;
  }

  /** Asigna version; las reglas de negocio se validan en el servicio. */
  public void setVersion(Long version) {
    this.version = version;
  }
}
