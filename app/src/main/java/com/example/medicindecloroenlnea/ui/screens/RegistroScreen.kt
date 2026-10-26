package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun RegistroScreen(
    usuarioViewModel: UsuarioViewModel,
    onRegistroExitoso: () -> Unit
) {
    val usuario by usuarioViewModel.uiState.collectAsState()
    val errores by usuarioViewModel.errores.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registro de usuario",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Usa datos ficticios para esta demostración.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = usuario.nombre,
                onValueChange = usuarioViewModel::actualizarNombre,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
                singleLine = true,
                isError = errores.nombre != null,
                supportingText = { errores.nombre?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = usuario.correo,
                onValueChange = usuarioViewModel::actualizarCorreo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = errores.correo != null,
                supportingText = { errores.correo?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = usuario.clave,
                onValueChange = usuarioViewModel::actualizarClave,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                isError = errores.clave != null,
                supportingText = { errores.clave?.let { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = usuario.direccion,
                onValueChange = usuarioViewModel::actualizarDireccion,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dirección") },
                singleLine = true,
                isError = errores.direccion != null,
                supportingText = { errores.direccion?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = usuario.aceptaTerminos,
                    onCheckedChange = usuarioViewModel::actualizarAceptaTerminos
                )
                Text(
                    text = "Acepto los términos de esta demostración.",
                    modifier = Modifier.clickable {
                        usuarioViewModel.actualizarAceptaTerminos(!usuario.aceptaTerminos)
                    }
                )
            }
            errores.aceptaTerminos?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    if (usuarioViewModel.validarFormulario()) {
                        onRegistroExitoso()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}
