package net.miksoft.kidsario.presentation.wordsGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordsGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WordsGameUiState())
    val uiState: StateFlow<WordsGameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var refreshJob: Job? = null
    private val availableAnimals = listOf(
        WordsGameAnimal.HIPPO,
        WordsGameAnimal.WHALE,
        WordsGameAnimal.SQUIRREL,
        WordsGameAnimal.CRANE
    )

    init {
        resetGameState()
    }

    fun startGame() {
        resetGameState(keepHighScore = true)
        _uiState.value = _uiState.value.copy(isGameActive = true, isGameOver = false)
        startTimer()
    }

    fun stopGame() {
        timerJob?.cancel()
        refreshJob?.cancel()
        _uiState.value = _uiState.value.copy(isGameActive = false)
    }

    fun toggleSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = !_uiState.value.showSettingsDialog)
    }

    fun changeLanguage(language: WordLanguage) {
        stopGame()
        _uiState.value = _uiState.value.copy(language = language, showSettingsDialog = false)
        resetGameState(keepHighScore = true)
    }

    fun changeTimerDuration(seconds: Int) {
        stopGame()
        _uiState.value = _uiState.value.copy(timerDuration = seconds, showSettingsDialog = false)
        resetGameState(keepHighScore = true)
    }

    fun changeWordCount(count: Int) {
        stopGame()
        _uiState.value = _uiState.value.copy(optionCount = count, showSettingsDialog = false)
        resetGameState(keepHighScore = true)
    }

    fun selectWord(option: WordOption) {
        if (!_uiState.value.isGameActive) return

        val newScore = if (option.isCorrect) _uiState.value.score + 1 else _uiState.value.score
        val newHighScore = maxOf(_uiState.value.highScore, newScore)

        _uiState.value = _uiState.value.copy(
            score = newScore,
            highScore = newHighScore,
            lastResultCorrect = option.isCorrect
        )

        refreshRound()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTime > 0 && _uiState.value.isGameActive) {
                delay(1000)
                val updatedTime = _uiState.value.remainingTime - 1
                _uiState.value = _uiState.value.copy(remainingTime = updatedTime)
            }
            if (_uiState.value.remainingTime <= 0) {
                endGame()
            }
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isGameActive = false,
            isGameOver = true
        )
    }

    private fun refreshRound() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            delay(200)
            val words = WordsGameData.wordsFor(_uiState.value.language)
            val options = WordsGameData.buildOptions(
                _uiState.value.targetLetter,
                words,
                optionCount = _uiState.value.optionCount
            )
            _uiState.value = _uiState.value.copy(
                currentOptions = options,
                isRefreshing = false,
                currentAnimal = pickNextAnimal(_uiState.value.currentAnimal)
            )
        }
    }

    private fun resetGameState(keepHighScore: Boolean = true) {
        val words = WordsGameData.wordsFor(_uiState.value.language)
        val targetLetter = WordsGameData.pickTargetLetter(words)
        val options = WordsGameData.buildOptions(
            targetLetter,
            words,
            optionCount = _uiState.value.optionCount
        )
        val highScore = if (keepHighScore) _uiState.value.highScore else 0

        _uiState.value = _uiState.value.copy(
            targetLetter = targetLetter,
            currentOptions = options,
            score = 0,
            remainingTime = _uiState.value.timerDuration,
            isGameActive = false,
            isGameOver = false,
            lastResultCorrect = null,
            isRefreshing = false,
            highScore = highScore,
            currentAnimal = pickNextAnimal(_uiState.value.currentAnimal)
        )
    }

    private fun pickNextAnimal(current: WordsGameAnimal): WordsGameAnimal {
        val candidates = availableAnimals.filterNot { it == current }
        return candidates.randomOrNull() ?: current
    }
}

enum class WordsGameAnimal {
    HIPPO,
    WHALE,
    SQUIRREL,
    CRANE
}

data class WordsGameUiState(
    val targetLetter: Char = 'A',
    val currentOptions: List<WordOption> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val remainingTime: Int = 30,
    val timerDuration: Int = 30,
    val optionCount: Int = 3,
    val language: WordLanguage = WordLanguage.GREEK,
    val isGameActive: Boolean = false,
    val isGameOver: Boolean = false,
    val isRefreshing: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val lastResultCorrect: Boolean? = null,
    val currentAnimal: WordsGameAnimal = WordsGameAnimal.HIPPO
)