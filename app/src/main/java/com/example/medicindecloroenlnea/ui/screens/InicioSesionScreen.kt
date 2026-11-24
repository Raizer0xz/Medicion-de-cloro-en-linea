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
                    errorCorreo = if (ValidacionFormulario.validarCorreo(correo)) {
                        null
                    } else {
                        ValidacionFormulario.MENSAJE_CORREO_INVALIDO
                    }
                    errorClave = if (ValidacionFormulario.validarClave(clave)) {
                        null
                    } else {
                        ValidacionFormulario.MENSAJE_CLAVE_VACIA
                    }
                    if (errorCorreo == null && errorClave == null) {
                        usuarioViewModel.iniciarSesion(correo, clave) { autenticado ->
                            if (autenticado) {
                                errorCorreo = null
                                errorClave = null
                                onInicioExitoso()
                            } else {
                                val errorCredenciales =
                                    "Correo o contraseña incorrectos. Revisa ambos campos."
                                errorCorreo = errorCredenciales
                                errorClave = errorCredenciales
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
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
