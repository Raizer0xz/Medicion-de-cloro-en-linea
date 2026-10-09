package com.example.medicindecloroenlnea.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.navigation.AppRoutes
import com.example.medicindecloroenlnea.navigation.NavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    fun navigateTo(
        route: AppRoutes,
        popUpToRoute: AppRoutes? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateTo(route, popUpToRoute, inclusive, singleTop))
        }
    }

    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }
}