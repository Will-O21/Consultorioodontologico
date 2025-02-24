"# ConsultorioOdontologico" 

Consultorio Odontológico
Este proyecto es una aplicación Android desarrollada en Kotlin para la gestión integral de un consultorio odontológico. Permite administrar pacientes, citas, pagos, historias clínicas interactivas, y cuenta con integración con Google Calendar y notificaciones push. La aplicación está diseñada para funcionar tanto en línea como fuera de línea, utilizando una base de datos local con Room.

Tabla de Contenidos
Características

Tecnologías Utilizadas

Requisitos del Sistema

Configuración del Proyecto

Estructura del Proyecto

Funcionalidades Detalladas

Próximos Pasos

Cómo Contribuir

Licencia

Características
Gestión de Pacientes: Registro, edición y eliminación de pacientes con datos como nombre, apellido, edad, hasta 4 fotos, y una historia clínica interactiva.

Historia Clínica Interactiva: Modelo dental gráfico donde se pueden marcar afecciones con una paleta de colores personalizable.

Gestión de Citas: Programación, modificación y cancelación de citas, con integración con Google Calendar.

Notificaciones Push: Recordatorios de citas mediante Firebase Cloud Messaging (FCM).

Gestión de Pagos: Registro de pagos, deudas y abonos por paciente.

Funcionamiento Offline: Uso de Room Database para almacenamiento local y sincronización en segundo plano cuando hay conexión.

Diseño Minimalista: Interfaz de usuario basada en un logo proporcionado, con colores y estilos personalizados.

Tecnologías Utilizadas
Lenguaje: Kotlin

Base de Datos: Room

Navegación: Jetpack Navigation

Interfaz de Usuario: Jetpack Compose, Material Design 3

Notificaciones: Firebase Cloud Messaging (FCM)

Integración con Google Calendar: Google Calendar API

Cámara: CameraX

Networking: Retrofit, Gson

Autenticación: Firebase Authentication

Gráficos: MPAndroidChart

Testing: JUnit, Espresso, MockK, Turbine

Requisitos del Sistema
Android Studio: Versión Flamingo o superior.

JDK: Versión 11 o superior.

Dispositivo Android: API 21 (Android 5.0) o superior.

Conexión a Internet: Para integración con Google Calendar y Firebase.

Configuración del Proyecto
Sigue estos pasos para configurar y ejecutar el proyecto en tu entorno local:

Clona el Repositorio:

bash
Copy
git clone https://github.com/Will-O21/Consultorioodontologico.git
Abre el Proyecto en Android Studio:

Abre Android Studio y selecciona "Open an Existing Project".

Navega hasta la carpeta del proyecto y selecciónala.

Configura las Credenciales de Firebase:

Descarga el archivo google-services.json desde Firebase Console y colócalo en la carpeta app/.

Configura las Credenciales de Google Calendar:

Habilita la API de Google Calendar en Google Cloud Console.

Configura las credenciales OAuth 2.0 para Android.

Sincroniza el Proyecto con Gradle:

Android Studio sincronizará automáticamente las dependencias del proyecto. Si no lo hace, haz clic en "Sync Now" en la barra de notificaciones.

Ejecuta la Aplicación:

Conecta un dispositivo Android o inicia un emulador.

Haz clic en el botón "Run" (▶️) en Android Studio para compilar y ejecutar la aplicación.

Estructura del Proyecto
El proyecto está organizado en los siguientes paquetes y módulos:

Copy
Consultorioodontologico/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com.wdog.consultorioodontologico/
│   │   │   │       └── MainActivity.kt
│   │   │   ├── kotlin/
│   │   │   │   └── com/wdog/consultorioodontologico/
│   │   │   │       ├── dao/
│   │   │   │       │   ├── Pacientedao.kt
│   │   │   │       │   └── citaDao.kt
│   │   │   │       ├── database/
│   │   │   │       │   ├── Appdatabase.kt
│   │   │   │       │   └── Converters.kt
│   │   │   │       ├── entities/
│   │   │   │       │   ├── Cita.kt
│   │   │   │       │   └── Paciente.kt
│   │   │   │       ├── navigation/
│   │   │   │       │   └── AppNavigation.kt
│   │   │   │       ├── ui/
│   │   │   │       │   ├── citas/
│   │   │   │       │   │   ├── PantallaAgregarCita.kt
│   │   │   │       │   │   └── PantallaCitas.kt
│   │   │   │       │   ├── inicio/
│   │   │   │       │   │   └── PantallaInicio.kt
│   │   │   │       │   ├── pacientes/
│   │   │   │       │   │   └── PantallaPacientes.kt
│   │   │   │       │   ├── pagos/
│   │   │   │       │   │   └── PantallaPagos.kt
│   │   │   │       │   └── registro/
│   │   │   │       │       └── PantallaRegistro.kt
│   │   │   │       └── theme/
│   │   │   │           └── Theme.kt
│   │   │   └── res/
│   │   │       └── (recursos de la aplicación)
│   │   └── (otros archivos)
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
Funcionalidades Detalladas
1. Registro de Pacientes
Formulario: Campos para nombre, apellido, edad, y hasta 4 fotos.

Historia Clínica Interactiva: Modelo dental gráfico con paleta de colores personalizable para marcar afecciones.

Almacenamiento: Datos guardados en Room Database.

2. Gestión de Citas
Integración con Google Calendar: Sincronización de citas agendadas.

Notificaciones Push: Recordatorios de citas mediante Firebase Cloud Messaging.

3. Gestión de Pagos
Registro de Pagos: Métodos de pago, abonos y deudas.

Reportes: Exportación de reportes en PDF.

4. Funcionamiento Offline
Sincronización en Segundo Plano: Uso de WorkManager para sincronizar datos cuando hay conexión.

Próximos Pasos
Implementar Autenticación: Añadir inicio de sesión seguro para el usuario principal (odontólogo).

Mejorar la Interfaz: Optimizar la UI/UX basada en feedback.

Pruebas y Depuración: Realizar pruebas unitarias y de UI para garantizar la calidad del código.

Despliegue: Preparar la aplicación para su publicación en Google Play Store.

Cómo Contribuir
¡Las contribuciones son bienvenidas! Si deseas contribuir a este proyecto, sigue estos pasos:

Haz un fork del repositorio.

Crea una rama para tu contribución:

bash
Copy
git checkout -b nombre-de-tu-rama
Realiza tus cambios y haz commit:

bash
Copy
git commit -m "Descripción de tus cambios"
Envía tus cambios:

bash
Copy
git push origin nombre-de-tu-rama
Abre un Pull Request en GitHub.

Licencia
Este proyecto está bajo la licencia MIT.