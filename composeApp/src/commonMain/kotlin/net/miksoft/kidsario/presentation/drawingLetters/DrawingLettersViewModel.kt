package net.miksoft.kidsario.presentation.drawingLetters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Drawing Letters mini game.
 */
class DrawingLettersViewModel : ViewModel() {

    // State for the Drawing Letters game
    private val _uiState = MutableStateFlow(DrawingLettersUiState())
    val uiState: StateFlow<DrawingLettersUiState> = _uiState.asStateFlow()

    init {
        startNewRound()
    }

    /**
     * Start a new round of the game with a random letter or number.
     */
    fun startNewRound() {
        viewModelScope.launch {
            // Generate a random character (letter or number)
            val character = generateRandomCharacter()

            _uiState.value = DrawingLettersUiState(
                character = character,
                userDrawingPoints = emptyList(),
                message = null,
                isCorrectDrawing = null,
                isGameActive = true
            )
        }
    }

    /**
     * Add a point to the user's drawing.
     * 
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @param isNewStroke Whether this point starts a new stroke
     */
    fun addDrawingPoint(x: Float, y: Float, isNewStroke: Boolean = false) {
        if (!_uiState.value.isGameActive) return

        val currentPoints = _uiState.value.userDrawingPoints.toMutableList()
        val newPoint = DrawingPoint(x, y, isNewStroke)
        currentPoints.add(newPoint)

        _uiState.value = _uiState.value.copy(
            userDrawingPoints = currentPoints
        )
    }

    /**
     * Check if the user's drawing is close enough to the target letter/number.
     */
    fun checkDrawing() {
        viewModelScope.launch {
            // In a real implementation, this would use a more sophisticated algorithm
            // to check if the drawing matches the letter/number.
            // For this example, we'll just check if the user has drawn enough points.
            val isCorrect = _uiState.value.userDrawingPoints.size >= 10

            // Update state to show feedback message
            _uiState.value = _uiState.value.copy(
                isCorrectDrawing = isCorrect,
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
     * Reset the current round to allow the user to try again with the same letter/number.
     */
    fun resetCurrentRound() {
        viewModelScope.launch {
            // Clear the user's drawing but keep the same character
            _uiState.value = _uiState.value.copy(
                userDrawingPoints = emptyList(),
                message = null,
                isCorrectDrawing = null,
                isGameActive = true
            )
        }
    }

    /**
     * Clear the user's drawing without resetting the round.
     */
    fun clearDrawing() {
        if (!_uiState.value.isGameActive) return

        _uiState.value = _uiState.value.copy(
            userDrawingPoints = emptyList()
        )
    }

    /**
     * Generate a random character (letter or number).
     * 
     * @return A random letter (A-Z) or number (0-9)
     */
    private fun generateRandomCharacter(): Char {
        val characters = ('A'..'Z') + ('0'..'9')
        return characters.random()
    }
}

/**
 * Data class representing the UI state for the Drawing Letters game
 */
data class DrawingLettersUiState(
    val character: Char = 'A',
    val userDrawingPoints: List<DrawingPoint> = emptyList(),
    val message: String? = null,
    val isCorrectDrawing: Boolean? = null,
    val isGameActive: Boolean = true
)

/**
 * Data class representing a point in the user's drawing
 */
data class DrawingPoint(
    val x: Float,
    val y: Float,
    val isNewStroke: Boolean = false
)