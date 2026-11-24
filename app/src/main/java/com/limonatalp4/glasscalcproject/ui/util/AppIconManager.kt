package com.limonatalp4.glasscalcproject.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AppIconManager {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val iconSwitchLock = Any()

    /**
     * Schaltet den <activity-alias> asynchron im Hintergrund (Dispatchers.IO) um,
     * ohne den UI-Thread zu blockieren oder laufende Coroutine-Scopes der UI abzubrechen.
     */
    fun applyAppIconAsync(context: Context, selectedIcon: AppIconOption): Job {
        val appContext = context.applicationContext ?: context
        return backgroundScope.launch {
            applyAppIcon(appContext, selectedIcon)
        }
    }

    /**
     * Aktiviert das gewählte <activity-alias> zuerst und deaktiviert anschließend alle übrigen,
     * sodass zu keinem Zeitpunkt alle Launcher-Einträge deaktiviert sind.
     */
    fun applyAppIcon(context: Context, selectedIcon: AppIconOption) {
        val packageManager = context.packageManager ?: return

        synchronized(iconSwitchLock) {
            val targetComponent = ComponentName(context, selectedIcon.aliasClassName)
            try {
                val currentState = packageManager.getComponentEnabledSetting(targetComponent)
                val isAlreadyEnabled = currentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                    (selectedIcon == AppIconOption.CLASSIC && currentState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                if (!isAlreadyEnabled) {
                    packageManager.setComponentEnabledSetting(
                        targetComponent,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } catch (e: Exception) {
                Log.e("GlassCalc", "Fehler beim Aktivieren des App-Icons ${selectedIcon.name}", e)
            }

            AppIconOption.entries.forEach { option ->
                if (option != selectedIcon) {
                    val component = ComponentName(context, option.aliasClassName)
                    try {
                        if (packageManager.getComponentEnabledSetting(component) !=
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        ) {
                            packageManager.setComponentEnabledSetting(
                                component,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("GlassCalc", "Fehler beim Deaktivieren des App-Icons ${option.name}", e)
                    }
                }
            }
        }
    }
}
