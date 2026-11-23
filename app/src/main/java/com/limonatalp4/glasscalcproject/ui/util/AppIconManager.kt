package com.limonatalp4.glasscalcproject.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.limonatalp4.glasscalcproject.data.model.AppIconOption

object AppIconManager {

    /**
     * Aktiviert das gewählte <activity-alias> zuerst und deaktiviert anschließend alle übrigen,
     * sodass zu keinem Zeitpunkt alle Launcher-Einträge deaktiviert sind.
     */
    fun applyAppIcon(context: Context, selectedIcon: AppIconOption) {
        val packageManager = context.packageManager ?: return

        val targetComponent = ComponentName(context, selectedIcon.aliasClassName)
        runCatching {
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
        }

        AppIconOption.entries.forEach { option ->
            if (option != selectedIcon) {
                val component = ComponentName(context, option.aliasClassName)
                runCatching {
                    if (packageManager.getComponentEnabledSetting(component) !=
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    ) {
                        packageManager.setComponentEnabledSetting(
                            component,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }
            }
        }
    }
}
