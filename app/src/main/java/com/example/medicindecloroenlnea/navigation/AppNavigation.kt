package com.example.medicindecloroenlnea.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medicindecloroenlnea.ui.screens.HistorialScreen
import com.example.medicindecloroenlnea.ui.screens.GestionPuntosScreen
import com.example.medicindecloroenlnea.ui.screens.AlertasScreen
import com.example.medicindecloroenlnea.ui.screens.InicioSesionScreen
import com.example.medicindecloroenlnea.ui.screens.PerfilScreen
import com.example.medicindecloroenlnea.ui.screens.PuntosScreen
import com.example.medicindecloroenlnea.ui.screens.RegistroScreen
import com.example.medicindecloroenlnea.ui.screens.ResumenScreen
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    usuarioViewModel: UsuarioViewModel,
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val usuario by usuarioViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val sectionRoutes = listOf(AppRoutes.Puntos, AppRoutes.Alertas, AppRoutes.Perfil)
    val isAuthenticatedSection = sectionRoutes.any { it.route == currentRoute }

    LaunchedEffect(mainViewModel, navController) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    val destination = (event.route as? AppRoutes.Detalle)?.buildRoute()
                        ?: event.route.route
                    navController.navigate(destination) {
                        event.popUpToRoute?.let {
                            popUpTo(it.route) { inclusive = event.inclusive }
                        }
                        launchSingleTop = event.singleTop
                        restoreState = true
                    }
                }

                NavigationEvent.PopBackStack -> navController.popBackStack()
                NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isAuthenticatedSection,
        drawerContent = {
            if (isAuthenticatedSection) {
                ModalDrawerSheet {
                    Text(
                        text = "Medición de cloro",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                    sectionRoutes.forEach { route ->
                        NavigationDrawerItem(
                            label = { Text(route.label) },
                            selected = currentRoute == route.route,
                            icon = {
                                Icon(
                                    imageVector = route.icon,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                navigateToSection(navController, route)
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(
                                androidx.compose.material3.NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isAuthenticatedSection) {
                    TopAppBar(
                        title = {
                            Text(sectionRoutes.first { it.route == currentRoute }.label)
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Abrir menú de navegación"
                                )
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    usuarioViewModel.cerrarSesion()
                                    navController.navigate(AppRoutes.Login.route) {
                                        popUpTo(AppRoutes.Puntos.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text("Cerrar sesión")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isAuthenticatedSection) {
                    NavigationBar {
                        sectionRoutes.forEach { route ->
                            NavigationBarItem(
                                selected = currentRoute == route.route,
                                onClick = { navigateToSection(navController, route) },
                                icon = {
                                    Icon(
                                        imageVector = route.icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(route.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppRoutes.Login.route) {
                    InicioSesionScreen(
                        usuarioViewModel = usuarioViewModel,
                        onInicioExitoso = {
                            navController.navigate(AppRoutes.Puntos.route) {
                                popUpTo(AppRoutes.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onCrearCuenta = {
                            navController.navigate(AppRoutes.Registro.route)
                        }
                    )
                }
                composable(AppRoutes.Registro.route) {
                    RegistroScreen(
                        usuarioViewModel = usuarioViewModel,
                        onRegistroExitoso = {
                            navController.navigate(AppRoutes.ResumenRegistro.route) {
                                launchSingleTop = true
                            }
                        },
                        onIniciarSesion = {
                            navController.popBackStack(
                                route = AppRoutes.Login.route,
                                inclusive = false
                            )
                        }
                    )
                }
                composable(AppRoutes.ResumenRegistro.route) {
                    ResumenScreen(
                        usuarioViewModel = usuarioViewModel,
                        onIrAlInicio = {
                            navController.navigate(AppRoutes.Puntos.route) {
                                popUpTo(AppRoutes.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(AppRoutes.Puntos.route) {
                    PuntosScreen(
                        usuario = usuario,
                        onIrAlHistorial = { puntoId ->
                            navController.navigate(
                                AppRoutes.Detalle(puntoId.toString()).buildRoute()
                            )
                        },
                        onGestionarPuntos = {
                            navController.navigate(AppRoutes.GestionPuntos.route)
                        }
                    )
                }
                composable(AppRoutes.GestionPuntos.route) {
                    GestionPuntosScreen(
                        usuario = usuario,
                        onVolver = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.Alertas.route) {
                    AlertasScreen(usuario = usuario)
                }
                composable(AppRoutes.Perfil.route) {
                    PerfilScreen(usuarioViewModel = usuarioViewModel)
                }
                composable(
                    route = RUTA_DETALLE,
                    arguments = listOf(navArgument("puntoId") { type = NavType.IntType })
                ) { entry ->
                    val puntoId = entry.arguments?.getInt("puntoId")
                        ?: return@composable
                    HistorialScreen(
                        puntoId = puntoId,
                        usuario = usuario,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun navigateToSection(
    navController: NavHostController,
    route: AppRoutes
) {
    navController.navigate(route.route) {
        popUpTo(AppRoutes.Puntos.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private val AppRoutes.label: String
    get() = when (this) {
        AppRoutes.Puntos -> "Puntos"
        AppRoutes.Alertas -> "Alertas"
        AppRoutes.Perfil -> "Perfil"
        else -> route
    }

private val AppRoutes.icon
    get() = when (this) {
        AppRoutes.Puntos -> Icons.Default.Home
        AppRoutes.Alertas -> Icons.Default.Notifications
        AppRoutes.Perfil -> Icons.Default.Person
        else -> Icons.Default.Home
    }
