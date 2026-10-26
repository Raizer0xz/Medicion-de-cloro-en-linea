package com.example.medicindecloroenlnea.navigation

sealed class NavigationEvent {
    data class NavigateTo(
        val route: AppRoutes,
        val popUpToRoute: AppRoutes? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false
    ) : NavigationEvent()

    object PopBackStack : NavigationEvent()
    object NavigateUp : NavigationEvent()
}