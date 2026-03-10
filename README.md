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
Arquitectura y Estructura del Proyecto
La aplicación está desarrollada utilizando una arquitectura Model-View-ViewModel (MVVM), lo que permite una clara separación de responsabilidades entre la lógica de negocio (ViewModel), la interfaz de usuario (UI) y los datos (Model). Se utiliza Room como base de datos local para almacenar pacientes, citas y notas. La interfaz de usuario está construida completamente con Jetpack Compose, lo que permite una experiencia moderna y reactiva. La navegación entre pantallas se gestiona mediante NavController y AppNavigation, y las operaciones asíncronas se manejan con corrutinas.

Funcionalidades Principales
1. Gestión de Pacientes
Registro de Pacientes: Permite registrar nuevos pacientes con campos como nombre, apellido, edad, fotos, observaciones y una historia clínica interactiva.

Subida de Fotos: Se pueden subir hasta 4 fotos usando ActivityResultContracts.GetContent().

Historia Clínica Interactiva: Un modelo dental gráfico permite marcar afecciones con una paleta de colores (rojo y amarillo). Los puntos de afección se guardan como coordenadas y colores en la base de datos.

Validaciones: Se realizan validaciones en los campos de nombre, apellido, edad, observaciones, monto y abono.

Edición de Pacientes: Permite editar la información de un paciente existente, incluyendo la historia clínica interactiva.

Listado de Pacientes: Muestra una lista de pacientes registrados, con opciones para ver detalles, editar o eliminar pacientes.

2. Gestión de Citas
Agendar Citas: Permite agendar nuevas citas, seleccionando un paciente, fecha, hora y observaciones.

Notificaciones Locales: Se programan notificaciones locales usando WorkManager para recordar las citas agendadas.

Validaciones: Se validan la fecha, hora, paciente seleccionado y observaciones.

Listado de Citas: Muestra una lista de citas agendadas, con opciones para ver detalles, editar o eliminar citas.

Detalle de Citas: Muestra los detalles de una cita y permite editar la fecha, hora y observaciones.

3. Gestión de Pagos
Registro de Pagos: Permite gestionar los pagos de los pacientes, incluyendo el estado de pago (Pendiente, Abonó, Completo), monto y abono.

Cálculo de Deudas: Se calcula automáticamente la deuda restante basada en el monto total y el abono.

Edición de Pagos: Permite editar el estado de pago, monto y abono de un paciente.

4. Gestión del Consultorio
Notas del Consultorio: Permite gestionar notas relacionadas con el consultorio, como fallas o necesidades. Las notas se pueden agregar, editar y eliminar.

Colores de Notas: Las notas pueden tener un color asociado (rojo, amarillo, verde) para indicar su prioridad.

5. Pantalla de Inicio
Navegación: Es la pantalla principal de la aplicación, con botones para navegar a las diferentes secciones (Registro, Pacientes, Citas, Pagos, Gestión del Consultorio).

Integración con Firebase
Firebase Messaging: Configurado para notificaciones push, aunque aún no se ha implementado completamente la lógica para enviar notificaciones desde Firebase.

Firebase Authentication: Configurado, pero aún no se ha implementado la autenticación del usuario principal (odontólogo).

Funcionamiento Offline
Room Database: La aplicación funciona completamente offline, utilizando Room para almacenar todos los datos localmente. Las operaciones de sincronización con Firebase aún no están implementadas.

Diseño y Estilo
Material Design 3: La aplicación utiliza Material Design 3 para la interfaz de usuario, con colores personalizados basados en el logo del consultorio.

Tema Personalizado: El tema de la aplicación está definido en Theme.kt, aunque aún no se han personalizado completamente los colores y estilos.

Requerimientos Originales Cubiertos
Registro de Pacientes: Campos para nombre, apellido, edad, fotos y observaciones. Historia clínica interactiva con modelo dental y paleta de colores.

Gestión de Citas: Agendar, editar y eliminar citas. Notificaciones locales para recordatorios de citas.

Gestión de Pagos: Registro de pagos, abonos y deudas. Cálculo automático de saldos pendientes.

Funcionamiento Offline: Uso de Room Database para almacenamiento local.

Diseño Minimalista: Interfaz de usuario basada en Material Design 3 con colores personalizados.

Lo que Falta
Integración con Google Calendar: Sincronización de citas con Google Calendar.

Notificaciones Push con Firebase: Lógica para enviar notificaciones push desde Firebase.

Autenticación del Usuario Principal: Autenticación del odontólogo usando Firebase Authentication.

Generación de Reportes en PDF: Exportación de reportes en PDF para los pagos.

Sincronización Offline-Online: Sincronización de datos con Firebase Firestore cuando hay conexión a internet.

Historia Clínica Interactiva Completa: Más colores para representar diferentes afecciones y mejorar la interacción con el modelo dental.

Testing: Pruebas unitarias y de UI exhaustivas.

Mejoras Sugeridas
Integración con Google Calendar: Sincronización de citas con Google Calendar.

Notificaciones Push: Implementar la lógica para enviar notificaciones push desde Firebase Cloud Messaging.

Autenticación Segura: Implementar la autenticación del odontólogo usando Firebase Authentication.

Generación de Reportes en PDF: Usar una librería como iTextPDF para generar reportes en PDF.

Mejoras en la Historia Clínica Interactiva: Agregar más colores y permitir zoom y desplazamiento.

Sincronización Offline-Online: Implementar la sincronización de datos con Firebase Firestore usando WorkManager.

Testing: Implementar pruebas unitarias y de UI.

Optimización del Código: Refactorizar el código para mejorar la legibilidad y el rendimiento.

Diseño y Experiencia de Usuario: Mejorar la UI/UX basada en feedback de usuarios.

¡Las contribuciones son bienvenidas! Si deseas contribuir a este proyecto.


Licencia
Este proyecto está bajo la licencia MIT.