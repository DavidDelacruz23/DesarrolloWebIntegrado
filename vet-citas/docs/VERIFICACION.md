# Verificación realizada el 11 de octubre de 2026

Se compiló y ejecutó el proyecto con Java 21.0.12.1, Maven Wrapper 3.9.12 y Spring Boot 3.3.4, conservando Java 17 como versión objetivo. La aplicación se conectó a MySQL 8.0.46 en una instancia exclusiva de pruebas. No se utilizaron bases de datos preexistentes del usuario.

## Pruebas de integración

`ApiIntegrationTest` contiene **14 pruebas, con 0 fallos y 0 errores**, ejecutadas con H2 y también con MySQL. La revisión posterior de comentarios y Swagger volvió a compilarse correctamente; las pruebas se repitieron tras integrar la página de documentación.

1. Registro: hash BCrypt válido, correo normalizado y rol CLIENTE obligatorio.
2. Login: cambio del identificador de sesión; logout revoca acceso.
3. Rechazo de credenciales incorrectas y escrituras sin token CSRF.
4. Protección por roles de endpoints, páginas y contrato OpenAPI.
5. CRUD de servicios, búsqueda JPQL e invisibilidad de servicios inactivos para clientes.
6. CRUD de usuarios, contraseña opcional al editar y perfil sin escalación de permisos.
7. CRUD de reservas y aislamiento entre clientes.
8. Acceso del veterinario a sus propias citas y transiciones de estado.
9. Rechazo de horarios cruzados, aceptación de citas consecutivas y liberación tras cancelar.
10. Dos reservas simultáneas para el mismo horario: solo una se guarda.
11. Restricciones de borrado por relaciones y conservación del precio y duración históricos.
12. Validación de campos, fechas, identificadores y propietario de la reserva.
13. Desactivación y cambio de rol aplicados a sesiones ya abiertas.
14. Cambio de contraseña revoca la sesión; límite de bytes UTF-8 de BCrypt.

## Pruebas de la aplicación completa

Se comprobaron **22 solicitudes HTTP** basadas en la colección Postman, incluyendo login, CSRF, CRUD de los tres recursos, filtros, confirmación y cierre de sesión. Todas devolvieron el estado esperado. La comprobación se hizo con un cliente HTTP y cookies reales; no se ejecutó la aplicación de escritorio Postman. El resultado está en `evidencias/08-comprobacion-coleccion.json`.

La interfaz se probó en un navegador: login de cliente, creación de una cita, reprogramación, logout, login de veterinario, confirmación de la cita y alta de un servicio como administrador. Se revisaron el listado de usuarios y la disposición de escritorio y móvil. Las capturas corresponden a datos de demostración locales.

En la página Swagger integrada `/api-docs.html` se ejecutó un POST real de servicio y se obtuvo **201 Created**, utilizando la sesión del panel y un token CSRF obtenido automáticamente. El contrato final documenta 201 para creación y 204 para borrado. `openapi.json` es una copia del contrato generado durante la prueba local en el puerto 8081; normalmente se consulta el contrato vivo del puerto configurado.

El script `database/schema.sql` se ejecutó en un esquema separado y la aplicación arrancó con `spring.jpa.hibernate.ddl-auto=validate`, confirmando la compatibilidad del SQL con las entidades. También se comprobó que la cita creada desde la interfaz continuaba guardada después de reiniciar el servidor.

## Evidencias incluidas

| Archivo | Qué permite comprobar |
|---|---|
| `01-panel-cliente.jpg` | Cita creada y reprogramada por el cliente |
| `02-panel-veterinario.jpg` | Cita confirmada por el profesional asignado |
| `03-servicios-admin.jpg` | Catálogo de servicios con controles administrativos |
| `04-usuarios-admin.jpg` | Listado de usuarios por rol |
| `05-vista-movil.jpg` | Interfaz de servicios en pantalla pequeña |
| `06-mysql.txt` | Usuarios, servicios y reserva persistidos en MySQL |
| `07-respuesta-reserva.json` | Respuesta REST real de una reserva |
| `08-comprobacion-coleccion.json` | Resultados de las 22 solicitudes de la colección |
| `09-pruebas.txt` | Resúmenes de Maven y validación del esquema |

Estas verificaciones cubren el avance académico pedido. No se realizó despliegue público, envío de recordatorios, publicación en GitHub ni pruebas de carga de producción.
