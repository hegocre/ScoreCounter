package es.hegocre.scorecounter.data

import android.content.SharedPreferences
import androidx.databinding.ObservableInt

data class Score(
    private val scoreId: String,
    var score: ObservableInt = ObservableInt(sharedPreferences?.getInt(scoreId, 0) ?: 0)
) {
    fun inc() {
        score.set(score.get().inc())
        save()
    }

    fun dec() {
        score.set(score.get().dec())
        save()
    }

    fun reset() {
        score.set(0)
        save()
    }

    private fun save() {
        sharedPreferences?.edit()?.putInt(scoreId, score.get())?.apply()
    }

    companion object {
        private var sharedPreferences: SharedPreferences? = null
        fun setSharedPreferences(preferences: SharedPreferences) {
            sharedPreferences = preferences
        }
    }
}