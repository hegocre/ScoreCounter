package es.hegocre.scorecounter

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val _preferencesManager =
        application.getSharedPreferences("scores", Context.MODE_PRIVATE)

    private val _score1 = mutableStateOf(_preferencesManager.getInt("score1", 0))
    var score1: Int
        get() = _score1.value
        set(value) {
            _score1.value = value
            _preferencesManager.edit().putInt("score1", value).apply()
        }
    private val _score2 = mutableStateOf(_preferencesManager.getInt("score2", 0))
    var score2: Int
        get() = _score2.value
        set(value) {
            _score2.value = value
            _preferencesManager.edit().putInt("score2", value).apply()
        }
}