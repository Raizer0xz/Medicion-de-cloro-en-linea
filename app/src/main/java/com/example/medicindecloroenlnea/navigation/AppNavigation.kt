package com.example.medicindecloroenlnea.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medicindecloroenlnea.ui.screens.RegistroScreen
import com.example.medicindecloroenlnea.ui.screens.ResumenScreen
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun AppNavigation(usuarioViewModel: UsuarioViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.REGISTRO
    ) {
        composable(AppRoutes.REGISTRO) {
            RegistroScreen(
                usuarioViewModel = usuarioViewModel,
                onRegistroExitoso = {
                    navController.navigate(AppRoutes.RESUMEN) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoutes.RESUMEN) {
            ResumenScreen(
                usuarioViewModel = usuarioViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
