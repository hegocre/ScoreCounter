package es.hegocre.scorecounter.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.MaterialTheme
import es.hegocre.scorecounter.ScoreViewModel
import es.hegocre.scorecounter.ui.components.ScoreList

class MainActivity : ComponentActivity() {
    private var scoreViewModel: ScoreViewModel? = null
    private val offset = mutableStateListOf(0, 0)
    private var isAmbient by mutableStateOf(false)

    private lateinit var ambientObserver: AmbientLifecycleObserver
    private var needsBurnProtect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        val scoreViewModel by viewModels<ScoreViewModel>()
        this.scoreViewModel = scoreViewModel

        setTheme(android.R.style.Theme_DeviceDefault)
        ambientObserver = AmbientLifecycleObserver(this, ambientCallback)
        lifecycle.addObserver(ambientObserver)

        setContent {
            MaterialTheme {
                val contentColor by animateColorAsState(
                    targetValue = if (isAmbient) Color.White else Color.LightGray,
                    label = "Ambient mode set"
                )
                CompositionLocalProvider(
                    LocalContentColor provides contentColor
                ) {
                    ScoreList(
                        scoreViewModel = scoreViewModel,
                        modifier = Modifier.offset { IntOffset(offset[0], offset[1]) }
                    )
                }
            }
        }
    }

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isAmbient = true
            needsBurnProtect = ambientDetails.burnInProtectionRequired
        }

        override fun onExitAmbient() {
            isAmbient = false
            offset[0] = 0
            offset[1] = 0
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
            if (needsBurnProtect) {
                offset[0] = (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX).toInt()
                offset[1] = (Math.random() * 2 * BURN_IN_OFFSET_PX - BURN_IN_OFFSET_PX).toInt()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                event?.startTracking()
                true
            }

            else -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                if ((event?.flags ?: 0) and KeyEvent.FLAG_CANCELED_LONG_PRESS == 0) {
                    scoreViewModel?.inc(0)
                    vibrate()
                }
                true
            }

            else -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                scoreViewModel?.inc(1)
                vibrate()
                true
            }

            else -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    @Suppress("deprecation")
    private fun Context.vibrate() {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    250,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    companion object {
        private const val BURN_IN_OFFSET_PX = 10
    }
}