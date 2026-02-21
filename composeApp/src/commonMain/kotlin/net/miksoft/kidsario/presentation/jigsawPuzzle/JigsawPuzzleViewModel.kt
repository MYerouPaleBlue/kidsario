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
    CRANE("puzzle_crane"),
    HIPPO("puzzle_hippo"),
    PHOTO001("puzzle_photo001"),
    PHOTO002("puzzle_photo002"),
    PHOTO003("puzzle_photo003"),
    PHOTO004("puzzle_photo004"),
    PHOTO005("puzzle_photo005"),
    PHOTO006("puzzle_photo006"),
    PHOTO007("puzzle_photo007"),
    PHOTO008("puzzle_photo008"),
    PHOTO009("puzzle_photo009"),
    PHOTO010("puzzle_photo010"),
    PHOTO011("puzzle_photo011"),
    PHOTO012("puzzle_photo012"),
    PHOTO013("puzzle_photo013"),
    PHOTO014("puzzle_photo014"),
    PHOTO015("puzzle_photo015"),
    PHOTO016("puzzle_photo016"),
    PHOTO017("puzzle_photo017"),
    PHOTO018("puzzle_photo018"),
    PHOTO019("puzzle_photo019"),
    SQUIRREL("puzzle_squirrel"),
    WHALE("puzzle_whale")
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

enum class TrayLayoutMode {
    BOTTOM,
    SIDE
}

/**
 * ViewModel for the Jigsaw Puzzle mini game.
 */
class JigsawPuzzleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawPuzzleUiState())
    val uiState: StateFlow<JigsawPuzzleUiState> = _uiState.asStateFlow()

    // Current level (increases progressively)
    private var currentLevel = 1

    // Balanced rotation of images
    private var availableImages = mutableListOf<PuzzleImageResource>()

    private var trayLayoutMode: TrayLayoutMode? = null

    private fun getNextImage(): PuzzleImageResource {
        if (availableImages.isEmpty()) {
            availableImages.addAll(PuzzleImageResource.entries.shuffled())
        }
        return availableImages.removeAt(0)
    }

    init {
        generateNewPuzzle()
    }

    /**
     * Generate a new puzzle with randomized pieces.
     */
    fun generateNewPuzzle(advanceLevel: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            trayLayoutMode = null

            if (advanceLevel) {
                currentLevel++
            }

            val gridSize = currentState.difficulty.gridSize
            // Select the next image in a balanced rotation
            val selectedImage = getNextImage()
            
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
     * Update the position of a dragged piece by adding delta values.
     * @param pieceId The ID of the piece being dragged
     * @param deltaX The normalized horizontal movement delta
     * @param deltaY The normalized vertical movement delta
     */
    fun updatePiecePosition(pieceId: Int, deltaX: Float, deltaY: Float) {
        if (_uiState.value.isComplete) return

        val currentState = _uiState.value
        val updatedPieces = currentState.pieces.map { piece ->
            if (piece.id == pieceId && !piece.isPlaced) {
                val newX = (piece.currentX + deltaX).coerceIn(0.05f, 0.95f)
                val newY = (piece.currentY + deltaY).coerceIn(0.05f, 0.95f)
                piece.copy(currentX = newX, currentY = newY)
            } else {
                piece
            }
        }

        _uiState.value = currentState.copy(pieces = updatedPieces)
    }

    fun updateTrayLayout(boardWidth: Float, boardHeight: Float) {
        val layout = calculatePuzzleLayout(boardWidth, boardHeight)
        val targetMode = if (layout.isShortHeight) TrayLayoutMode.SIDE else TrayLayoutMode.BOTTOM

        if (trayLayoutMode == targetMode) return

        trayLayoutMode = targetMode

        val currentState = _uiState.value
        if (currentState.pieces.isEmpty()) return

        val updatedPieces = when (targetMode) {
            TrayLayoutMode.SIDE -> {
                val unplacedPieces = currentState.pieces.filter { !it.isPlaced }
                val columns = layout.trayColumns.coerceAtLeast(1)
                val rows = (unplacedPieces.size + columns - 1) / columns
                val cellWidth = layout.trayWidth / columns
                val cellHeight = layout.trayHeight / rows.coerceAtLeast(1)

                val positions = mutableMapOf<Int, Pair<Float, Float>>()
                unplacedPieces.forEachIndexed { index, piece ->
                    val col = index % columns
                    val row = index / columns
                    val centerX = layout.trayStartX + cellWidth * (col + 0.5f)
                    val centerY = layout.trayStartY + cellHeight * (row + 0.5f)
                    positions[piece.id] = Pair(
                        (centerX / boardWidth).coerceIn(0.05f, 0.95f),
                        (centerY / boardHeight).coerceIn(0.05f, 0.95f)
                    )
                }

                currentState.pieces.map { piece ->
                    val position = positions[piece.id]
                    if (position != null) {
                        piece.copy(currentX = position.first, currentY = position.second)
                    } else {
                        piece
                    }
                }
            }
            TrayLayoutMode.BOTTOM -> {
                currentState.pieces.map { piece ->
                    if (!piece.isPlaced) {
                        piece.copy(
                            currentX = Random.nextFloat() * 0.7f + 0.15f,
                            currentY = Random.nextFloat() * 0.25f + 0.7f
                        )
                    } else {
                        piece
                    }
                }
            }
        }

        _uiState.value = currentState.copy(pieces = updatedPieces)
    }

    /**
     * End dragging and check if the piece snaps to its correct position.
     */
    fun endDragging(pieceId: Int, boardWidth: Float, boardHeight: Float) {
        if (_uiState.value.isComplete) return

        val currentState = _uiState.value
        val gridSize = currentState.gridSize
        val piece = currentState.pieces.find { it.id == pieceId } ?: return

        val layout = calculatePuzzleLayout(boardWidth, boardHeight)
        val puzzleAreaStartX = layout.puzzleAreaStartX
        val puzzleAreaEndX = layout.puzzleAreaStartX + layout.puzzleSize
        val puzzleAreaStartY = layout.puzzleAreaStartY
        val puzzleAreaEndY = layout.puzzleAreaStartY + layout.puzzleSize
        val cellSize = layout.puzzleSize / gridSize

        val currentX = piece.currentX * boardWidth
        val currentY = piece.currentY * boardHeight

        val targetCol = ((currentX - puzzleAreaStartX) / cellSize).toInt()
        val targetRow = ((currentY - puzzleAreaStartY) / cellSize).toInt()

        // Check if the piece is within the puzzle area and in the correct position
        val isInPuzzleArea = currentX in puzzleAreaStartX..puzzleAreaEndX &&
                currentY in puzzleAreaStartY..puzzleAreaEndY

        val isCorrectPosition = isInPuzzleArea &&
                targetCol == piece.correctCol &&
                targetRow == piece.correctRow

        // Check if another piece is already placed at this position
        val isPositionOccupied = currentState.pieces.any { 
            it.isPlaced && it.correctRow == targetRow && it.correctCol == targetCol && it.id != pieceId
        }

        val updatedPieces = if (isCorrectPosition && !isPositionOccupied) {
            // Snap to correct position
            val correctX = (puzzleAreaStartX + (piece.correctCol + 0.5f) * cellSize) / boardWidth
            val correctY = (puzzleAreaStartY + (piece.correctRow + 0.5f) * cellSize) / boardHeight

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
