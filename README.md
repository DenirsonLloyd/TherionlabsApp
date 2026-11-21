

## 1. Nombre del proyecto

TherionLabs.

## 2. Integrantes

Juan Roldan y Denirson Lloyd.

## 3. Funcionalidades

La aplicación permite registrar usuarios, iniciar sesión, completar postulaciones, revisar el estado de las solicitudes y consultar la información guardada. También incluye un panel de administración para revisar y gestionar los registros. El usuario puede adjuntar una fotografía a su postulación. La aplicación mantiene información local con Room y se comunica con los microservicios para guardar y consultar usuarios y postulaciones. Además, consulta los feriados de Chile mediante una API externa.

## 4. Endpoints utilizados

### Microservicio de usuarios

Base URL: `http://107.21.117.137:8081/`

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Consultar un usuario |
| GET | `/api/usuarios/correo/{correo}` | Buscar por correo |
| POST | `/api/usuarios` | Registrar usuario |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

### Microservicio de postulaciones

Base URL: `http://107.21.117.137:8082/`

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/api/postulaciones` | Listar postulaciones |
| GET | `/api/postulaciones/usuario/{usuarioId}` | Listar postulaciones de un usuario |
| GET | `/api/postulaciones/{id}` | Consultar una postulación |
| POST | `/api/postulaciones` | Crear una postulación |
| PUT | `/api/postulaciones/{id}` | Actualizar una postulación |
| PUT | `/api/postulaciones/{id}/estado` | Cambiar el estado |
| POST | `/api/postulaciones/{id}/foto` | Subir una fotografía (multipart, campo `foto`) |
| GET | `/api/postulaciones/{id}/foto` | Consultar la fotografía |
| DELETE | `/api/postulaciones/{id}` | Eliminar una postulación |

### API externa

La aplicación consulta los feriados de Chile mediante Nager.Date:

`GET https://date.nager.at/api/v3/PublicHolidays/{anio}/CL`

## 5. Pasos para ejecutar

### Aplicación Android

1. Instalar Android Studio y el SDK requerido por el proyecto.
2. Abrir la carpeta principal del proyecto en Android Studio.
3. Esperar que termine la sincronización de Gradle.
4. Revisar en `app/src/main/java/com/dsy1105/therionlabsapp/api/ApiCliente.kt` que las direcciones de los microservicios correspondan a la IP pública de EC2.
5. Iniciar un emulador o conectar un dispositivo Android y ejecutar la configuración `app`.

### Microservicios en AWS EC2

Se necesita Java 17 y Maven. Los dos servicios se ejecutan por separado en la misma instancia EC2.

Para usuarios, desde la carpeta principal del proyecto:

```bash
cd backend/usuarios-service
mkdir -p datos
mvn clean package -DskipTests
nohup java -jar target/usuarios-service-1.0.0.jar > app.log 2>&1 &
```

Para postulaciones, abrir otra terminal y ejecutar:

```bash
cd backend/postulaciones-service
mkdir -p datos/fotos
mvn clean package -DskipTests
nohup java -jar target/postulaciones-service-1.0.0.jar > app.log 2>&1 &
```

El servicio de usuarios utiliza el puerto `8081` y el de postulaciones utiliza el puerto `8082`. El grupo de seguridad de EC2 debe permitir el acceso a esos puertos.

Para ejecutar los microservicios localmente en Windows, abrir una terminal para cada servicio y utilizar `mvn spring-boot:run` desde su respectiva carpeta. En ese caso, las direcciones de `ApiCliente.kt` deben apuntar al servidor local correspondiente.

## 6. Captura del APK firmado y archivo .jks

DENIRSON
