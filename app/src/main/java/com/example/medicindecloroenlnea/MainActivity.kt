package com.example.medicindecloroenlnea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medicindecloroenlnea.navigation.AppRoutes
import com.example.medicindecloroenlnea.navigation.NavigationEvent
import com.example.medicindecloroenlnea.ui.screens.PuntosScreen
//import com.example.medicindecloroenlnea.ui.screens.ResumenScreen
import com.example.medicindecloroenlnea.ui.theme.MediciónDeCloroEnLíneaTheme
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import com.example.medicindecloroenlnea.ui.screens.PuntosHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediciónDeCloroEnLíneaTheme {
                val viewModel: MainViewModel = viewModel()
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    viewModel.navigationEvents.collectLatest { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                val destino = (event.route as? AppRoutes.Detalle)?.buildRoute()
                                    ?: event.route.route
                                navController.navigate(destino) {
                                    event.popUpToRoute?.let {
                                        popUpTo(it.route) { inclusive = event.inclusive }
                                    }
                                    launchSingleTop = event.singleTop
                                    restoreState = true
                                }
                            }
                            is NavigationEvent.PopBackStack -> navController.popBackStack()
                            is NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.Puntos.route
                ) {
                    composable(AppRoutes.Puntos.route) { PuntosScreen() }
                    //composable(AppRoutes.Resumen.route) { ResumenScreen() }
                    composable(AppRoutes.Puntos.route) { PuntosHomeScreen(viewModel) }
                }
            }
        }
    }
}