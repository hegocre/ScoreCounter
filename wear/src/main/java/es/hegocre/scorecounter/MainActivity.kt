package es.hegocre.scorecounter

import android.content.Context
import android.os.*
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.input.WearableButtons
import es.hegocre.scorecounter.data.Score
import es.hegocre.scorecounter.databinding.ActivityMainBinding

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private var needsBurnProtect = false

    private val buttonsAvailable = mutableListOf(false, false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Score.setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this))
        Score("score1").let { score ->
            binding.score1 = score
            loadScore(score, binding.add1Layout, binding.sub1Layout)
        }
        Score("score2").let { score ->
            binding.score2 = score
            loadScore(score, binding.add2Layout, binding.sub2Layout)
        }

        buttonsAvailable[0] = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1) != null
        buttonsAvailable[1] = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_2) != null
        buttonsAvailable[2] = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_3) != null

        ambientController = AmbientModeSupport.attach(this)
    }

    private fun loadScore(score: Score, addLayout: View, subLayout: View) {
        addLayout.setOnClickListener {
            score.inc()
        }
        addLayout.setOnLongClickListener {
            score.reset()
            true
        }
        subLayout.setOnClickListener {
            score.dec()
        }
        subLayout.setOnLongClickListener {
            score.reset()
            true
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {

        override fun onEnterAmbient(ambientDetails: Bundle?) {
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.black
                )
            )
            binding.divider.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.white
                )
            )

            needsBurnProtect =
                ambientDetails?.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false)
                    ?: false
            super.onEnterAmbient(ambientDetails)
        }

        override fun onExitAmbient() {
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.dark_grey
                )
            )
            binding.divider.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.grey
                )
            )
            if (needsBurnProtect) binding.root.setPadding(0, 0, 0, 0)
            super.onExitAmbient()
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
            if (needsBurnProtect) {
                val x =
                    (Math.random() * 2 * Companion.BURN_IN_OFFSET_PX - Companion.BURN_IN_OFFSET_PX).toInt()
                val y =
                    (Math.random() * 2 * Companion.BURN_IN_OFFSET_PX - Companion.BURN_IN_OFFSET_PX).toInt()
                binding.root.setPadding(x, y, 0, 0)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (event?.repeatCount ?: 0) {
            0 -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_STEM_1 -> {
                        binding.score1?.inc()
                        vibrate()
                        true
                    }
                    KeyEvent.KEYCODE_STEM_2 -> {
                        if (buttonsAvailable[0]) binding.score2?.inc()
                        else binding.score1?.inc()
                        vibrate()
                        true
                    }
                    KeyEvent.KEYCODE_STEM_3 -> {
                        if (!(buttonsAvailable[0] && buttonsAvailable[1])) {
                            if (buttonsAvailable[0] || buttonsAvailable[1])
                            //Button 1 or 2 available
                                binding.score2?.inc()
                            else
                            //No other button available
                                binding.score1?.inc()
                        }
                        vibrate()
                        true
                    }
                    else -> {
                        super.onKeyDown(keyCode, event)
                    }
                }
            }
            1 -> onKeyLongPress(keyCode, event)
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                binding.score1?.reset()
                vibrate()
                true
            }
            KeyEvent.KEYCODE_STEM_2 -> {
                if (buttonsAvailable[0])
                //Button 1 available
                    binding.score2?.reset()
                else
                //Buttons 2 and/or 3 available
                    binding.score1?.reset()

                vibrate()
                true
            }
            KeyEvent.KEYCODE_STEM_3 -> {
                if (!(buttonsAvailable[0] && buttonsAvailable[1])) {
                    if (buttonsAvailable[0] || buttonsAvailable[1])
                    //Button 1 or 2 available
                        binding.score2?.reset()
                    else
                    //No other button available
                        binding.score1?.reset()
                }
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
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        250,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(250)
            }
        }
    }

    companion object {
        private const val BURN_IN_OFFSET_PX = 10
    }

}