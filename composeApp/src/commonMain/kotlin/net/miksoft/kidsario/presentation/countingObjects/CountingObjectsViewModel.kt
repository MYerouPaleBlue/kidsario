package net.miksoft.kidsario.presentation.countingObjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Counting Objects mini game.
 */
class CountingObjectsViewModel : ViewModel() {

    // State for the Counting Objects game
    private val _uiState = MutableStateFlow(CountingObjectsUiState())
    val uiState: StateFlow<CountingObjectsUiState> = _uiState.asStateFlow()

    init {
        startNewRound()
    }

    /**
     * Start a new round of the game with a random number of objects and answer options.
     */
    fun startNewRound() {
        viewModelScope.launch {
            val currentDifficulty = _uiState.value.difficulty

            // Generate a random number of objects based on the current difficulty
            val range = currentDifficulty.range
            val correctCount = Random.nextInt(range.first, range.last + 1)

            // Generate 4 unique answer options including the correct answer
            val answerOptions = generateAnswerOptions(correctCount)

            // Generate a random shape type for this round
            val shapeType = ShapeType.values().random()

            _uiState.value = _uiState.value.copy(
                objectCount = correctCount,
                answerOptions = answerOptions,
                shapeType = shapeType,
                message = null,
                isCorrectAnswer = null,
                isGameActive = true,
                clickedObjects = emptySet() // Reset clicked objects for new round
            )
        }
    }

    /**
     * Handle user's answer selection.
     * 
     * @param selectedAnswer The number selected by the user
     */
    fun onAnswerSelected(selectedAnswer: Int) {
        viewModelScope.launch {
            val isCorrect = selectedAnswer == _uiState.value.objectCount

            // Update state to show feedback message
            _uiState.value = _uiState.value.copy(
                isCorrectAnswer = isCorrect,
                message = if (isCorrect) "Great job!" else "Try again!",
                isGameActive = false
            )

            // If correct, wait a moment and start a new round
            if (isCorrect) {
                delay(1500) // Wait 1.5 seconds before starting a new round
                startNewRound()
            }
        }
    }

    /**
     * Reset the current round to allow the user to try again without changing the objects and answers.
     */
    fun resetCurrentRound() {
        viewModelScope.launch {
            // Only reset the game state, keeping the same objects, answers, and shape type
            _uiState.value = _uiState.value.copy(
                message = null,
                isCorrectAnswer = null,
                isGameActive = true,
                clickedObjects = emptySet() // Reset clicked objects
            )
        }
    }

    /**
     * Handle object click events.
     * 
     * @param objectIndex The index of the clicked object
     */
    fun onObjectClicked(objectIndex: Int) {
        println("[DEBUG_LOG] Object clicked: $objectIndex")
        val currentClickedObjects = _uiState.value.clickedObjects.toMutableSet()

        // Toggle the clicked state of the object
        if (currentClickedObjects.contains(objectIndex)) {
            println("[DEBUG_LOG] Removing object $objectIndex from clicked objects")
            currentClickedObjects.remove(objectIndex)
        } else {
            println("[DEBUG_LOG] Adding object $objectIndex to clicked objects")
            currentClickedObjects.add(objectIndex)
        }

        _uiState.value = _uiState.value.copy(
            clickedObjects = currentClickedObjects
        )
        println("[DEBUG_LOG] Updated clicked objects: ${_uiState.value.clickedObjects}")
    }

    /**
     * Toggle the visibility of the difficulty selection dialog.
     */
    fun toggleDifficultyDialog() {
        _uiState.value = _uiState.value.copy(
            showDifficultyDialog = !_uiState.value.showDifficultyDialog
        )
    }

    /**
     * Change the game difficulty and start a new round.
     * 
     * @param difficulty The new difficulty level
     */
    fun changeDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            showDifficultyDialog = false
        )
        startNewRound()
    }

    /**
     * Generate 4 unique answer options including the correct answer.
     * 
     * @param correctAnswer The correct answer to include in the options
     * @return List of 4 unique integers including the correct answer
     */
    private fun generateAnswerOptions(correctAnswer: Int): List<Int> {
        val currentDifficulty = _uiState.value.difficulty
        val range = currentDifficulty.range
        val options = mutableSetOf(correctAnswer)

        // Add 3 more unique options
        while (options.size < 4) {
            // Generate a random number within the difficulty range, but avoid the correct answer
            val option = Random.nextInt(range.first, range.last + 1)
            options.add(option)
        }

        // Return the options in a random order
        return options.toList().shuffled()
    }
}

/**
 * Data class representing the UI state for the Counting Objects game
 */
data class CountingObjectsUiState(
    val objectCount: Int = 0,
    val answerOptions: List<Int> = emptyList(),
    val shapeType: ShapeType = ShapeType.CIRCLE,
    val message: String? = null,
    val isCorrectAnswer: Boolean? = null,
    val isGameActive: Boolean = true,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val showDifficultyDialog: Boolean = false,
    val clickedObjects: Set<Int> = emptySet()
)

/**
 * Enum representing the different types of shapes that can be displayed
 */
enum class ShapeType {
    CIRCLE,
    RECTANGLE,
    SQUARE,
    TRIANGLE
}

/**
 * Enum representing the different difficulty levels for the game
 */
enum class Difficulty(val displayName: String, val range: IntRange) {
    EASY("Easy", 0..5),
    MEDIUM("Medium", 0..10),
    HARD("Hard", 0..20),
    SUPER_HARD("Super Hard", 0..40)
}
