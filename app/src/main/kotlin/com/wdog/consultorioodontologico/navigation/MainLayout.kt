package com.wdog.consultorioodontologico.navigation

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wdog.consultorioodontologico.sync.SyncManager
import com.wdog.consultorioodontologico.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    navController: NavHostController,
    onConfigClick: () -> Unit,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // --- 1. DEFINIMOS QUÉ SE MUESTRA EN CADA LUGAR ---

    // Solo estas pantallas permiten deslizar para abrir el menú lateral (Drawer)
    val pantallasConMenuLateral = listOf(
        AppNavigation.INICIO,
        AppNavigation.PACIENTES,
        AppNavigation.CITAS,
        AppNavigation.PRESUPUESTO,
        AppNavigation.FINANZAS,
        AppNavigation.LABORATORIOS,
        AppNavigation.INVENTARIO
    )

    // Estas pantallas mostrarán la barra inferior (incluimos Finanzas y Laboratorios)
    val pantallasConBarraInferior = listOf(
        AppNavigation.INICIO,
        AppNavigation.PACIENTES,
        AppNavigation.CITAS,
        AppNavigation.PRESUPUESTO,
        AppNavigation.FINANZAS,
        AppNavigation.LABORATORIOS,
        AppNavigation.INVENTARIO
    )

    val permiteMenu = rutaActual in pantallasConMenuLateral
    val mostrarBarra = rutaActual in pantallasConBarraInferior
    // El Dashboard no está en ninguna lista, por lo que saldrá "limpio".

    val estaSincronizado by SyncState.estaSincronizado.collectAsState(initial = true)

    // --- NUEVO: LÓGICA DE ANIMACIÓN DE LATIDO ---
    val infiniteTransition = rememberInfiniteTransition(label = "latidoSync")
    val escalaAnimada by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!estaSincronizado) 1.25f else 1f, // Solo pulsa si NO está sincronizado
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaIcono"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = permiteMenu, // Solo activo en las 4 principales
        drawerContent = {
            MenuLateral(navController, scope, drawerState, onConfigClick)
        }
    ) {
        Scaffold(
            topBar = {
                // Solo mostramos la TopBar si NO estamos en el Dashboard
                if (rutaActual != AppNavigation.DASHBOARD) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (rutaActual) {
                                    AppNavigation.FINANZAS -> ""
                                    AppNavigation.LABORATORIOS -> ""
                                    else -> "" // Inicio, Pacientes, etc., quedan vacíos como pediste
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (permiteMenu) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menú", tint = Color.White)
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                try {
                                    SyncManager.enqueue(context)
                                } catch (_: Exception) {}
                            }) {
                                Icon(
                                    imageVector = if (estaSincronizado) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = "Sync",
                                    tint = if (estaSincronizado) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    // --- NUEVO: APLICAMOS LA ESCALA ANIMADA ---
                                    modifier = Modifier.graphicsLayer(
                                        scaleX = if (!estaSincronizado) escalaAnimada else 1f,
                                        scaleY = if (!estaSincronizado) escalaAnimada else 1f
                                    )
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101084))
                    )
                }
            },
            bottomBar = {
                if (mostrarBarra) {
                    // --- NUEVO: COLUMNA PARA AGREGAR EL DIVISOR AJUSTABLE ---
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.55.dp, // <--- AJUSTA AQUÍ EL GROSOR DE LA LÍNEA
                            color = Color.LightGray.copy(alpha = 100f) // <--- AJUSTA AQUÍ EL COLOR
                        )
                        BarraInferior(navController, rutaActual)
                    }
                }
            }
        ) { innerPadding ->
            contenido(innerPadding)
        }
    }
}

@Composable
fun MenuLateral(navController: NavHostController, scope: CoroutineScope, drawerState: DrawerState, onConfigClick: () -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Menú Principal", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101084))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        NavigationDrawerItem(
            label = { Text("Finanzas y Reportes") },
            selected = false,
            icon = { Icon(Icons.Default.BarChart, null, tint = Color(0xFF094293)) },
            onClick = { scope.launch { drawerState.close() }; navController.navigate(AppNavigation.FINANZAS) {
                // Esto evita que se abran muchas copias de la misma pantalla
                launchSingleTop = true
                // Esto hace que al dar "Atrás" vuelvas al inicio y no a un bucle de menús
                popUpTo(AppNavigation.INICIO) { saveState = true }
            }},
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // 2. LABORATORIOS (Añadir este bloque nuevo)
        NavigationDrawerItem(
            label = { Text("Laboratorios, Proveedores y Técnicos") },
            selected = false,
            icon = { Icon(Icons.Default.Science, null, tint = Color(0xFF094293)) }, // Icono de ciencia/lab
            onClick = {
                scope.launch { drawerState.close() }
                // Asegúrate de tener AppNavigation.LABORATORIOS definidos en tu archivo de rutas
                navController.navigate(AppNavigation.LABORATORIOS){
                    // Esto evita que se abran muchas copias de la misma pantalla
                    launchSingleTop = true
                    // Esto hace que al dar "Atrás" vuelvas al inicio y no a un bucle de menús
                    popUpTo(AppNavigation.INICIO) { saveState = true }
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Inventario") },
            selected = false,
            icon = { Icon(Icons.Default.Inventory, null, tint = Color(0xFF094293)) },
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate(AppNavigation.INVENTARIO){
                    // Esto evita que se abran muchas copias de la misma pantalla
                    launchSingleTop = true
                    // Esto hace que al dar "Atrás" vuelvas al inicio y no a un bucle de menús
                    popUpTo(AppNavigation.INICIO) { saveState = true }
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Configuración") },
            selected = false,
            icon = { Icon(Icons.Default.Settings, null, tint = Color.Gray) },
            onClick = { scope.launch { drawerState.close() }; onConfigClick() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
fun BarraInferior(navController: NavHostController, rutaActual: String?) {
    NavigationBar(containerColor = Color.White) {
        val esRutaEspecial = rutaActual == AppNavigation.FINANZAS || rutaActual == AppNavigation.LABORATORIOS || rutaActual == AppNavigation.INVENTARIO
        val colorAcento = if (esRutaEspecial) Color(0xFF094293).copy(alpha = 0.5f) else Color.Gray
        val items = listOf(
            Triple("Inicio", AppNavigation.INICIO, Icons.Default.Home),
            Triple("Pacientes", AppNavigation.PACIENTES, Icons.Default.People),
            Triple("Citas", AppNavigation.CITAS, Icons.Default.DateRange),
            Triple("Presupuesto", AppNavigation.PRESUPUESTO, Icons.Default.Calculate)
        )
        items.forEach { (label, ruta, icono) ->
            NavigationBarItem(
                icon = { Icon(icono, null) },
                label = { Text(label) },
                selected = rutaActual == ruta,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF101084),
                    selectedTextColor = Color(0xFF101084),
                    unselectedIconColor = colorAcento, // Usamos la variable aquí
                    unselectedTextColor = colorAcento, // Usamos la variable aquí
                    indicatorColor = Color(0xFF101084).copy(alpha = 0.1f)
                ),
                onClick = {
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) {
                            popUpTo(AppNavigation.INICIO) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}