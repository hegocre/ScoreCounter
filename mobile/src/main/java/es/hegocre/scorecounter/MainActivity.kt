package es.hegocre.scorecounter

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import es.hegocre.scorecounter.data.Score
import es.hegocre.scorecounter.databinding.ActivityScoreBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityScoreBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        hideSystemUI()

        Scorer (binding.score1, binding.add1Layout, binding.sub1Layout, "score1")
        Scorer (binding.score2, binding.add2Layout, binding.sub2Layout, "score2")
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        hideSystemUI()
        super.onResume()
    }

    inner class Scorer (private val scoreView : TextView, addLayout : View, subLayout : View, private val scoreId : String) {
        private var isLong = false
        private val editor = sharedPreferences.edit()

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

            scoreView.text = sharedPreferences.getString(scoreId + "last", "0")
        }

        private fun add() {
            scoreView.text = (scoreView.text.toString().toInt() + 1).toString()
            saveValue()
        }

        private fun sub() {
            val so = scoreView.text.toString().toInt()
            if (so > 0) scoreView.text = (so - 1).toString()
            saveValue()
        }

        private fun reset() {
            scoreView.text = "0"
            saveValue()
        }

        private fun saveValue() {
            editor.putString(scoreId + "last", scoreView.text.toString())
            editor.apply()
        }
    }

    private fun showTutorialDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_tutorial_title)
            .setMessage(R.string.dialog_tutorial_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        fun isFirstInstall (context: Context): Boolean {
            return try {
                val firstInstallTime =
                    context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
                val lastUpdateTime =
                    context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
                firstInstallTime == lastUpdateTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                true
            }
        }
    }
}