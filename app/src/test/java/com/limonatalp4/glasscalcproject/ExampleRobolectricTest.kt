package com.limonatalp4.glasscalcproject

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GlassCalc", appName)
  }

  @Test
  fun `haptic feedback is enabled by default in preferences`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences(context)
    val defaultHaptic = prefs.hapticFeedbackEnabledFlow.first()
    assertEquals(true, defaultHaptic)

    prefs.setHapticFeedbackEnabled(false)
    val updatedHaptic = prefs.hapticFeedbackEnabledFlow.first()
    assertEquals(false, updatedHaptic)
  }
}
