package com.example.medicindecloroenlnea

import android.app.Application
import com.example.medicindecloroenlnea.data.repository.LocalCloroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class YourApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val repository = LocalCloroRepository(this)
        applicationScope.launch {
            while (isActive) {
                delay(INTERVALO_SIMULACION_MS)
                repository.simularMediciones()
            }
        }
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }

    private companion object {
        const val INTERVALO_SIMULACION_MS = 5 * 60 * 1000L
    }
}
