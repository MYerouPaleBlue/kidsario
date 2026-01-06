package net.miksoft.kidsario.presentation.maze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Enum representing difficulty levels for the maze game
 */
enum class MazeDifficulty(val displayName: String, val gridSize: Int) {
    EASY("Easy", 11),
    MEDIUM("Medium", 15),
    HARD("Hard", 19),
    SUPER_HARD("Super Hard", 23)
}

/**
 * ViewModel for the Maze mini game.
 */
class MazeViewModel : ViewModel() {

    // State for the Maze game
    private val _uiState = MutableStateFlow(MazeUiState())
    val uiState: StateFlow<MazeUiState> = _uiState.asStateFlow()

    // Maze dimensions based on difficulty
    private val mazeWidth: Int
        get() = _uiState.value.difficulty.gridSize

    private val mazeHeight: Int
        get() = _uiState.value.difficulty.gridSize

    // Flag to control difficulty dialog visibility
    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    init {
        generateNewMaze()
    }

    /**
     * Generate a new maze.
     */
    fun generateNewMaze() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val maze = generateMaze(mazeWidth, mazeHeight)

            _uiState.value = MazeUiState(
                maze = maze,
                userDrawingPoints = emptyList(),
                message = null,
                isSuccess = false,
                isGameActive = true,
                startPoint = findStartPoint(maze),
                endPoint = findEndPoint(maze),
                difficulty = currentState.difficulty,
                showDifficultyDialog = false
            )
        }
    }

    /**
     * Add a point to the user's drawing path.
     * 
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @param isNewStroke Whether this point starts a new stroke
     */
    fun addDrawingPoint(x: Float, y: Float, isNewStroke: Boolean = false) {
        if (!_uiState.value.isGameActive) return

        val currentState = _uiState.value
        val currentPoints = currentState.userDrawingPoints.toMutableList()

        // Convert screen coordinates to maze coordinates
        val mazeX = (x * mazeWidth).toInt()
        val mazeY = (y * mazeHeight).toInt()

        // Check if the point is within a wall
        if (isPointInWall(mazeX, mazeY, currentState.maze)) {
            // Don't add the point if it's in a wall
            return
        }

        val newPoint = DrawingPoint(x, y, isNewStroke)
        currentPoints.add(newPoint)

        // Check if the user has reached the end point
        val endPoint = currentState.endPoint
        val isAtEndPoint = isNearPoint(x, y, endPoint.first / mazeWidth.toFloat(), endPoint.second / mazeHeight.toFloat())

        if (isAtEndPoint) {
            _uiState.value = currentState.copy(
                userDrawingPoints = currentPoints,
                isSuccess = true,
                message = "Great job! You solved the maze!",
                isGameActive = false
            )

            // Generate a new maze after a delay
            viewModelScope.launch {
                delay(1500) // Wait 1.5 seconds before generating a new maze
                generateNewMaze()
            }
        } else {
            _uiState.value = currentState.copy(
                userDrawingPoints = currentPoints
            )
        }
    }

    /**
     * Clear the user's drawing without resetting the maze.
     */
    fun clearDrawing() {
        if (!_uiState.value.isGameActive) return

        _uiState.value = _uiState.value.copy(
            userDrawingPoints = emptyList()
        )
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
     * Change the difficulty level and generate a new maze.
     * 
     * @param difficulty The new difficulty level
     */
    fun changeDifficulty(difficulty: MazeDifficulty) {
        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            showDifficultyDialog = false
        )
        generateNewMaze()
    }

    /**
     * Check if a point is near another point (within a certain threshold).
     */
    private fun isNearPoint(x1: Float, y1: Float, x2: Float, y2: Float, threshold: Float = 0.05f): Boolean {
        val distance = kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
        return distance < threshold
    }

    /**
     * Check if a point is within a wall in the maze.
     */
    private fun isPointInWall(x: Int, y: Int, maze: Array<Array<MazeCell>>): Boolean {
        // Make sure the point is within the maze bounds
        if (x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight) {
            return true // Consider out-of-bounds as walls
        }

        return maze[y][x] == MazeCell.WALL
    }

    /**
     * Find the start point in the maze.
     */
    private fun findStartPoint(maze: Array<Array<MazeCell>>): Pair<Int, Int> {
        // For simplicity, use the top-left corner as the start point
        // Find the first non-wall cell from the top-left
        for (y in 0 until mazeHeight) {
            for (x in 0 until mazeWidth) {
                if (maze[y][x] == MazeCell.PATH) {
                    return Pair(x, y)
                }
            }
        }
        return Pair(1, 1) // Fallback
    }

    /**
     * Find the end point in the maze.
     */
    private fun findEndPoint(maze: Array<Array<MazeCell>>): Pair<Int, Int> {
        // Use the bottom-right corner as the end point, but with a margin to ensure visibility
        // Find the first non-wall cell from the bottom-right, starting with a margin
        val margin = 1 // Add a margin to keep the end point away from the edge

        for (y in mazeHeight - 1 - margin downTo 0) {
            for (x in mazeWidth - 1 - margin downTo 0) {
                if (maze[y][x] == MazeCell.PATH) {
                    return Pair(x, y)
                }
            }
        }
        return Pair(mazeWidth - 2 - margin, mazeHeight - 2 - margin) // Fallback with margin
    }

    /**
     * Generate a random maze using a simple algorithm.
     * This uses a depth-first search algorithm with backtracking.
     */
    private fun generateMaze(width: Int, height: Int): Array<Array<MazeCell>> {
        // Initialize the maze with all walls
        val maze = Array(height) { Array(width) { MazeCell.WALL } }

        // Define the directions: up, right, down, left
        val directions = listOf(
            Pair(0, -2), // Up
            Pair(2, 0),  // Right
            Pair(0, 2),  // Down
            Pair(-2, 0)  // Left
        )

        // Start from a random cell (must be odd coordinates)
        val startX = 1
        val startY = 1

        // Mark the start cell as a path
        maze[startY][startX] = MazeCell.PATH

        // Stack for backtracking
        val stack = mutableListOf<Pair<Int, Int>>()
        stack.add(Pair(startX, startY))

        // Continue until the stack is empty
        while (stack.isNotEmpty()) {
            val current = stack.last()
            val x = current.first
            val y = current.second

            // Get unvisited neighbors
            val unvisitedNeighbors = mutableListOf<Pair<Int, Int>>()

            for (dir in directions) {
                val nx = x + dir.first
                val ny = y + dir.second

                // Check if the neighbor is within bounds and unvisited
                if (nx in 1 until width - 1 && ny in 1 until height - 1 && maze[ny][nx] == MazeCell.WALL) {
                    unvisitedNeighbors.add(Pair(nx, ny))
                }
            }

            if (unvisitedNeighbors.isNotEmpty()) {
                // Choose a random unvisited neighbor
                val next = unvisitedNeighbors.random()
                val nx = next.first
                val ny = next.second

                // Remove the wall between the current cell and the chosen neighbor
                maze[ny][nx] = MazeCell.PATH
                maze[(y + ny) / 2][(x + nx) / 2] = MazeCell.PATH

                // Push the neighbor to the stack
                stack.add(next)
            } else {
                // Backtrack
                stack.removeAt(stack.size - 1)
            }
        }

        // Ensure the start and end points are paths
        maze[1][1] = MazeCell.PATH

        // Set the end point with margin to ensure it's visible
        val margin = 1
        maze[height - 2 - margin][width - 2 - margin] = MazeCell.PATH

        return maze
    }
}

/**
 * Data class representing the UI state for the Maze game
 */
data class MazeUiState(
    val maze: Array<Array<MazeCell>> = Array(10) { Array(10) { MazeCell.WALL } },
    val userDrawingPoints: List<DrawingPoint> = emptyList(),
    val message: String? = null,
    val isSuccess: Boolean = false,
    val isGameActive: Boolean = true,
    val startPoint: Pair<Int, Int> = Pair(1, 1),
    val endPoint: Pair<Int, Int> = Pair(8, 8),
    val difficulty: MazeDifficulty = MazeDifficulty.EASY,
    val showDifficultyDialog: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MazeUiState

        if (!maze.contentDeepEquals(other.maze)) return false
        if (userDrawingPoints != other.userDrawingPoints) return false
        if (message != other.message) return false
        if (isSuccess != other.isSuccess) return false
        if (isGameActive != other.isGameActive) return false
        if (startPoint != other.startPoint) return false
        if (endPoint != other.endPoint) return false
        if (difficulty != other.difficulty) return false
        if (showDifficultyDialog != other.showDifficultyDialog) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maze.contentDeepHashCode()
        result = 31 * result + userDrawingPoints.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + isSuccess.hashCode()
        result = 31 * result + isGameActive.hashCode()
        result = 31 * result + startPoint.hashCode()
        result = 31 * result + endPoint.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + showDifficultyDialog.hashCode()
        return result
    }
}

/**
 * Data class representing a point in the user's drawing
 */
data class DrawingPoint(
    val x: Float,
    val y: Float,
    val isNewStroke: Boolean = false
)

/**
 * Enum representing the types of cells in the maze
 */
enum class MazeCell {
    WALL,
    PATH
}
