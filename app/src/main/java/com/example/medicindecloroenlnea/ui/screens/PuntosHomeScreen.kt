package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.navigation.AppRoutes
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuntosHomeScreen(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val itemPadding = NavigationDrawerItemDefaults.ItemPadding

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Cloro en línea",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                NavigationDrawerItem(
                    label = { Text("Puntos") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(itemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Alertas") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(AppRoutes.Alertas, singleTop = true)
                    },
                    modifier = Modifier.padding(itemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Resumen") },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(AppRoutes.Resumen, singleTop = true)
                    },
                    modifier = Modifier.padding(itemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Puntos de medición") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) { PuntosScreen() }
        }
    }
}