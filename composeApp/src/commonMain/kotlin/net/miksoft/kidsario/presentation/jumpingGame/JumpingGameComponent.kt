package net.miksoft.kidsario.presentation.jumpingGame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.miksoft.kidsario.theme.GameColors

/**
 * Jumping Game component - A Chrome Dino-style endless runner for kids.
 * Kids tap anywhere to make the cute bunny jump over obstacles.
 */
@Composable
fun JumpingGameComponent(
    modifier: Modifier = Modifier,
    viewModel: JumpingGameViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = {
                    Text(
                        "Bunny Jump",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.pauseGame()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GameColors.GameBackgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score display
            ScoreDisplay(
                score = uiState.score,
                highScore = uiState.highScore,
                modifier = Modifier.padding(16.dp)
            )

            // Game area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (uiState.isPlaying) {
                            viewModel.jump()
                        } else if (!uiState.isGameOver) {
                            viewModel.startGame()
                        }
                    }
            ) {
                // Game canvas
                GameCanvas(
                    player = uiState.player,
                    obstacles = uiState.obstacles,
                    modifier = Modifier.fillMaxSize()
                )

                // Start/Game Over overlay
                if (!uiState.isPlaying) {
                    GameOverlay(
                        isGameOver = uiState.isGameOver,
                        score = uiState.score,
                        highScore = uiState.highScore,
                        onStart = { viewModel.startGame() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = if (uiState.isPlaying) "Tap anywhere to jump!" else "Tap to start",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Score display showing current score and high score
 */
@Composable
private fun ScoreDisplay(
    score: Int,
    highScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Current score
        Card(
            backgroundColor = GameColors.CardBackgroundColor,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$score",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.CircleColor
                )
            }
        }

        // High score
        Card(
            backgroundColor = GameColors.AlternateCardBackgroundColor,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Best",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$highScore",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.HeartColor
                )
            }
        }
    }
}

/**
 * Main game canvas where the game is drawn
 */
@Composable
private fun GameCanvas(
    player: Player,
    obstacles: List<Obstacle>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val groundY = size.height * 0.75f
        val groundHeight = size.height * 0.25f

        // Draw sky gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFB3E5FC), // Light blue sky
                    Color(0xFFE1F5FE)  // Very light blue
                ),
                startY = 0f,
                endY = groundY
            ),
            size = Size(size.width, groundY)
        )

        // Draw ground
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8BC34A), // Light green
                    Color(0xFF689F38)  // Darker green
                ),
                startY = groundY,
                endY = size.height
            ),
            topLeft = Offset(0f, groundY),
            size = Size(size.width, groundHeight)
        )

        // Draw grass details
        drawGrassDetails(groundY)

        // Draw clouds
        drawClouds()

        // Draw obstacles
        obstacles.forEach { obstacle ->
            drawObstacle(obstacle, groundY)
        }

        // Draw player (cute bunny)
        drawBunny(player, groundY)
    }
}

/**
 * Draw cute bunny player
 */
private fun DrawScope.drawBunny(player: Player, groundY: Float) {
    val bunnySize = size.height * 0.15f
    val bunnyX = size.width * 0.15f
    val bunnyY = groundY - bunnySize - (player.yPosition * size.height)

    // Bunny body (oval)
    drawOval(
        color = Color(0xFFFFF8E1), // Cream color
        topLeft = Offset(bunnyX, bunnyY + bunnySize * 0.3f),
        size = Size(bunnySize * 0.8f, bunnySize * 0.7f)
    )

    // Bunny head
    drawCircle(
        color = Color(0xFFFFF8E1),
        radius = bunnySize * 0.35f,
        center = Offset(bunnyX + bunnySize * 0.4f, bunnyY + bunnySize * 0.25f)
    )

    // Bunny ears
    val earPath = Path().apply {
        moveTo(bunnyX + bunnySize * 0.2f, bunnyY + bunnySize * 0.1f)
        lineTo(bunnyX + bunnySize * 0.15f, bunnyY - bunnySize * 0.25f)
        lineTo(bunnyX + bunnySize * 0.35f, bunnyY + bunnySize * 0.05f)
        close()
    }
    drawPath(earPath, Color(0xFFFFF8E1))
    
    val earPath2 = Path().apply {
        moveTo(bunnyX + bunnySize * 0.5f, bunnyY + bunnySize * 0.1f)
        lineTo(bunnyX + bunnySize * 0.55f, bunnyY - bunnySize * 0.2f)
        lineTo(bunnyX + bunnySize * 0.7f, bunnyY + bunnySize * 0.1f)
        close()
    }
    drawPath(earPath2, Color(0xFFFFF8E1))

    // Inner ear (pink)
    val innerEarPath = Path().apply {
        moveTo(bunnyX + bunnySize * 0.22f, bunnyY + bunnySize * 0.05f)
        lineTo(bunnyX + bunnySize * 0.18f, bunnyY - bunnySize * 0.15f)
        lineTo(bunnyX + bunnySize * 0.32f, bunnyY + bunnySize * 0.02f)
        close()
    }
    drawPath(innerEarPath, Color(0xFFFFCDD2))

    // Bunny eye
    drawCircle(
        color = Color(0xFF333333),
        radius = bunnySize * 0.06f,
        center = Offset(bunnyX + bunnySize * 0.5f, bunnyY + bunnySize * 0.2f)
    )

    // Eye shine
    drawCircle(
        color = Color.White,
        radius = bunnySize * 0.025f,
        center = Offset(bunnyX + bunnySize * 0.52f, bunnyY + bunnySize * 0.18f)
    )

    // Bunny nose (pink)
    drawCircle(
        color = Color(0xFFFFAB91),
        radius = bunnySize * 0.04f,
        center = Offset(bunnyX + bunnySize * 0.6f, bunnyY + bunnySize * 0.28f)
    )

    // Bunny tail (fluffy circle)
    drawCircle(
        color = Color.White,
        radius = bunnySize * 0.12f,
        center = Offset(bunnyX, bunnyY + bunnySize * 0.7f)
    )

    // Bunny cheek (blush)
    drawCircle(
        color = Color(0xFFFFCDD2).copy(alpha = 0.5f),
        radius = bunnySize * 0.08f,
        center = Offset(bunnyX + bunnySize * 0.55f, bunnyY + bunnySize * 0.32f)
    )
}

/**
 * Draw obstacle based on its type
 */
private fun DrawScope.drawObstacle(obstacle: Obstacle, groundY: Float) {
    val obstacleX = obstacle.xPosition * size.width
    val obstacleWidth = obstacle.width * size.width
    val obstacleHeight = obstacle.height * size.height
    val obstacleY = groundY - obstacleHeight

    when (obstacle.type) {
        ObstacleType.CACTUS -> drawCactus(obstacleX, obstacleY, obstacleWidth, obstacleHeight)
        ObstacleType.ROCK -> drawRock(obstacleX, obstacleY, obstacleWidth, obstacleHeight)
        ObstacleType.MUSHROOM -> drawMushroom(obstacleX, obstacleY, obstacleWidth, obstacleHeight)
        ObstacleType.FLOWER -> drawFlower(obstacleX, obstacleY, obstacleWidth, obstacleHeight)
    }
}

/**
 * Draw a cute cactus obstacle
 */
private fun DrawScope.drawCactus(x: Float, y: Float, width: Float, height: Float) {
    // Main body
    drawRoundRect(
        color = Color(0xFF66BB6A),
        topLeft = Offset(x + width * 0.3f, y + height * 0.1f),
        size = Size(width * 0.4f, height * 0.9f),
        cornerRadius = CornerRadius(width * 0.2f)
    )

    // Left arm
    drawRoundRect(
        color = Color(0xFF66BB6A),
        topLeft = Offset(x, y + height * 0.3f),
        size = Size(width * 0.35f, height * 0.25f),
        cornerRadius = CornerRadius(width * 0.1f)
    )

    // Left arm vertical part
    drawRoundRect(
        color = Color(0xFF66BB6A),
        topLeft = Offset(x, y + height * 0.25f),
        size = Size(width * 0.25f, height * 0.35f),
        cornerRadius = CornerRadius(width * 0.1f)
    )

    // Right arm
    drawRoundRect(
        color = Color(0xFF66BB6A),
        topLeft = Offset(x + width * 0.65f, y + height * 0.4f),
        size = Size(width * 0.35f, height * 0.2f),
        cornerRadius = CornerRadius(width * 0.1f)
    )

    // Cute face
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.06f,
        center = Offset(x + width * 0.4f, y + height * 0.35f)
    )
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.06f,
        center = Offset(x + width * 0.6f, y + height * 0.35f)
    )

    // Smile
    drawArc(
        color = Color(0xFF333333),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(x + width * 0.35f, y + height * 0.4f),
        size = Size(width * 0.3f, height * 0.15f)
    )
}

/**
 * Draw a cute rock obstacle
 */
private fun DrawScope.drawRock(x: Float, y: Float, width: Float, height: Float) {
    // Main rock body
    val rockPath = Path().apply {
        moveTo(x + width * 0.1f, y + height)
        lineTo(x, y + height * 0.6f)
        lineTo(x + width * 0.2f, y + height * 0.2f)
        lineTo(x + width * 0.5f, y)
        lineTo(x + width * 0.8f, y + height * 0.15f)
        lineTo(x + width, y + height * 0.5f)
        lineTo(x + width * 0.9f, y + height)
        close()
    }
    drawPath(rockPath, Color(0xFF9E9E9E))

    // Highlight
    val highlightPath = Path().apply {
        moveTo(x + width * 0.3f, y + height * 0.3f)
        lineTo(x + width * 0.5f, y + height * 0.15f)
        lineTo(x + width * 0.65f, y + height * 0.25f)
        lineTo(x + width * 0.45f, y + height * 0.4f)
        close()
    }
    drawPath(highlightPath, Color(0xFFBDBDBD))

    // Cute face
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.06f,
        center = Offset(x + width * 0.35f, y + height * 0.5f)
    )
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.06f,
        center = Offset(x + width * 0.65f, y + height * 0.5f)
    )

    // Simple mouth
    drawLine(
        color = Color(0xFF333333),
        start = Offset(x + width * 0.4f, y + height * 0.65f),
        end = Offset(x + width * 0.6f, y + height * 0.65f),
        strokeWidth = width * 0.04f
    )
}

/**
 * Draw a cute mushroom obstacle
 */
private fun DrawScope.drawMushroom(x: Float, y: Float, width: Float, height: Float) {
    // Stem
    drawRoundRect(
        color = Color(0xFFFFF8E1),
        topLeft = Offset(x + width * 0.35f, y + height * 0.4f),
        size = Size(width * 0.3f, height * 0.6f),
        cornerRadius = CornerRadius(width * 0.05f)
    )

    // Cap
    drawArc(
        color = Color(0xFFEF5350),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(x, y),
        size = Size(width, height * 0.8f)
    )

    // White spots on cap
    drawCircle(
        color = Color.White,
        radius = width * 0.1f,
        center = Offset(x + width * 0.3f, y + height * 0.2f)
    )
    drawCircle(
        color = Color.White,
        radius = width * 0.08f,
        center = Offset(x + width * 0.6f, y + height * 0.15f)
    )
    drawCircle(
        color = Color.White,
        radius = width * 0.07f,
        center = Offset(x + width * 0.75f, y + height * 0.3f)
    )

    // Cute face on stem
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.04f,
        center = Offset(x + width * 0.42f, y + height * 0.55f)
    )
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.04f,
        center = Offset(x + width * 0.58f, y + height * 0.55f)
    )

    // Blush
    drawCircle(
        color = Color(0xFFFFCDD2),
        radius = width * 0.06f,
        center = Offset(x + width * 0.35f, y + height * 0.65f)
    )
    drawCircle(
        color = Color(0xFFFFCDD2),
        radius = width * 0.06f,
        center = Offset(x + width * 0.65f, y + height * 0.65f)
    )
}

/**
 * Draw a cute flower obstacle
 */
private fun DrawScope.drawFlower(x: Float, y: Float, width: Float, height: Float) {
    // Stem
    drawRoundRect(
        color = Color(0xFF81C784),
        topLeft = Offset(x + width * 0.45f, y + height * 0.4f),
        size = Size(width * 0.1f, height * 0.6f),
        cornerRadius = CornerRadius(width * 0.02f)
    )

    // Leaf
    val leafPath = Path().apply {
        moveTo(x + width * 0.5f, y + height * 0.6f)
        quadraticTo(
            x + width * 0.8f, y + height * 0.55f,
            x + width * 0.7f, y + height * 0.7f
        )
        quadraticTo(
            x + width * 0.55f, y + height * 0.65f,
            x + width * 0.5f, y + height * 0.6f
        )
    }
    drawPath(leafPath, Color(0xFF66BB6A))

    // Petals
    val petalColor = Color(0xFFFF80AB)
    val centerX = x + width * 0.5f
    val centerY = y + height * 0.25f
    val petalRadius = width * 0.22f

    // Draw 5 petals
    for (i in 0 until 5) {
        val angle = (i * 72f - 90f) * (kotlin.math.PI / 180f).toFloat()
        val petalX = centerX + kotlin.math.cos(angle) * petalRadius * 0.7f
        val petalY = centerY + kotlin.math.sin(angle) * petalRadius * 0.7f
        drawCircle(
            color = petalColor,
            radius = petalRadius,
            center = Offset(petalX, petalY)
        )
    }

    // Flower center
    drawCircle(
        color = Color(0xFFFFEB3B),
        radius = width * 0.15f,
        center = Offset(centerX, centerY)
    )

    // Cute face
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.03f,
        center = Offset(centerX - width * 0.05f, centerY - width * 0.02f)
    )
    drawCircle(
        color = Color(0xFF333333),
        radius = width * 0.03f,
        center = Offset(centerX + width * 0.05f, centerY - width * 0.02f)
    )

    // Smile
    drawArc(
        color = Color(0xFF333333),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(centerX - width * 0.05f, centerY),
        size = Size(width * 0.1f, width * 0.06f)
    )
}

/**
 * Draw grass details on the ground
 */
private fun DrawScope.drawGrassDetails(groundY: Float) {
    val grassColor = Color(0xFF9CCC65)
    val spacing = size.width / 20

    for (i in 0 until 20) {
        val grassX = i * spacing + spacing / 2
        drawLine(
            color = grassColor,
            start = Offset(grassX, groundY),
            end = Offset(grassX - 5f, groundY - 15f),
            strokeWidth = 3f
        )
        drawLine(
            color = grassColor,
            start = Offset(grassX, groundY),
            end = Offset(grassX + 5f, groundY - 12f),
            strokeWidth = 3f
        )
    }
}

/**
 * Draw decorative clouds
 */
private fun DrawScope.drawClouds() {
    val cloudColor = Color.White.copy(alpha = 0.8f)

    // Cloud 1
    drawCircle(cloudColor, radius = 30f, center = Offset(size.width * 0.2f, size.height * 0.15f))
    drawCircle(cloudColor, radius = 25f, center = Offset(size.width * 0.25f, size.height * 0.12f))
    drawCircle(cloudColor, radius = 20f, center = Offset(size.width * 0.15f, size.height * 0.13f))

    // Cloud 2
    drawCircle(cloudColor, radius = 35f, center = Offset(size.width * 0.7f, size.height * 0.2f))
    drawCircle(cloudColor, radius = 28f, center = Offset(size.width * 0.75f, size.height * 0.17f))
    drawCircle(cloudColor, radius = 22f, center = Offset(size.width * 0.65f, size.height * 0.18f))
}

/**
 * Overlay shown when game is not playing (start screen or game over)
 */
@Composable
private fun GameOverlay(
    isGameOver: Boolean,
    score: Int,
    highScore: Int,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            backgroundColor = GameColors.CardBackgroundColor,
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isGameOver) {
                    Text(
                        text = "Game Over!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameColors.HeartColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You jumped $score obstacles!",
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    )

                    if (score >= highScore && score > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 New High Score! 🎉",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameColors.StarColor
                        )
                    }
                } else {
                    Text(
                        text = "🐰 Bunny Jump 🐰",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Help the bunny jump\nover the obstacles!",
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = GameColors.CircleColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isGameOver) "Play Again" else "Start Game",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
