package com.limonatalp4.glasscalcproject.lifecycle

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLifecycleObserver : DefaultLifecycleObserver {

    private val _isForeground = MutableStateFlow(true)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    private val _lastLifecycleEvent = MutableStateFlow("INITIALIZED")
    val lastLifecycleEvent: StateFlow<String> = _lastLifecycleEvent.asStateFlow()

    private var onStopCallback: (() -> Unit)? = null
    private var onStartCallback: (() -> Unit)? = null
    private var isRegistered = false

    fun initialize(onStop: () -> Unit, onStart: () -> Unit) {
        this.onStopCallback = onStop
        this.onStartCallback = onStart
        if (!isRegistered) {
            try {
                ProcessLifecycleOwner.get().lifecycle.addObserver(this)
                isRegistered = true
                Log.d("AppLifecycleObserver", "ProcessLifecycleOwner registered successfully.")
            } catch (t: Throwable) {
                Log.w("AppLifecycleObserver", "Could not register ProcessLifecycleOwner, fallback active: ${t.message}")
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        _isForeground.value = true
        _lastLifecycleEvent.value = "ON_START"
        onStartCallback?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _isForeground.value = false
        _lastLifecycleEvent.value = "ON_STOP"
        onStopCallback?.invoke()
    }

    fun simulateBackground() {
        _isForeground.value = false
        _lastLifecycleEvent.value = "ON_STOP (Simuliert)"
        onStopCallback?.invoke()
    }

    fun simulateForeground() {
        _isForeground.value = true
        _lastLifecycleEvent.value = "ON_START (Simuliert)"
        onStartCallback?.invoke()
    }
}
