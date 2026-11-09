package com.limonatalp4.glasscalcproject

import android.app.Application

class GlassCalcApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // mXparser Dual License Non-Commercial Confirmation
        try {
            org.mariuszgromada.math.mxparser.License.iConfirmNonCommercialUse("limonatalp4")
        } catch (e: Exception) {
            // Ignore license confirmation failures in release
        }
    }
}
