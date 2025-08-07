package es.hegocre.scorecounter

import android.app.Application
import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import androidx.core.content.edit

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val _preferencesManager =
        application.getSharedPreferences("scores", Context.MODE_PRIVATE)

    private val _scores = Json.decodeFromString<List<Int>>(
        _preferencesManager.getString("scores", "[0, 0]") ?: "[0, 0]"
    ).toMutableStateList()
    val scores: List<Int>
        get() = _scores

    private suspend fun saveScore() {
        withContext(Dispatchers.IO) {
            val scoresString = Json.encodeToString(_scores.toList())
            _preferencesManager.edit { putString("scores", scoresString) }
        }
    }

    fun inc(index: Int) {
        if (index < _scores.size) {
            _scores[index]++
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun dec(index: Int) {
        if (index < _scores.size) {
            _scores[index]--
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun reset(index: Int) {
        if (index < _scores.size) {
            _scores[index] = 0
            viewModelScope.launch {
                saveScore()
            }
        }
    }
}