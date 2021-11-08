package es.hegocre.scorecounter

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
    private lateinit var binding: ActivityScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_score)

        Score.setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this).also {
            if (it.getBoolean("firstRun", true)) {
                showTutorialDialog()
            }
        })
        Score("score1").let { score ->
            binding.score1 = score
            loadScore(score, binding.add1Layout, binding.sub1Layout)
        }
        Score("score2").let { score ->
            binding.score2 = score
            loadScore(score, binding.add2Layout, binding.sub2Layout)
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        hideSystemUI()
        super.onResume()
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

    private fun showTutorialDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_tutorial_title)
            .setMessage(R.string.dialog_tutorial_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}