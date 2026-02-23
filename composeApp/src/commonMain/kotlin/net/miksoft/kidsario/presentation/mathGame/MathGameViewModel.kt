package net.miksoft.kidsario.presentation.mathGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MathGameAnimal(val displayName: String) {
    HIPPO("Hippo"),
    WHALE("Whale"),
    SQUIRREL("Squirrel"),
    CRANE("Crane")
}

data class MathGameUiState(
    val score: Int = 0,
    val highScore: Int = 0,
    val optionCount: Int = 3,
    val rangeOption: MathRangeOption = MathRangeOption.SMALL,
    val allowedOperations: Set<MathOperation> = setOf(MathOperation.ADD, MathOperation.SUBTRACT),
    val currentProblem: MathProblem = MathProblem(1, 1, MathOperation.ADD),
    val options: List<MathOption> = emptyList(),
    val isGameActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val lastResultCorrect: Boolean? = null,
    val currentAnimal: MathGameAnimal = MathGameAnimal.HIPPO
)

class MathGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MathGameUiState())
    val uiState = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private val animals = MathGameAnimal.values().toList()

    init {
        resetGameState()
    }

    fun startGame() {
        stopGame()
        resetGameState(keepHighScore = true)
        _uiState.value = _uiState.value.copy(
            isGameActive = true
        )
    }

    fun stopGame() {
        refreshJob?.cancel()
        _uiState.value = _uiState.value.copy(isGameActive = false)
    }

    fun toggleSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = !_uiState.value.showSettingsDialog)
    }


    fun changeOptionCount(count: Int) {
        stopGame()
        _uiState.value = _uiState.value.copy(
            optionCount = count,
            showSettingsDialog = false
        )
        resetGameState(keepHighScore = true)
    }

    fun changeRangeOption(rangeOption: MathRangeOption) {
        stopGame()
        _uiState.value = _uiState.value.copy(
            rangeOption = rangeOption,
            showSettingsDialog = false
        )
        resetGameState(keepHighScore = true)
    }

    fun toggleOperation(operation: MathOperation) {
        val current = _uiState.value.allowedOperations.toMutableSet()
        if (current.contains(operation)) {
            if (current.size == 1) {
                return
            }
            current.remove(operation)
        } else {
            current.add(operation)
        }

        stopGame()
        _uiState.value = _uiState.value.copy(allowedOperations = current)
        resetGameState(keepHighScore = true)
    }

    fun selectOption(option: MathOption) {
        if (!_uiState.value.isGameActive || _uiState.value.isRefreshing) {
            return
        }

        val isCorrect = option.isCorrect
        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score
        val highScore = maxOf(_uiState.value.highScore, newScore)
        _uiState.value = _uiState.value.copy(
            score = newScore,
            highScore = highScore,
            lastResultCorrect = isCorrect,
            isRefreshing = true
        )
        refreshRound()
    }

    fun playAgain() {
        startGame()
    }

    private fun resetGameState(keepHighScore: Boolean = false) {
        val range = _uiState.value.rangeOption.range
        val allowed = _uiState.value.allowedOperations
        val problem = MathGameData.generateProblem(range, allowed)
        val options = MathGameData.buildOptions(problem, _uiState.value.optionCount, range)
        val highScore = if (keepHighScore) _uiState.value.highScore else 0
        _uiState.value = _uiState.value.copy(
            score = 0,
            highScore = highScore,
            currentProblem = problem,
            options = options,
            lastResultCorrect = null,
            isRefreshing = false,
            currentAnimal = animals.random()
        )
    }

    private fun refreshRound() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            delay(300)
            val range = _uiState.value.rangeOption.range
            val allowed = _uiState.value.allowedOperations
            val problem = MathGameData.generateProblem(range, allowed)
            val options = MathGameData.buildOptions(problem, _uiState.value.optionCount, range)
            _uiState.value = _uiState.value.copy(
                currentProblem = problem,
                options = options,
                isRefreshing = false,
                currentAnimal = animals.random()
            )
        }
    }

}