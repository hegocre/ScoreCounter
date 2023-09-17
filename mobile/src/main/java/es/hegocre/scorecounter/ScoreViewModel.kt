package es.hegocre.scorecounter

import android.app.Application
import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.hegocre.scorecounter.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val _preferencesManager =
        application.getSharedPreferences("scores", Context.MODE_PRIVATE)

    private val _scores = (_preferencesManager.getString("scores", null)?.let {
        try {
            Json.decodeFromString(it)
        } catch (e: Exception) {
            listOf(Score(), Score())
        }
    } ?: listOf(Score(), Score())).toMutableStateList()
    val scores: List<Score>
        get() = _scores

    private suspend fun saveScore() {
        withContext(Dispatchers.IO) {
            val scoresString = Json.encodeToString(_scores.toList())
            _preferencesManager.edit().putString("scores", scoresString).apply()
        }
    }

    fun add() {
        _scores.add(Score())
        viewModelScope.launch {
            saveScore()
        }
    }

    fun del(index: Int) {
        if (index < _scores.size) {
            _scores.removeAt(index)
            if (_scores.isEmpty()) {
                _scores.add(Score())
            }
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun inc(index: Int) {
        if (index < _scores.size) {
            _scores[index] = _scores[index].copy(score = _scores[index].score + 1)
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun dec(index: Int) {
        if (index < _scores.size) {
            _scores[index] = _scores[index].copy(score = _scores[index].score - 1)
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun reset(index: Int) {
        if (index < _scores.size) {
            _scores[index] = _scores[index].copy(score = 0)
            viewModelScope.launch {
                saveScore()
            }
        }
    }

    fun setPlayerName(index: Int, name: String) {
        if (index < _scores.size) {
            _scores[index] = _scores[index].copy(playerName = name)
            viewModelScope.launch {
                saveScore()
            }
        }
    }
}