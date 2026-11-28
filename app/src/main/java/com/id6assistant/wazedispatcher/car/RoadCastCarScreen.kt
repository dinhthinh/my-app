package com.id6assistant.wazedispatcher.car

import android.graphics.Rect
import android.view.Surface
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.id6assistant.wazedispatcher.service.DispatcherRepository
import com.id6assistant.wazedispatcher.service.Id6DispatchService
import com.id6assistant.wazedispatcher.service.TelemetrySourceMode
import kotlinx.coroutines.launch

class RoadCastCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {
    private val renderer = CarHudRenderer()
    private var surface: Surface? = null
    private var visibleArea: Rect? = null
    private var lastRunningState: Boolean? = null
    private var currentModel: CarHudUiModel = CarHudUiModel.from(DispatcherRepository.state.value)

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            surface = surfaceContainer.surface
            renderSurface()
        }

        override fun onVisibleAreaChanged(visibleArea: Rect) {
            this@RoadCastCarScreen.visibleArea = Rect(visibleArea)
            renderSurface()
        }

        override fun onStableAreaChanged(stableArea: Rect) {
            renderSurface()
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            surface = null
        }
    }

    init {
        lifecycle.addObserver(this)
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                DispatcherRepository.state.collect { state ->
                    val nextModel = CarHudUiModel.from(state)
                    val runningChanged = lastRunningState != state.running
                    lastRunningState = state.running
                    currentModel = nextModel
                    if (runningChanged) {
                        invalidate()
                    }
                    renderSurface()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(null)
        surface = null
    }

    override fun onGetTemplate(): Template {
        val isRunning = DispatcherRepository.state.value.running
        val connectAction = Action.Builder()
            .setTitle(if (isRunning) "Disconnect" else "Connect WazeMod")
            .setOnClickListener {
                if (DispatcherRepository.state.value.running) {
                    Id6DispatchService.stop(carContext)
                } else {
                    DispatcherRepository.setSourceMode(TelemetrySourceMode.LIVE_AIDL)
                    Id6DispatchService.startOrReload(carContext)
                }
            }
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(connectAction)
                    .build(),
            )
            .build()
    }

    private fun renderSurface() {
        val targetSurface = surface ?: return
        if (!targetSurface.isValid) return
        val canvas = try {
            targetSurface.lockCanvas(null)
        } catch (_: Exception) {
            return
        }
        try {
            renderer.draw(canvas, visibleArea, currentModel)
        } finally {
            try {
                targetSurface.unlockCanvasAndPost(canvas)
            } catch (_: Exception) {
                // Surface may have been destroyed concurrently.
            }
        }
    }
}
