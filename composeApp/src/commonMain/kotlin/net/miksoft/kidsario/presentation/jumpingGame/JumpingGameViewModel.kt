package net.miksoft.kidsario.presentation.jumpingGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Enum representing different obstacle types with kid-friendly themes
 */
enum class ObstacleType {
    CACTUS,      // Green cactus
    ROCK,        // Gray rock
    MUSHROOM,    // Colorful mushroom
    FLOWER       // Pink flower
}

/**
 * Data class representing an obstacle in the game
 */
data class Obstacle(
    val id: Int,
    val type: ObstacleType,
    var xPosition: Float,  // 0.0 to 1.0 (right to left)
    val width: Float = 0.08f,
    val height: Float = 0.12f
)

/**
 * Data class representing the player character (a cute bunny)
 */
data class Player(
    val yPosition: Float = 0f,  // 0.0 is ground level, positive is up
    val isJumping: Boolean = false,
    val jumpVelocity: Float = 0f
)

/**
 * ViewModel for the Jumping Game mini game.
 * A Chrome Dino-style endless runner where kids tap to jump over obstacles.
 */
class JumpingGameViewModel : ViewModel() {

    // State for the game
    private val _uiState = MutableStateFlow(JumpingGameUiState())
    val uiState: StateFlow<JumpingGameUiState> = _uiState.asStateFlow()

    // Game loop job
    private var gameLoopJob: Job? = null

    // Obstacle generation settings
    private var obstacleIdCounter = 0
    private var frameCounter = 0
    private var lastObstacleFrame = 0
    private val initialMinObstacleFrames = 90  // Initial minimum frames between obstacles (~1.5s at 60fps)
    private val initialMaxObstacleFrames = 180 // Initial maximum frames between obstacles (~3s at 60fps)
    private val hardMinObstacleFrames = 50     // Hardest minimum frames between obstacles (~0.8s at 60fps)
    private val hardMaxObstacleFrames = 90     // Hardest maximum frames between obstacles (~1.5s at 60fps)
    private var currentMinObstacleFrames = initialMinObstacleFrames
    private var currentMaxObstacleFrames = initialMaxObstacleFrames
    private var nextObstacleFrame = 90  // First obstacle after ~1.5s

    // Physics constants
    private val gravity = 0.0018f
    private val jumpStrength = 0.042f
    private val groundLevel = 0f

    // Game speed (increases over time)
    private var gameSpeed = 0.008f
    private val initialGameSpeed = 0.008f
    private val maxGameSpeed = 0.016f
    private val speedIncreaseRate = 0.000008f
    
    // Difficulty progression
    private val difficultyIncreaseInterval = 300  // Increase difficulty every ~5 seconds (300 frames at 60fps)

    init {
        // Start with the game ready but not running
        resetGame()
    }

    /**
     * Start the game
     */
    fun startGame() {
        if (_uiState.value.isGameOver) {
            resetGame()
        }

        _uiState.value = _uiState.value.copy(
            isPlaying = true,
            isGameOver = false
        )
        startGameLoop()
    }

    /**
     * Pause the game
     */
    fun pauseGame() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        gameLoopJob?.cancel()
    }

    /**
     * Resume the game
     */
    fun resumeGame() {
        if (!_uiState.value.isGameOver) {
            _uiState.value = _uiState.value.copy(isPlaying = true)
            startGameLoop()
        }
    }

    /**
     * Reset the game to initial state
     */
    fun resetGame() {
        gameLoopJob?.cancel()
        obstacleIdCounter = 0
        gameSpeed = initialGameSpeed
        currentMinObstacleFrames = initialMinObstacleFrames
        currentMaxObstacleFrames = initialMaxObstacleFrames

        _uiState.value = JumpingGameUiState(
            player = Player(),
            obstacles = emptyList(),
            score = 0,
            highScore = _uiState.value.highScore,
            isPlaying = false,
            isGameOver = false
        )
    }

    /**
     * Make the player jump
     */
    fun jump() {
        val currentState = _uiState.value

        // Only jump if on the ground and game is playing
        if (!currentState.player.isJumping && currentState.isPlaying && !currentState.isGameOver) {
            _uiState.value = currentState.copy(
                player = currentState.player.copy(
                    isJumping = true,
                    jumpVelocity = jumpStrength
                )
            )
        }
    }

    /**
     * Start the main game loop
     */
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        frameCounter = 0
        lastObstacleFrame = 0
        nextObstacleFrame = Random.nextInt(currentMinObstacleFrames, currentMaxObstacleFrames)

        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlaying && !_uiState.value.isGameOver) {
                updateGame()
                delay(16) // ~60 FPS
            }
        }
    }

    /**
     * Update game state (called every frame)
     */
    private fun updateGame() {
        val currentState = _uiState.value
        if (!currentState.isPlaying || currentState.isGameOver) return

        // Update player physics
        val updatedPlayer = updatePlayerPhysics(currentState.player)

        // Update obstacles
        val (updatedObstacles, obstaclesJumped) = updateObstacles(currentState.obstacles)

        // Check for collisions
        val collision = checkCollision(updatedPlayer, updatedObstacles)

        // Calculate new score
        val newScore = currentState.score + obstaclesJumped

        // Update high score if needed
        val newHighScore = maxOf(currentState.highScore, newScore)

        // Generate new obstacles
        val obstaclesWithNew = maybeGenerateObstacle(updatedObstacles)

        // Gradually increase difficulty over time
        updateDifficulty()

        if (collision) {
            // Game over
            _uiState.value = currentState.copy(
                player = updatedPlayer,
                obstacles = obstaclesWithNew,
                score = newScore,
                highScore = newHighScore,
                isPlaying = false,
                isGameOver = true
            )
            gameLoopJob?.cancel()
        } else {
            _uiState.value = currentState.copy(
                player = updatedPlayer,
                obstacles = obstaclesWithNew,
                score = newScore,
                highScore = newHighScore
            )
        }
    }

    /**
     * Update player physics (jumping/falling)
     */
    private fun updatePlayerPhysics(player: Player): Player {
        if (!player.isJumping && player.yPosition <= groundLevel) {
            return player
        }

        val newVelocity = player.jumpVelocity - gravity
        var newYPosition = player.yPosition + newVelocity

        // Check if landed
        if (newYPosition <= groundLevel) {
            newYPosition = groundLevel
            return Player(
                yPosition = groundLevel,
                isJumping = false,
                jumpVelocity = 0f
            )
        }

        return player.copy(
            yPosition = newYPosition,
            jumpVelocity = newVelocity
        )
    }

    /**
     * Update obstacles (move left and remove off-screen ones)
     * Returns updated obstacles and count of obstacles that were successfully jumped
     */
    private fun updateObstacles(obstacles: List<Obstacle>): Pair<List<Obstacle>, Int> {
        var jumped = 0
        val updatedObstacles = obstacles.mapNotNull { obstacle ->
            val newX = obstacle.xPosition - gameSpeed

            // Remove obstacles that have gone off screen
            if (newX + obstacle.width < 0) {
                jumped++
                null
            } else {
                obstacle.copy(xPosition = newX)
            }
        }

        return Pair(updatedObstacles, jumped)
    }

    /**
     * Check for collision between player and obstacles
     */
    private fun checkCollision(player: Player, obstacles: List<Obstacle>): Boolean {
        // Player hitbox (positioned at left side of screen)
        val playerLeft = 0.12f
        val playerRight = 0.20f
        val playerBottom = player.yPosition  // Player's bottom is at their current Y position
        val playerTop = player.yPosition + 0.15f  // Player height

        for (obstacle in obstacles) {
            val obstacleLeft = obstacle.xPosition
            val obstacleRight = obstacle.xPosition + obstacle.width
            val obstacleBottom = groundLevel
            val obstacleTop = obstacle.height

            // Check AABB collision with some padding for kid-friendliness
            val horizontalOverlap = playerRight > obstacleLeft + 0.01f && playerLeft < obstacleRight - 0.01f
            val verticalOverlap = playerTop > obstacleBottom && playerBottom < obstacleTop - 0.02f

            if (horizontalOverlap && verticalOverlap) {
                return true
            }
        }

        return false
    }

    /**
     * Update game difficulty based on time played
     */
    private fun updateDifficulty() {
        // Gradually increase game speed
        if (gameSpeed < maxGameSpeed) {
            gameSpeed += speedIncreaseRate
        }
        
        // Gradually decrease obstacle spawn intervals (more frequent obstacles)
        // Calculate progress (0.0 to 1.0) based on game speed
        val speedProgress = (gameSpeed - initialGameSpeed) / (maxGameSpeed - initialGameSpeed)
        
        // Interpolate obstacle frame intervals based on progress
        currentMinObstacleFrames = (initialMinObstacleFrames - 
            ((initialMinObstacleFrames - hardMinObstacleFrames) * speedProgress)).toInt()
            .coerceAtLeast(hardMinObstacleFrames)
        currentMaxObstacleFrames = (initialMaxObstacleFrames - 
            ((initialMaxObstacleFrames - hardMaxObstacleFrames) * speedProgress)).toInt()
            .coerceAtLeast(hardMaxObstacleFrames)
    }

    /**
     * Maybe generate a new obstacle based on frame count
     */
    private fun maybeGenerateObstacle(obstacles: List<Obstacle>): List<Obstacle> {
        frameCounter++

        if (frameCounter >= nextObstacleFrame) {
            lastObstacleFrame = frameCounter
            // Set next obstacle spawn time using current difficulty settings
            nextObstacleFrame = frameCounter + Random.nextInt(currentMinObstacleFrames, currentMaxObstacleFrames)

            val obstacleType = ObstacleType.entries.random()
            val newObstacle = Obstacle(
                id = obstacleIdCounter++,
                type = obstacleType,
                xPosition = 1.0f  // Start from right side of screen
            )

            return obstacles + newObstacle
        }

        return obstacles
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

/**
 * Data class representing the UI state for the Jumping Game
 */
data class JumpingGameUiState(
    val player: Player = Player(),
    val obstacles: List<Obstacle> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false
)
