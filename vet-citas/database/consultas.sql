-- Ejecuta cada consulta en MySQL Workbench para obtener evidencias del avance.
USE vet_citas_db;

-- Listar cuentas sin mostrar contraseñas ni hashes.
SELECT id, nombre, correo, telefono, rol, activo FROM usuarios ORDER BY id;
-- Consultar el catálogo y su duración.
SELECT id, nombre, precio, duracion_minutos, activo FROM servicios ORDER BY nombre;
-- JOIN reúne las tablas relacionadas en una agenda legible.
SELECT r.id, c.nombre AS cliente, v.nombre AS veterinario, s.nombre AS servicio,
       r.mascota, r.fecha_hora, r.fin, r.precio, r.estado
FROM reservas r
JOIN usuarios c ON c.id = r.usuario_id
JOIN usuarios v ON v.id = r.veterinario_id
JOIN servicios s ON s.id = r.servicio_id
ORDER BY r.fecha_hora;
-- Equivalente SQL de la búsqueda JPQL por nombre.
SELECT id, nombre FROM servicios WHERE LOWER(nombre) LIKE '%consulta%';
-- Comprobar las relaciones que existen realmente en MySQL.
SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL;
