package com.example.medicindecloroenlnea.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.medicindecloroenlnea.R

object NotificacionesCloro {
    private const val CHANNEL_ID = "alertas_cloro"

    fun notificar(context: Context, id: Int, detalle: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de cloro",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos cuando una medición de cloro sale del rango normal."
                }
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Medición de cloro fuera de rango")
            .setContentText(detalle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    @Composable
    fun rememberNotificador(): (Int, String) -> Unit {
        val context = LocalContext.current
        var pendingId by rememberSaveable { mutableIntStateOf(0) }
        var pendingDetail by rememberSaveable { mutableStateOf("") }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && pendingDetail.isNotEmpty()) {
                notificar(context, pendingId, pendingDetail)
            }
            pendingDetail = ""
        }

        return { id, detail ->
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingId = id
                pendingDetail = detail
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificar(context, id, detail)
            }
        }
    }
}
