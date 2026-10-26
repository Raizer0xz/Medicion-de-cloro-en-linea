package com.example.medicindecloroenlnea.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medicindecloroenlnea.ui.screens.PuntosHomeScreen
import com.example.medicindecloroenlnea.ui.screens.InicioSesionScreen
import com.example.medicindecloroenlnea.ui.screens.RegistroScreen
import com.example.medicindecloroenlnea.ui.screens.ResumenScreen
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation(
    usuarioViewModel: UsuarioViewModel,
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()

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

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Login.route
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
            PuntosHomeScreen(
                usuarioViewModel = usuarioViewModel,
                onCerrarSesion = {
                    navController.navigate(AppRoutes.Login.route) {
                        popUpTo(AppRoutes.Puntos.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
