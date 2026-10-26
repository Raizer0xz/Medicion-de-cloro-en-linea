package com.example.medicindecloroenlnea.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medicindecloroenlnea.ui.screens.InicioScreen
import com.example.medicindecloroenlnea.ui.screens.InicioSesionScreen
import com.example.medicindecloroenlnea.ui.screens.RegistroScreen
import com.example.medicindecloroenlnea.ui.screens.ResumenScreen
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun AppNavigation(usuarioViewModel: UsuarioViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.INICIO_SESION
    ) {
        composable(AppRoutes.INICIO_SESION) {
            InicioSesionScreen(
                usuarioViewModel = usuarioViewModel,
                onInicioExitoso = {
                    navController.navigate(AppRoutes.INICIO) {
                        popUpTo(AppRoutes.INICIO_SESION) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrearCuenta = {
                    navController.navigate(AppRoutes.REGISTRO)
                }
            )
        }
        composable(AppRoutes.REGISTRO) {
            RegistroScreen(
                usuarioViewModel = usuarioViewModel,
                onRegistroExitoso = {
                    navController.navigate(AppRoutes.RESUMEN) {
                        launchSingleTop = true
                    }
                },
                onIniciarSesion = {
                    navController.popBackStack(
                        route = AppRoutes.INICIO_SESION,
                        inclusive = false
                    )
                }
            )
        }
        composable(AppRoutes.RESUMEN) {
            ResumenScreen(
                usuarioViewModel = usuarioViewModel,
                onIrAlInicio = {
                    if (!navController.popBackStack(AppRoutes.INICIO, inclusive = false)) {
                        navController.navigate(AppRoutes.INICIO) {
                            popUpTo(AppRoutes.INICIO_SESION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(AppRoutes.INICIO) {
            InicioScreen(
                usuarioViewModel = usuarioViewModel,
                onVerPerfil = { navController.navigate(AppRoutes.RESUMEN) },
                onCerrarSesion = {
                    navController.navigate(AppRoutes.INICIO_SESION) {
                        popUpTo(AppRoutes.INICIO) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
