package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.components.CampoValidado
import com.example.medicindecloroenlnea.domain.model.ValidacionFormulario
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun InicioSesionScreen(
    usuarioViewModel: UsuarioViewModel,
    onInicioExitoso: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorClave by rememberSaveable { mutableStateOf<String?>(null) }
    var errorOperacion by rememberSaveable { mutableStateOf<String?>(null) }
    var iniciandoSesion by rememberSaveable { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Medición de cloro en línea",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Inicia sesión con una cuenta registrada en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium
            )
            CampoValidado(
                value = correo,
                onValueChange = {
                    correo = it
                    errorCorreo = null
                    errorClave = null
                    errorOperacion = null
                },
                label = "Correo electrónico",
                leadingIcon = Icons.Default.Email,
                errorMessage = errorCorreo,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            CampoValidado(
                value = clave,
                onValueChange = {
                    clave = it
                    errorCorreo = null
                    errorClave = null
                    errorOperacion = null
                },
                label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                errorMessage = errorClave,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            Button(
                onClick = {
                    val correoValido = ValidacionFormulario.validarCorreo(correo)
                    val claveValida = ValidacionFormulario.validarClave(clave)
                    errorCorreo = if (correoValido) {
                        null
                    } else {
                        ValidacionFormulario.MENSAJE_CORREO_INVALIDO
                    }
                    errorClave = if (claveValida) {
                        null
                    } else {
                        ValidacionFormulario.MENSAJE_CLAVE_VACIA
                    }
                    if (correoValido && claveValida && !iniciandoSesion) {
                        iniciandoSesion = true
                        usuarioViewModel.iniciarSesion(correo, clave) { autenticado, error ->
                            iniciandoSesion = false
                            if (autenticado) {
                                errorCorreo = null
                                errorClave = null
                                errorOperacion = null
                                onInicioExitoso()
                            } else if (error != null) {
                                errorOperacion = error
                            } else {
                                val errorCredenciales =
                                    "Correo o contraseña incorrectos. Revisa ambos campos."
                                errorCorreo = errorCredenciales
                                errorClave = errorCredenciales
                            }
                        }
                    }
                },
                enabled = !iniciandoSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (iniciandoSesion) {
                    CircularProgressIndicator()
                } else {
                    Text("Iniciar sesión")
                }
            }
            errorOperacion?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            TextButton(
                onClick = onCrearCuenta,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }
        }
    }
}
