package com.limonatalp4.glasscalcproject

import android.app.Application
import android.util.Log

class GlassCalcApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Setup global safety uncaught exception logging to prevent silent crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GlassCalc", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // mXparser Dual License Non-Commercial Confirmation
        try {
            org.mariuszgromada.math.mxparser.License.iConfirmNonCommercialUse("limonatalp4")
        } catch (e: Exception) {
            Log.w("GlassCalc", "Could not set mXparser license confirmation: ${e.message}")
        }
    }
}
