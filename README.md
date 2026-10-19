# 🎓 TherionLabs

### Aplicación móvil para el registro, seguimiento y administración de prospectos académicos

Una solución móvil robusta desarrollada en Android que facilita la captura de contactos en terreno, la organización de intereses académicos, la persistencia local de datos y un módulo exclusivo de control y supervisión gerencial.

## 📌 Descripción del proyecto

**TherionLabs** es una aplicación móvil desarrollada para Android orientada a optimizar el trabajo de captación en terreno y la gestión administrativa de prospectos interesados en una oferta académica. El sistema permite registrar datos personales y preferencias de estudio mediante flujos guiados, almacenar la información de forma segura en el dispositivo mediante una base de datos local y ofrecer un panel de control avanzado para administradores.

El proyecto se desarrolla como parte de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**.

## 🎯 Objetivo

Proveer una herramienta multiplataforma e intuitiva que permita a los captadores registrar prospectos rápidamente y a los administradores supervisar el rendimiento general, consultar bases de datos globales y configurar los parámetros del sistema de manera local.

## ✨ Funcionalidades principales

- 🔐 **Control de acceso basado en roles:** separación estricta de credenciales y flujos de navegación entre usuarios captadores y administradores.
- 📝 **Formulario de captación por pasos:** interfaz intuitiva para registrar datos de contacto y preferencias académicas.
- 💾 **Persistencia local con Room:** almacenamiento seguro y consulta reactiva mediante bases de datos relacionales en el dispositivo.
- 📋 **Listados maestros y estados:** visualización organizada de los prospectos capturados.
- ⚙️ **Panel de administración general:** herramientas de supervisión, reportes y control de operaciones en terreno.

## 🛠️ Tecnologías utilizadas

| **Tecnología** | **Uso** |
|---|---|
| **Kotlin** | Lenguaje principal de desarrollo |
| **Android Studio** | Entorno oficial de desarrollo y compilación |
| **Jetpack Compose** | Construcción de interfaces de usuario modernas y declarativas |
| **Material 3** | Componentes visuales y diseño de interfaz corporativa |
| **Room** | ORM y persistencia de datos relacionales locales |
| **Kotlin Coroutines & Flow** | Operaciones asíncronas y flujo reactivo de datos |
| **Gradle** | Gestión de dependencias y construcción de paquetes |
| **Git & GitHub** | Control de versiones y colaboración en equipo |

## 🧱 Estructura y explicación del proyecto

El código fuente del proyecto está organizado de manera modular bajo el paquete raíz `com.dsy1105.therionlabsapp`. A continuación se detalla cada componente:

```text
TherionlabsApp/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/dsy1105/therionlabsapp/
│       │   │   ├── datos/                     # Persistencia y modelos de base de datos
│       │   │   │   ├── BaseDatosApp.kt        # Configuración principal de Room
│       │   │   │   ├── ProspectoDao.kt        # Consultas para insertar y leer datos
│       │   │   │   ├── ProspectoEntidad.kt    # Entidad de la tabla de prospectos
│       │   │   │   └── ProspectoRepositorio.kt # Centraliza las operaciones de datos
│       │   │   │
│       │   │   ├── interfaz/                  # Vistas del flujo de captadores
│       │   │   │   ├── PantallaLogin.kt       # Autenticación y acceso al módulo admin
│       │   │   │   ├── PantallaInicio.kt      # Panel principal del captador
│       │   │   │   ├── PantallaFormulario.kt  # Formulario por pasos para prospectos
│       │   │   │   ├── PantallaRegistro.kt    # Consulta y seguimiento de registros
│       │   │   │   ├── PantallaRegistroCaptador.kt # Registro de perfiles de captadores
│       │   │   │   ├── ComponentesComunes.kt  # Botones, campos y elementos reutilizables
│       │   │   │   └── ProspecctoViewModel.kt # Estado y lógica de la interfaz
│       │   │   │
│       │   │   ├── interfazAdmin/             # Módulo de administración
│       │   │   │   ├── InterfazAdmin.kt      # Navegación principal del panel admin
│       │   │   │   ├── PantallaAdmin.kt      # Inicio de sesión de administradores
│       │   │   │   ├── PanelAdministrativo.kt # Dashboard y accesos gerenciales
│       │   │   │   └── ListadoMaestroProspectos.kt # Listado global de prospectos
│       │   │   │
│       │   │   ├── ui/theme/                  # Diseño visual de Material 3
│       │   │   │   ├── Color.kt               # Paleta de colores
│       │   │   │   ├── Theme.kt               # Configuración de temas
│       │   │   │   └── Type.kt                # Tipografías y estilos de texto
│       │   │   │
│       │   │   └── MainActivity.kt            # Actividad principal y navegación
│       │   └── res/                           # Recursos estáticos
│       ├── androidTest/                       # Pruebas instrumentadas
│       └── test/                              # Pruebas unitarias
├── build.gradle.kts                           # Configuración de Gradle
├── settings.gradle.kts                        # Configuración de módulos
└── README.md                                  # Documentación del proyecto
```

## 📱 Explicación detallada de las pantallas

La aplicación divide su experiencia de usuario en dos módulos principales, según el rol del usuario autenticado.

### 1. Módulo de autenticación y captación

- **`PantallaLogin` (`PantallaLogin.kt`):** puerta de entrada a la aplicación. Incluye campos para usuario y contraseña, validación de credenciales, opción para mostrar u ocultar la contraseña y un botón **“Soy administrador”** que lleva al portal gerencial.
- **`PantallaInicio` (`PantallaInicio.kt`):** menú principal para captadores autenticados. Permite iniciar un nuevo registro o consultar el estado de la captación.
- **`PantallaFormulario` (`PantallaFormulario.kt`):** flujo guiado por pasos para ingresar los datos personales del prospecto —nombre, correo y teléfono— y sus preferencias académicas —nivel educativo, área de interés, curso y modalidad—.
- **`PantallaRegistro` (`PantallaRegistro.kt`):** muestra los prospectos almacenados localmente y permite revisar el estado de cada contacto.
- **`PantallaRegistroCaptador` (`PantallaRegistroCaptador.kt`):** vista auxiliar para registrar nuevos operadores o cuentas de captación en terreno.

### 2. Módulo de administración

- **`PantallaAdmin` (`PantallaAdmin.kt`):** pantalla de autenticación para administradores, destinada a restringir el acceso a las herramientas gerenciales.
- **`PanelAdministrativo` (`PanelAdministrativo.kt`):** centro de control que presenta tarjetas interactivas y accesos a funciones de gestión, reportes y parámetros de seguridad.
- **`ListadoMaestroProspectos` (`ListadoMaestroProspectos.kt`):** muestra el conjunto de prospectos registrados por los distintos captadores, facilitando la revisión y el análisis de la campaña.

## 🚀 Requisitos de ejecución

- [Android Studio](https://developer.android.com/studio) (se recomienda una versión compatible con la configuración del proyecto).
- Android SDK configurado según las versiones definidas en el proyecto.
- Dispositivo Android o emulador compatible.

## ▶️ Instalación y configuración

1. **Clona el repositorio:**

   ```bash
   git clone URL_DEL_REPOSITORIO
   ```

2. Abre la carpeta del proyecto desde Android Studio.
3. Espera a que Gradle sincronice las dependencias.
4. Selecciona un emulador o conecta un dispositivo Android.
5. Presiona **Run (▶)** para compilar y ejecutar la aplicación.

> **Nota:** reemplaza `URL_DEL_REPOSITORIO` por la URL real del repositorio de GitHub.

## 🔑 Credenciales de acceso de demostración

Credenciales de ejemplo para probar los distintos roles:

- **Administrador**
  - Usuario: `admin`
  - Contraseña: `admin123`
- **Captadores en terreno**
  - Usuario: `juan`
  - Contraseña: `1234`
  - Usuario: `captador`
  - Contraseña: `2026`

> **Importante:** estas credenciales son únicamente para demostración. No utilices contraseñas de ejemplo en un entorno real ni publiques credenciales reales en un repositorio público.

## 👥 Integrantes del equipo

- **Denilson Moya**
- **Juan Roldan**
- **Benjamin Catalan**
- **Camilo Ambrosetti**

## 📚 Información académica

- **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles
- **Institución:** Duoc UC
- **Proyecto:** TherionLabs App

---

**TherionLabs** · Innovación en la captación y gestión académica
