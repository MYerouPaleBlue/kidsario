package net.miksoft.kidsario.presentation.jigsawPuzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Enum representing difficulty levels for the jigsaw puzzle game.
 * The gridSize determines the number of pieces (gridSize x gridSize).
 */
enum class JigsawDifficulty(val displayName: String, val gridSize: Int) {
    EASY("Easy", 2),       // 2x2 = 4 pieces
    MEDIUM("Medium", 3),   // 3x3 = 9 pieces
    HARD("Hard", 4)        // 4x4 = 16 pieces
}

/**
 * Enum representing available puzzle images.
 * Images should be placed in: composeApp/src/commonMain/composeResources/drawable/puzzle/
 * Add more images to the folder and add entries here.
 */
enum class PuzzleImageResource(val resourceName: String) {
    WHALE("puzzle_whale"),
    HIPPO("puzzle_hippo")
}

/**
 * Data class representing a single puzzle piece.
 */
data class PuzzlePiece(
    val id: Int,
    val correctRow: Int,
    val correctCol: Int,
    var currentX: Float,
    var currentY: Float,
    val isPlaced: Boolean = false
)

/**
 * ViewModel for the Jigsaw Puzzle mini game.
 */
class JigsawPuzzleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawPuzzleUiState())
    val uiState: StateFlow<JigsawPuzzleUiState> = _uiState.asStateFlow()

    // Current level (increases progressively)
    private var currentLevel = 1

    init {
        generateNewPuzzle()
    }

    /**
     * Generate a new puzzle with randomized pieces.
     */
    fun generateNewPuzzle(advanceLevel: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (advanceLevel) {
                currentLevel++
            }

            val gridSize = currentState.difficulty.gridSize
            // Select a random image
            val imageResources = PuzzleImageResource.values()
            val selectedImage = imageResources[Random.nextInt(imageResources.size)]
            
            val pieces = mutableListOf<PuzzlePiece>()

            // Create pieces with shuffled starting positions in the tray area
            var id = 0
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    pieces.add(
                        PuzzlePiece(
                            id = id++,
                            correctRow = row,
                            correctCol = col,
                            currentX = Random.nextFloat() * 0.7f + 0.15f, // Random X in tray
                            currentY = Random.nextFloat() * 0.25f + 0.7f, // Random Y in bottom tray area
                            isPlaced = false
                        )
                    )
                }
            }

            // Shuffle the pieces
            val shuffledPieces = pieces.shuffled()

            _uiState.value = JigsawPuzzleUiState(
                pieces = shuffledPieces,
                difficulty = currentState.difficulty,
                puzzleImage = selectedImage,
                level = currentLevel,
                gridSize = gridSize,
                isComplete = false,
                message = null,
                showDifficultyDialog = false,
                draggedPieceId = null
            )
        }
    }

    /**
     * Start dragging a piece.
     */
    fun startDragging(pieceId: Int) {
        if (_uiState.value.isComplete) return

        _uiState.value = _uiState.value.copy(draggedPieceId = pieceId)
    }

    /**
     * Update the position of a dragged piece.
     */
    fun updatePiecePosition(pieceId: Int, x: Float, y: Float) {
        if (_uiState.value.isComplete) return

        val currentState = _uiState.value
        val updatedPieces = currentState.pieces.map { piece ->
            if (piece.id == pieceId && !piece.isPlaced) {
                piece.copy(currentX = x.coerceIn(0.05f, 0.95f), currentY = y.coerceIn(0.05f, 0.95f))
            } else {
                piece
            }
        }

        _uiState.value = currentState.copy(pieces = updatedPieces)
    }

    /**
     * End dragging and check if the piece snaps to its correct position.
     */
    fun endDragging(pieceId: Int) {
        if (_uiState.value.isComplete) return

        val currentState = _uiState.value
        val gridSize = currentState.gridSize
        val piece = currentState.pieces.find { it.id == pieceId } ?: return

        // Calculate the target grid position based on current coordinates
        // The puzzle grid is a square that fits in the top portion of the canvas
        // Use normalized coordinates (0-1 range based on board dimensions)
        // These values match the calculations in JigsawPuzzleComponent
        val maxPuzzleWidth = 0.8f  // 80% of board width
        val maxPuzzleHeight = 0.5f // 50% of board height
        val puzzleSize = minOf(maxPuzzleWidth, maxPuzzleHeight) // Square puzzle area
        
        val puzzleAreaStartX = (1f - puzzleSize) / 2f  // Centered horizontally
        val puzzleAreaEndX = puzzleAreaStartX + puzzleSize
        val puzzleAreaStartY = 0.05f
        val puzzleAreaEndY = puzzleAreaStartY + puzzleSize

        val cellSize = puzzleSize / gridSize

        val targetCol = ((piece.currentX - puzzleAreaStartX) / cellSize).toInt()
        val targetRow = ((piece.currentY - puzzleAreaStartY) / cellSize).toInt()

        // Check if the piece is within the puzzle area and in the correct position
        val isInPuzzleArea = piece.currentX in puzzleAreaStartX..puzzleAreaEndX &&
                piece.currentY in puzzleAreaStartY..puzzleAreaEndY

        val isCorrectPosition = isInPuzzleArea &&
                targetCol == piece.correctCol &&
                targetRow == piece.correctRow

        // Check if another piece is already placed at this position
        val isPositionOccupied = currentState.pieces.any { 
            it.isPlaced && it.correctRow == targetRow && it.correctCol == targetCol && it.id != pieceId
        }

        val updatedPieces = if (isCorrectPosition && !isPositionOccupied) {
            // Snap to correct position
            val correctX = puzzleAreaStartX + (piece.correctCol + 0.5f) * cellSize
            val correctY = puzzleAreaStartY + (piece.correctRow + 0.5f) * cellSize

            currentState.pieces.map { p ->
                if (p.id == pieceId) {
                    p.copy(currentX = correctX, currentY = correctY, isPlaced = true)
                } else {
                    p
                }
            }
        } else {
            currentState.pieces
        }

        // Check if all pieces are placed
        val allPlaced = updatedPieces.all { it.isPlaced }

        _uiState.value = currentState.copy(
            pieces = updatedPieces,
            draggedPieceId = null,
            isComplete = allPlaced,
            message = if (allPlaced) "Great job! Puzzle complete!" else null
        )

        // If complete, generate a new puzzle after a delay
        if (allPlaced) {
            viewModelScope.launch {
                delay(2000)
                generateNewPuzzle(advanceLevel = true)
            }
        }
    }

    /**
     * Toggle the difficulty selection dialog.
     */
    fun toggleDifficultyDialog() {
        _uiState.value = _uiState.value.copy(
            showDifficultyDialog = !_uiState.value.showDifficultyDialog
        )
    }

    /**
     * Change the difficulty level and generate a new puzzle.
     */
    fun changeDifficulty(difficulty: JigsawDifficulty) {
        currentLevel = 1 // Reset level when difficulty changes
        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            showDifficultyDialog = false
        )
        generateNewPuzzle()
    }
}

/**
 * Data class representing the UI state for the Jigsaw Puzzle game.
 */
data class JigsawPuzzleUiState(
    val pieces: List<PuzzlePiece> = emptyList(),
    val difficulty: JigsawDifficulty = JigsawDifficulty.EASY,
    val puzzleImage: PuzzleImageResource = PuzzleImageResource.WHALE,
    val level: Int = 1,
    val gridSize: Int = 2,
    val isComplete: Boolean = false,
    val message: String? = null,
    val showDifficultyDialog: Boolean = false,
    val draggedPieceId: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JigsawPuzzleUiState

        if (pieces != other.pieces) return false
        if (difficulty != other.difficulty) return false
        if (puzzleImage != other.puzzleImage) return false
        if (level != other.level) return false
        if (gridSize != other.gridSize) return false
        if (isComplete != other.isComplete) return false
        if (message != other.message) return false
        if (showDifficultyDialog != other.showDifficultyDialog) return false
        if (draggedPieceId != other.draggedPieceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pieces.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + puzzleImage.hashCode()
        result = 31 * result + level
        result = 31 * result + gridSize
        result = 31 * result + isComplete.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + showDifficultyDialog.hashCode()
        result = 31 * result + (draggedPieceId ?: 0)
        return result
    }
}
