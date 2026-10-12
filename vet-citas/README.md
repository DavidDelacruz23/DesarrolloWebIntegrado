# VetCitas — Citas veterinarias con Spring Boot y MySQL

Este proyecto amplía el ZIP original de autenticación con CRUD de usuarios, servicios y reservas, persistencia relacional, seguridad en el servidor y paneles web funcionales. El código propio está comentado en español para facilitar su estudio.

## Inicio rápido en Windows

1. Descomprime el proyecto. Necesitas **Java 17 o posterior** y **MySQL 8** en ejecución. No hace falta instalar Maven: se incluye Maven Wrapper. La primera ejecución necesita Internet para descargar dependencias.
2. Abre PowerShell en esta carpeta. Para probar con cuentas de ejemplo, ejecuta:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\iniciar.ps1 -Demo
   ```

   El permiso de ejecución anterior se limita a ese proceso. El script solicita el usuario y la contraseña de MySQL, sin guardarlos en el código. El usuario de MySQL necesita permisos sobre `vet_citas_db`; si no puede crear bases, crea primero esa base desde Workbench.
3. Espera el mensaje de inicio de Spring Boot y abre **http://localhost:8080/login.html**.
4. El perfil `demo` crea estas cuentas si sus correos todavía no existen:

   | Rol | Correo | Contraseña de demostración |
   |---|---|---|
   | Administrador | admin@vetcitas.com | Password123 |
   | Veterinario | veterinario@vetcitas.com | Password123 |
   | Cliente | cliente@vetcitas.com | Password123 |

5. Entra como cliente, pulsa **Nueva cita**, selecciona mascota, servicio, profesional y una fecha futura. Entra después como veterinario para confirmar la cita. Desde el administrador puedes gestionar todo el catálogo y las cuentas.
6. Detén el servidor con **Ctrl+C**. Los registros permanecen en MySQL para la siguiente ejecución.

Las cuentas anteriores son para práctica local. Sin `-Demo` no se crean cuentas de demostración. El perfil demo no sobrescribe usuarios existentes; si conservas cuentas del SQL original con hashes ficticios, registra una cuenta nueva y asigna el rol con el procedimiento de abajo.

## Configuración manual o desde IntelliJ

Abre `backend/pom.xml` como proyecto Maven y ejecuta `GestionCitasVeterinariasApplication`. Las variables se definen en la configuración de ejecución del IDE o en la misma terminal que inicia Java:

```powershell
$env:DB_USER = 'root'
$env:DB_PASSWORD = 'TU_CLAVE_LOCAL'
$env:DB_URL = 'jdbc:mysql://localhost:3306/vet_citas_db?createDatabaseIfNotExist=true&serverTimezone=America/Lima'
cd backend
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

Sustituye `TU_CLAVE_LOCAL` por tu contraseña real. Si prefieres no escribirla en el historial, usa `iniciar.ps1`. En Linux/macOS, configura las mismas variables y ejecuta `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo` dentro de `backend` (puede requerir `chmod +x mvnw`).

`application.properties` toma `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT` y `SHOW_SQL`. Usa el puerto 8080 por defecto. Al iniciar, Hibernate crea las tablas faltantes con `ddl-auto=update`; `database/schema.sql` permite crearlas manualmente y no contiene `DROP TABLE`.

Para trabajar sobre tu base original, conserva una copia de respaldo y usa su URL en `DB_URL`. Se mantienen las columnas de `usuarios` y se añaden `servicios` y `reservas`. El SQL original traía hashes de ejemplo que no correspondían a contraseñas funcionales; esta versión genera hashes reales al registrar o crear usuarios.

## Crear el primer administrador sin perfil demo

Registra una cuenta desde la web. Luego, en tu base de datos local, promueve **ese correo que tú registraste**:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE correo = 'tu-correo-registrado@ejemplo.com';
```

Inicia sesión de nuevo como administrador. Desde Usuarios puedes crear veterinarios, clientes y otros administradores con contraseñas reales. El registro público nunca permite asignarse ADMIN.

## Qué permite cada rol

| Operación | Cliente | Veterinario | Administrador |
|---|---|---|---|
| Consultar servicios activos y profesionales | Sí | Sí | Sí, incluye servicios inactivos |
| Crear o modificar servicios | No | No | Sí |
| Gestionar usuarios y sus roles | No | No | Sí |
| Consultar y editar su propio perfil | Sí | Sí | Sí |
| Consultar citas | Propias | Asignadas | Todas |
| Crear y reprogramar citas | Propias | No | Para clientes |
| Confirmar y marcar atendida | No | Asignadas | Todas |
| Cancelar | Propias y futuras | Asignadas | Todas |
| Eliminar citas | Propias canceladas | No | Sí |

No se borra un usuario o servicio que tenga reservas; el administrador puede desactivarlo. No puede eliminar su propia cuenta, desactivarla ni quitarse su rol. Los cambios de rol, desactivación y contraseña afectan las sesiones existentes.

## Reglas de las citas

- Todas las fechas de la agenda se interpretan en **America/Lima**. La API recibe `2026-11-20T10:30:00`, sin sufijo UTC; elige siempre una fecha futura al probar.
- Una reserva enlaza cliente, veterinario y servicio activos. En este avance el nombre de la mascota forma parte de la reserva; no hay una tabla de mascotas independiente.
- Se calcula la hora final usando la duración del servicio y se guarda una copia del precio. Editar el catálogo no altera precios ni duraciones de citas existentes; cambiar el servicio de una cita sí recalcula ambos.
- Se rechazan intervalos que se solapan para el mismo veterinario. Dos citas consecutivas, donde una termina exactamente cuando empieza otra, sí están permitidas. El bloqueo de fila y la transacción también cubren peticiones simultáneas.
- Flujo: `PENDIENTE → CONFIRMADA → ATENDIDA`. Una pendiente o confirmada puede pasar a `CANCELADA`. Una cita finalizada no se reabre y solo puede marcarse atendida cuando haya comenzado.
- Reprogramar una cita futura pendiente o confirmada la devuelve a pendiente. Una cancelación libera el horario. No se han definido jornadas, feriados ni recordatorios automáticos porque el Word no los especifica.

## Probar la API

El prefijo `/api` separa las rutas REST de los archivos HTML. `reservas` es el nombre técnico de las citas veterinarias.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/auth/csrf` | Token para formularios y operaciones de escritura |
| POST | `/api/auth/registro` | Alta pública de cliente |
| POST | `/api/auth/login` | Iniciar sesión y recibir cookie JSESSIONID |
| GET | `/api/auth/sesion` | Cuenta de la sesión, sin contraseña |
| POST | `/api/auth/logout` | Cerrar sesión |
| GET / POST | `/api/usuarios` | Listar / crear usuarios (ADMIN) |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Consultar / modificar / borrar (ADMIN) |
| GET / PUT | `/api/usuarios/me` | Consultar / editar perfil propio |
| GET | `/api/veterinarios` | IDs y nombres de profesionales activos |
| GET / POST | `/api/servicios` | Buscar / crear servicios |
| GET / PUT / DELETE | `/api/servicios/{id}` | Consultar / modificar / borrar |
| GET / POST | `/api/reservas` | Listar citas visibles / reservar |
| GET / PUT / DELETE | `/api/reservas/{id}` | Consultar / reprogramar / borrar |
| PATCH | `/api/reservas/{id}/estado` | Cambiar estado |

Filtros: `/api/servicios?nombre=consulta` y `/api/reservas?estado=PENDIENTE`.

**Postman:** importa `docs/VetCitas.postman_collection.json`, configura `baseUrl` y ejecuta las solicitudes en orden con el perfil demo. El script de la colección pide el token CSRF automáticamente; deja habilitado el almacenamiento de cookies de Postman. Crea sus propios registros y usa variables para sus IDs. La última carpeta elimina esos registros de prueba.

**Swagger:** inicia sesión en la web y abre http://localhost:8080/api-docs.html en el mismo navegador. La UI comparte la sesión y pide un token CSRF antes de escribir. El contrato JSON está en `/v3/api-docs`. Primero inicia sesión con el rol correspondiente; Swagger no elimina restricciones de acceso.

Respuestas: `201` al crear, `200` al consultar/modificar, `204` al borrar; `400` para datos inválidos, `401` sin sesión/credenciales incorrectas, `403` por permisos o CSRF, `404` si no existe el recurso y `409` por conflicto de horario, correo o relaciones.

## Pruebas y compilación

Dentro de `backend`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/gestion-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Las 14 pruebas de integración usan H2 en memoria por defecto para que puedas ejecutarlas sin una base configurada. La aplicación normal siempre usa MySQL. Se comprobaron también las mismas pruebas con MySQL 8.0.46 en una instancia separada. Lee `docs/VERIFICACION.md` para los resultados reales.

Para ejecutarlas en una **base de pruebas vacía y exclusiva**, cambia las propiedades de prueba mediante argumentos JVM/Maven. Las pruebas usan `create-drop` y limpian sus tablas: nunca apuntes al esquema de trabajo. Ejemplo sin contraseña, para una instancia local aislada:

```powershell
.\mvnw.cmd test '-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3308/vet_citas_test?createDatabaseIfNotExist=true' '-Dspring.datasource.username=root' '-Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver'
```

## Estructura y lectura para principiantes

```text
vet-citas/
├── README.md                         Cómo iniciar y usar el sistema
├── iniciar.ps1                       Lanzador que solicita credenciales
├── database/                         Esquema y consultas SQL comentadas
├── docs/                             Guías, requisitos, pruebas y Postman
└── backend/
    ├── pom.xml                       Dependencias Maven; conserva Boot 3.3.4 del original
    ├── mvnw / mvnw.cmd                Maven Wrapper
    └── src/
        ├── main/java/com/vetcitas/gestion/
        │   ├── controller/           Rutas HTTP y respuestas JSON
        │   ├── dto/                  Datos que entran/salen y sus validaciones
        │   ├── service/              Reglas y transacciones
        │   ├── repository/           Acceso a JPA y consultas JPQL
        │   ├── entity/               Tablas y relaciones
        │   ├── security/             Identidad y revisión de sesiones
        │   ├── config/               Seguridad y datos demo
        │   └── exception/            Errores JSON uniformes
        ├── main/resources/static/    HTML, CSS y JavaScript sin frameworks extra
        └── test/                     Pruebas de integración con datos aislados
```

Empieza por `docs/GUIA-PARA-PRINCIPIANTES.md`. Para relacionar la implementación con el Word, consulta `docs/AVANCE-SEMANA-10.md`.
