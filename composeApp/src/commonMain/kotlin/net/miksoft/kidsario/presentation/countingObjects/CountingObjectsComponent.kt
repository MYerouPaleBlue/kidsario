package net.miksoft.kidsario.presentation.countingObjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import net.miksoft.kidsario.theme.GameColors

/**
 * Counting Objects game component that displays random shapes and answer options.
 * Updated with a more playful, kid-friendly design.
 */
@Composable
fun CountingObjectsComponent(
    modifier: Modifier = Modifier,
    viewModel: CountingObjectsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Difficulty selection dialog
    if (uiState.showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDifficultyDialog() },
            title = {
                Text(
                    "Select Difficulty",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { difficulty ->
                        Button(
                            onClick = { viewModel.changeDifficulty(difficulty) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (difficulty == uiState.difficulty) 
                                    MaterialTheme.colors.primary else MaterialTheme.colors.surface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${difficulty.displayName} (${difficulty.range.first}-${difficulty.range.last})",
                                color = if (difficulty == uiState.difficulty) 
                                    Color.White else MaterialTheme.colors.onSurface,
                                fontWeight = if (difficulty == uiState.difficulty) 
                                    FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.toggleDifficultyDialog() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = { 
                    Text(
                        "Counting Objects",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Back arrow icon would go here
                        Text(
                            "<",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Cog button for difficulty settings
                    IconButton(onClick = { viewModel.toggleDifficultyDialog() }) {
                        Text(
                            "⚙️",
                            fontSize = 24.sp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(GameColors.GameBackgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions with playful design
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameColors.CardBackgroundColor,
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "How many shapes do you see?",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Display area for shapes with a playful container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                backgroundColor = Color.White,
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Draw the shapes
                    ShapesCanvas(
                        count = uiState.objectCount,
                        shapeType = uiState.shapeType,
                        clickedObjects = uiState.clickedObjects,
                        onObjectClicked = { viewModel.onObjectClicked(it) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Feedback message with playful design
                    if (uiState.message != null) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300)),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            val backgroundColor = if (uiState.isCorrectAnswer == true)
                                GameColors.CorrectColor else GameColors.IncorrectColor

                            Surface(
                                color = backgroundColor,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = uiState.message!!,
                                    color = Color.White,
                                    style = MaterialTheme.typography.h5.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(24.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Answer buttons with playful design
            if (uiState.answerOptions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // First row of buttons (first 2 options)
                    uiState.answerOptions.take(2).forEachIndexed { index, answer ->
                        AnswerButton(
                            answer = answer,
                            onClick = { viewModel.onAnswerSelected(answer) },
                            enabled = uiState.isGameActive,
                            backgroundColor = if (index == 0) GameColors.CircleColor else GameColors.SquareColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Second row of buttons (last 2 options)
                    uiState.answerOptions.drop(2).forEachIndexed { index, answer ->
                        AnswerButton(
                            answer = answer,
                            onClick = { viewModel.onAnswerSelected(answer) },
                            enabled = uiState.isGameActive,
                            backgroundColor = if (index == 0) GameColors.RectangleColor else GameColors.TriangleColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Try again button with playful design (only shown for incorrect answers)
            AnimatedVisibility(
                visible = uiState.isCorrectAnswer == false,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Button(
                    onClick = { viewModel.resetCurrentRound() },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.CircleColor),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Try Again",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Button for selecting an answer with playful design.
 */
@Composable
private fun AnswerButton(
    answer: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = answer.toString(),
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * Canvas for drawing the shapes.
 */
@Composable
private fun ShapesCanvas(
    count: Int,
    shapeType: ShapeType,
    clickedObjects: Set<Int> = emptySet(),
    onObjectClicked: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Remember the positions to ensure consistency between hit detection and rendering
    val positions = remember(count) {
        val canvasSize = 1000f // Use a placeholder size, will be replaced in drawing
        generateShapePositions(
            count = count,
            canvasWidth = canvasSize,
            canvasHeight = canvasSize,
            shapeSize = canvasSize / 7
        )
    }

    Canvas(modifier = modifier.pointerInput(count, positions) {
        detectTapGestures { tapOffset ->
            // Get actual canvas dimensions
            val canvasWidth = size.width
            val canvasHeight = size.height
            val shapeSize = minOf(canvasWidth, canvasHeight) / 7

            // Scale the remembered positions to the actual canvas size
            val scaledPositions = positions.map { position ->
                Offset(
                    x = position.x * canvasWidth / 1000f,
                    y = position.y * canvasHeight / 1000f
                )
            }

            println("[DEBUG_LOG] Tap detected at: $tapOffset")
            // Check if any shape was clicked
            scaledPositions.forEachIndexed { index, position ->
                // Check if the click is within the shape's bounds
                val isClicked = when (shapeType) {
                    ShapeType.CIRCLE -> {
                        val center = Offset(position.x + shapeSize / 2, position.y + shapeSize / 2)
                        val distance = kotlin.math.sqrt(
                            (tapOffset.x - center.x) * (tapOffset.x - center.x) +
                            (tapOffset.y - center.y) * (tapOffset.y - center.y)
                        )
                        distance <= shapeSize / 2
                    }
                    ShapeType.RECTANGLE -> {
                        tapOffset.x >= position.x &&
                        tapOffset.x <= position.x + shapeSize &&
                        tapOffset.y >= position.y &&
                        tapOffset.y <= position.y + shapeSize * 0.6f
                    }
                    ShapeType.SQUARE -> {
                        tapOffset.x >= position.x &&
                        tapOffset.x <= position.x + shapeSize &&
                        tapOffset.y >= position.y &&
                        tapOffset.y <= position.y + shapeSize
                    }
                    ShapeType.TRIANGLE -> {
                        // Simple triangle hit detection
                        val x1 = position.x + shapeSize / 2
                        val y1 = position.y
                        val x2 = position.x
                        val y2 = position.y + shapeSize
                        val x3 = position.x + shapeSize
                        val y3 = position.y + shapeSize

                        // Check if point is inside triangle using barycentric coordinates
                        val d1 = (tapOffset.x - x2) * (y1 - y2) - (x1 - x2) * (tapOffset.y - y2)
                        val d2 = (tapOffset.x - x3) * (y2 - y3) - (x2 - x3) * (tapOffset.y - y3)
                        val d3 = (tapOffset.x - x1) * (y3 - y1) - (x3 - x1) * (tapOffset.y - y1)

                        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
                        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)

                        !(hasNeg && hasPos)
                    }
                }

                if (isClicked) {
                    println("[DEBUG_LOG] Shape detected as clicked: $index")
                    println("[DEBUG_LOG] Shape position: $position")
                    println("[DEBUG_LOG] Tap position: $tapOffset")
                    onObjectClicked(index)
                    return@detectTapGestures
                }
            }
            println("[DEBUG_LOG] No shape was hit by the tap")
        }
    }) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define shape size (not too big, not too small)
        val shapeSize = minOf(canvasWidth, canvasHeight) / 7

        // Scale the remembered positions to the actual canvas size
        val scaledPositions = positions.map { position ->
            Offset(
                x = position.x * canvasWidth / 1000f,
                y = position.y * canvasHeight / 1000f
            )
        }

        // Draw each shape at its position with playful colors
        scaledPositions.forEachIndexed { index, position ->
            // Determine if this object has been clicked
            val isClicked = clickedObjects.contains(index)
            // Use grey color for clicked objects, otherwise use the default color
            val fillColor = if (isClicked) Color.Gray else when (shapeType) {
                ShapeType.CIRCLE -> GameColors.CircleColor
                ShapeType.RECTANGLE -> GameColors.RectangleColor
                ShapeType.SQUARE -> GameColors.SquareColor
                ShapeType.TRIANGLE -> GameColors.TriangleColor
            }

            when (shapeType) {
                ShapeType.CIRCLE -> {
                    // Draw filled circle with appropriate color
                    drawCircle(
                        color = fillColor,
                        radius = shapeSize / 2,
                        center = Offset(position.x + shapeSize / 2, position.y + shapeSize / 2)
                    )
                    // Draw border
                    drawCircle(
                        color = Color.DarkGray.copy(alpha = 0.7f), // Softer border
                        radius = shapeSize / 2,
                        center = Offset(position.x + shapeSize / 2, position.y + shapeSize / 2),
                        style = Stroke(width = 3f) // Slightly thicker for better visibility
                    )
                }
                ShapeType.RECTANGLE -> {
                    // Draw filled rectangle with appropriate color
                    drawRect(
                        color = fillColor,
                        topLeft = position,
                        size = Size(shapeSize, shapeSize * 0.6f)
                    )
                    // Draw border
                    drawRect(
                        color = Color.DarkGray.copy(alpha = 0.7f), // Softer border
                        topLeft = position,
                        size = Size(shapeSize, shapeSize * 0.6f),
                        style = Stroke(width = 3f) // Slightly thicker for better visibility
                    )
                }
                ShapeType.SQUARE -> {
                    // Draw filled square with appropriate color
                    drawRect(
                        color = fillColor,
                        topLeft = position,
                        size = Size(shapeSize, shapeSize)
                    )
                    // Draw border
                    drawRect(
                        color = Color.DarkGray.copy(alpha = 0.7f), // Softer border
                        topLeft = position,
                        size = Size(shapeSize, shapeSize),
                        style = Stroke(width = 3f) // Slightly thicker for better visibility
                    )
                }
                ShapeType.TRIANGLE -> {
                    // Approximate a triangle with a path
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(position.x + shapeSize / 2, position.y)
                        lineTo(position.x, position.y + shapeSize)
                        lineTo(position.x + shapeSize, position.y + shapeSize)
                        close()
                    }
                    // Draw filled triangle with appropriate color
                    drawPath(
                        path = path,
                        color = fillColor
                    )
                    // Draw border
                    drawPath(
                        path = path,
                        color = Color.DarkGray.copy(alpha = 0.7f), // Softer border
                        style = Stroke(width = 3f) // Slightly thicker for better visibility
                    )
                }
            }
        }
    }
}

/**
 * Generate random positions for the shapes, ensuring they don't overlap.
 */
private fun generateShapePositions(
    count: Int,
    canvasWidth: Float,
    canvasHeight: Float,
    shapeSize: Float
): List<Offset> {
    val positions = mutableListOf<Offset>()
    val random = Random.Default
    val maxAttempts = 100 // Prevent infinite loop

    // Place shapes one by one, ensuring they don't overlap
    for (i in 0 until count) {
        var attempts = 0
        var validPosition = false
        var x: Float
        var y: Float

        // Try to find a non-overlapping position
        while (!validPosition && attempts < maxAttempts) {
            x = random.nextFloat() * (canvasWidth - shapeSize)
            y = random.nextFloat() * (canvasHeight - shapeSize)
            val newPosition = Offset(x, y)

            // Check if this position overlaps with any existing shape
            validPosition = true
            for (existingPosition in positions) {
                // Calculate distance between centers
                val dx = newPosition.x - existingPosition.x
                val dy = newPosition.y - existingPosition.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                // Use a minimum distance to prevent overlapping
                // For different shapes, we use a safe margin to ensure they don't overlap
                val minDistance = shapeSize * 1.2f  // 20% extra margin for better visual separation
                if (distance < minDistance) {
                    validPosition = false
                    break
                }
            }

            // If position is valid, add it to the list
            if (validPosition) {
                positions.add(newPosition)
            }

            attempts++
        }

        // If we couldn't find a valid position after max attempts, just place it randomly
        // This is a fallback to ensure we always have the correct count of shapes
        if (!validPosition) {
            val x = random.nextFloat() * (canvasWidth - shapeSize)
            val y = random.nextFloat() * (canvasHeight - shapeSize)
            positions.add(Offset(x, y))
        }
    }

    return positions
}
