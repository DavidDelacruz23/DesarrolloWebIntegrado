-- VetCitas: esquema MySQL 8. No se borran tablas ni registros existentes.
-- Si utilizas Hibernate con ddl-auto=update, ejecutar este archivo es opcional.
CREATE DATABASE IF NOT EXISTS vet_citas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vet_citas_db;

-- Un usuario representa una cuenta que inicia sesión. La contraseña contiene un hash BCrypt.
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE','VETERINARIO','ADMIN') NOT NULL DEFAULT 'CLIENTE',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- Catálogo de prestaciones; desactivar permite conservar servicios con historial.
CREATE TABLE IF NOT EXISTS servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    duracion_minutos INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- La reserva une cliente, profesional y servicio mediante claves foráneas.
CREATE TABLE IF NOT EXISTS reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    veterinario_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    mascota VARCHAR(100) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    fin DATETIME(6) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE','CONFIRMADA','ATENDIDA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY(usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_reserva_veterinario FOREIGN KEY(veterinario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_reserva_servicio FOREIGN KEY(servicio_id) REFERENCES servicios(id),
    INDEX idx_reserva_agenda(veterinario_id, fecha_hora, fin)
) ENGINE=InnoDB;

-- No se insertan contraseñas ficticias en SQL. El perfil demo genera hashes BCrypt válidos en Java.
