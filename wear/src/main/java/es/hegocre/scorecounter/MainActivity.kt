package es.hegocre.scorecounter

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.wear.ambient.AmbientModeSupport
import es.hegocre.scorecounter.data.Score
import es.hegocre.scorecounter.databinding.ActivityMainBinding

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private var needsBurnProtect = false

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

    companion object {
        private const val BURN_IN_OFFSET_PX = 10
    }

}