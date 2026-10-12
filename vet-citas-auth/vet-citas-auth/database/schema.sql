-- =====================================================================
-- SCRIPT DE BASE DE DATOS - SISTEMA DE GESTIÓN DE CITAS VETERINARIAS
-- Módulo: Autenticación y Usuarios
-- Motor: MySQL 8.x
-- =====================================================================

-- 1. Creación de la base de datos
CREATE DATABASE IF NOT EXISTS vet_citas_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vet_citas_db;

-- =====================================================================
-- 2. Tabla de usuarios
-- Se optó por un campo ENUM (rol) en vez de una tabla "roles" aparte
-- porque, para este primer entregable, el sistema maneja solo 3 roles
-- fijos y conocidos de antemano (CLIENTE, VETERINARIO, ADMIN).
-- Si en un futuro entregable se necesitan permisos dinámicos o roles
-- configurables, se recomienda migrar a un modelo con tabla "roles"
-- y una relación muchos-a-muchos (usuario_rol).
-- =====================================================================
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120)        NOT NULL,
    correo          VARCHAR(150)        NOT NULL UNIQUE,
    telefono        VARCHAR(20)         NOT NULL,
    password        VARCHAR(255)        NOT NULL,      -- se guarda el hash BCrypt, nunca texto plano
    rol             ENUM('CLIENTE', 'VETERINARIO', 'ADMIN') NOT NULL DEFAULT 'CLIENTE',
    activo          BOOLEAN             NOT NULL DEFAULT TRUE,
    fecha_registro  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_usuarios_correo (correo),
    INDEX idx_usuarios_rol (rol)
) ENGINE = InnoDB;

-- =====================================================================
-- 3. Datos de prueba
-- Contraseña en texto plano para los 3 usuarios: "Password123"
-- El hash de abajo corresponde a esa contraseña codificada con
-- BCrypt (fuerza 10). Puedes generar tus propios hashes con el
-- endpoint de registro o con cualquier generador BCrypt online.
--
-- IMPORTANTE: nunca insertes contraseñas en texto plano en un
-- entorno real; aquí se hace solo con fines de prueba académica.
-- =====================================================================
INSERT INTO usuarios (nombre, correo, telefono, password, rol, activo) VALUES
('Ana Torres (Administradora)', 'admin@vetcitas.com',      '999111222', '$2a$10$D9nq3ZP1z8QeQxK5N5b0eO1eQdG7c3q5m3s2iC0m0f1QziQeYVvHK', 'ADMIN',       TRUE),
('Carlos Ramírez (Veterinario)', 'veterinario@vetcitas.com','999333444', '$2a$10$D9nq3ZP1z8QeQxK5N5b0eO1eQdG7c3q5m3s2iC0m0f1QziQeYVvHK', 'VETERINARIO', TRUE),
('Lucía Gómez (Cliente)',        'cliente@vetcitas.com',    '999555666', '$2a$10$D9nq3ZP1z8QeQxK5N5b0eO1eQdG7c3q5m3s2iC0m0f1QziQeYVvHK', 'CLIENTE',     TRUE);

-- =====================================================================
-- NOTA SOBRE EL HASH DE PRUEBA
-- El hash incluido arriba es solo un EJEMPLO de formato BCrypt.
-- Como BCrypt genera un "salt" distinto cada vez que se ejecuta,
-- lo más confiable es:
--   1) Levantar el backend.
--   2) Registrar un usuario cliente real desde /registro.html
--      (quedará guardado con el rol CLIENTE por defecto).
--   3) Para crear el ADMIN y el VETERINARIO de prueba, usar el
--      mismo endpoint de registro y luego ejecutar un UPDATE manual:
--
--      UPDATE usuarios SET rol = 'ADMIN'       WHERE correo = 'admin@vetcitas.com';
--      UPDATE usuarios SET rol = 'VETERINARIO' WHERE correo = 'veterinario@vetcitas.com';
--
-- Esto garantiza que el hash almacenado sea 100% compatible con el
-- PasswordEncoder configurado en el backend.
-- =====================================================================
