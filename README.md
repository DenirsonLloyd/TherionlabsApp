<div align="center">

# 🎓 TherionLabs

### Aplicación móvil para el registro y seguimiento de prospectos académicos

Una solución móvil diseñada para facilitar el registro de contactos, organizar sus intereses académicos y consultar la información almacenada localmente.

</div>

---

## 📌 Descripción del proyecto

**TherionLabs** es una aplicación móvil Android orientada al registro de prospectos interesados en una oferta académica. Permite ingresar información personal y preferencias de estudio mediante una interfaz organizada, y consultar los registros guardados en el dispositivo.

El proyecto se desarrolla como parte de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**.

## 🎯 Objetivo

Ofrecer una experiencia sencilla e intuitiva para registrar prospectos, recopilar sus datos de contacto y organizar sus preferencias académicas, manteniendo la información disponible para su consulta.

## ✨ Funcionalidades

- 📝 **Registro de prospectos:** formulario organizado en pasos para ingresar datos personales y preferencias académicas.
- 👤 **Datos de contacto:** nombre, correo electrónico y teléfono.
- 📚 **Preferencias académicas:** nivel educativo, área de interés, curso y modalidad de estudio.
- ✅ **Validación de datos:** retroalimentación básica para el ingreso del correo electrónico.
- 💾 **Almacenamiento local:** persistencia de los registros mediante una base de datos Room.
- 📋 **Consulta de registros:** visualización de prospectos organizados según su estado.
- 🧭 **Navegación entre pantallas:** acceso a las secciones principales desde la interfaz de la aplicación.

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal de desarrollo |
| **Android Studio** | Entorno de desarrollo |
| **Jetpack Compose** | Construcción de interfaces de usuario |
| **Material 3** | Componentes y diseño visual |
| **Room** | Persistencia de datos local |
| **Kotlin Coroutines y Flow** | Operaciones asíncronas y observación de datos |
| **Gradle** | Configuración y construcción del proyecto |
| **Git y GitHub** | Control de versiones y colaboración |

## 🧱 Estructura del proyecto

```text
TherionlabsApp/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/dsy1105/therionlabsapp/
│       │   │   ├── datos/          # Entidades, DAO y acceso a datos
│       │   │   ├── interfaz/       # Pantallas y componentes de interfaz
│       │   │   ├── ui/theme/       # Tema visual de la aplicación
│       │   │   └── MainActivity.kt # Actividad principal
│       │   └── res/                # Recursos de Android
│       ├── androidTest/             # Pruebas instrumentadas
│       └── test/                    # Pruebas unitarias
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 Requisitos

Antes de ejecutar el proyecto, asegúrate de contar con:

- [Android Studio](https://developer.android.com/studio).
- Android SDK instalado y configurado.
- JDK compatible con la configuración de Gradle del proyecto.
- Un emulador Android o un dispositivo físico con depuración USB habilitada.
- Conexión a Internet para descargar las dependencias durante la primera sincronización.

## ▶️ Instalación y ejecución

1. **Clona el repositorio:**

   ```bash
   git clone URL_DEL_REPOSITORIO
   ```

2. **Ingresa a la carpeta del proyecto:**

   ```bash
   cd TherionlabsApp
   ```

3. **Abre Android Studio** y selecciona **Open**.
4. Selecciona la carpeta raíz del proyecto, donde se encuentra `settings.gradle.kts`.
5. Espera a que termine la sincronización de Gradle.
6. Selecciona un emulador o conecta un dispositivo Android.
7. Presiona **Run ▶** para compilar y ejecutar la aplicación.

> Reemplaza `URL_DEL_REPOSITORIO` por la dirección real del repositorio de GitHub.

## 🗃️ Datos almacenados

La información de cada prospecto contempla los siguientes datos:

- Identificador.
- Nombre.
- Correo electrónico.
- Teléfono.
- Nivel educativo.
- Área de interés.
- Curso de interés.
- Modalidad de estudio.
- Estado del registro.
- Fecha de creación.

Los datos se gestionan mediante almacenamiento local con **Room**, utilizando la base de datos `therionlabs_db`.

## 🤝 Trabajo colaborativo

El proyecto utiliza **Git y GitHub** para mantener el control de versiones y facilitar la colaboración entre los integrantes del equipo. La planificación y distribución de tareas se puede organizar mediante **Trello**.

## 👥 Integrantes

- **Integrante 1:** Denilson Moya
- **Integrante 2:** Juan Roldan
- **Integrante 3:** Benjamin Catalan
- **Integrante 4:** Camilo Ambrosetti

## 📚 Información académica

- **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles
- **Proyecto:** TherionLabs
- **Tipo de entrega:** Desarrollo de aplicación móvil Android

---

<div align="center">

**TherionLabs** · Registro y organización de prospectos académicos

</div>
