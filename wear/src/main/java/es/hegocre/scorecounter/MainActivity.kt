package es.hegocre.scorecounter

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import es.hegocre.scorecounter.databinding.ActivityMainBinding

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var binding : ActivityMainBinding
    private lateinit var ambientController : AmbientModeSupport.AmbientController
    private var needsBurnProtect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Scorer(binding.score1, binding.add1Layout, binding.sub1Layout)
        Scorer(binding.score2, binding.add2Layout, binding.sub2Layout)

        ambientController = AmbientModeSupport.attach(this)

    }

    class Scorer(private val scoreView: TextView, addLayout: View, subLayout: View) {
        private var isLong = false

        init {
            addLayout.setOnClickListener {
                if (isLong) isLong = false
                else add()
            }
            addLayout.setOnLongClickListener {
                isLong = true
                reset()
                false
            }
            subLayout.setOnClickListener {
                if (isLong) isLong = false
                else sub()
            }
            subLayout.setOnLongClickListener {
                isLong = true
                reset()
                false
            }
        }

        private fun add() {
            scoreView.text = (scoreView.text.toString().toInt() + 1).toString()
        }

        private fun sub() {
            val so = scoreView.text.toString().toInt()
            if (so > 0) scoreView.text = (so - 1).toString()
        }

        private fun reset() {
            scoreView.text = "0"
        }
    }

    override fun getAmbientCallback() : AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {

        override fun onEnterAmbient(ambientDetails: Bundle?) {
            binding.root.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.black))
            binding.divider.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))

            needsBurnProtect = ambientDetails!!.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false)
            super.onEnterAmbient(ambientDetails)
        }

        override fun onExitAmbient() {
            binding.root.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.dark_grey))
            binding.divider.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.grey))
            if (needsBurnProtect) binding.root.setPadding(0, 0, 0, 0)
            super.onExitAmbient()
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
            if (needsBurnProtect) {
                val x = (Math.random() * 2 * Companion.BURN_IN_OFFSET_PX - Companion.BURN_IN_OFFSET_PX).toInt()
                val y = (Math.random() * 2 * Companion.BURN_IN_OFFSET_PX - Companion.BURN_IN_OFFSET_PX).toInt()
                binding.root.setPadding(x, y, 0, 0)
            }
        }
    }

    companion object {
        private const val BURN_IN_OFFSET_PX = 10
    }

}