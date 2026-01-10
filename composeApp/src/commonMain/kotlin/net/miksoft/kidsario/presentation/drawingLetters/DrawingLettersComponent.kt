package net.miksoft.kidsario.presentation.drawingLetters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import net.miksoft.kidsario.theme.GameColors

/**
 * Drawing Letters game component that displays dotted letters/numbers for the user to trace.
 * Updated with a more playful, kid-friendly design.
 */
@Composable
fun DrawingLettersComponent(
    modifier: Modifier = Modifier,
    viewModel: DrawingLettersViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Settings dialog
    if (uiState.showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSettingsDialog() },
            title = {
                Text(
                    "Select Character Set",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LetterType.values().forEach { type ->
                        Button(
                            onClick = { viewModel.changeLetterType(type) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (type == uiState.letterType) 
                                    MaterialTheme.colors.primary else MaterialTheme.colors.surface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = type.displayName,
                                color = if (type == uiState.letterType) 
                                    Color.White else MaterialTheme.colors.onSurface,
                                fontWeight = if (type == uiState.letterType) 
                                    FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.toggleSettingsDialog() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = { 
                    Text(
                        "Drawing Letters",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            "<",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSettingsDialog() }) {
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
            modifier = Modifier
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
                    text = "Trace the dotted letter or number",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Display the current character to trace with playful design
            Card(
                modifier = Modifier.padding(8.dp),
                backgroundColor = GameColors.AlternateCardBackgroundColor,
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = uiState.character.toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(24.dp)
                )
            }

            // Drawing area with playful container
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
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw the dotted letter/number and capture user's drawing
                    DrawingCanvas(
                        character = uiState.character,
                        userDrawingPoints = uiState.userDrawingPoints,
                        onDrawingPointAdded = { x, y, isNewStroke ->
                            viewModel.addDrawingPoint(x, y, isNewStroke)
                        },
                        isEnabled = uiState.isGameActive,
                        modifier = Modifier.aspectRatio(1f)
                    )

                    // Feedback message with playful design
                    if (uiState.message != null) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(300))
                            ) {
                                val backgroundColor = if (uiState.isCorrectDrawing == true) 
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
            }

            // Control buttons with playful design
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Clear button
                Button(
                    onClick = { viewModel.clearDrawing() },
                    enabled = uiState.isGameActive && uiState.userDrawingPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.SquareColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Clear",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Check button
                Button(
                    onClick = { viewModel.checkDrawing() },
                    enabled = uiState.isGameActive && uiState.userDrawingPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.CircleColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Check",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Try again button with playful design (only shown for incorrect drawings)
            AnimatedVisibility(
                visible = uiState.isCorrectDrawing == false,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Button(
                    onClick = { viewModel.resetCurrentRound() },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.RectangleColor),
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
 * Canvas for drawing the dotted letter/number and capturing the user's drawing.
 */
@Composable
private fun DrawingCanvas(
    character: Char,
    userDrawingPoints: List<DrawingPoint>,
    onDrawingPointAdded: (Float, Float, Boolean) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Track whether the user is currently drawing
    var isDrawing by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .pointerInput(isEnabled) {
                if (!isEnabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        isDrawing = true
                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                            onDrawingPointAdded(offset.x / canvasSize.width, offset.y / canvasSize.height, true)
                        }
                    },
                    onDragEnd = {
                        isDrawing = false
                    },
                    onDragCancel = {
                        isDrawing = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                            onDrawingPointAdded(
                                change.position.x / canvasSize.width,
                                change.position.y / canvasSize.height,
                                false
                            )
                        }
                    }
                )
            }
    ) {
        canvasSize = size
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw the dotted letter/number
        drawLetterDots(character, canvasWidth, canvasHeight)

        // Draw the user's drawing with playful color
        if (userDrawingPoints.isNotEmpty()) {
            var currentPath = Path()
            // Denormalize the first point
            var currentPoint = userDrawingPoints.first()
            currentPath.moveTo(currentPoint.x * canvasWidth, currentPoint.y * canvasHeight)

            for (i in 1 until userDrawingPoints.size) {
                val point = userDrawingPoints[i]

                if (point.isNewStroke) {
                    // Draw the completed path with playful color
                    drawPath(
                        path = currentPath,
                        color = GameColors.DrawingColor,
                        style = Stroke(
                            width = 10f, // Slightly thicker for better visibility
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Start a new path
                    currentPath = Path()
                    // Denormalize
                    currentPath.moveTo(point.x * canvasWidth, point.y * canvasHeight)
                } else {
                    // Continue the current path
                    // Denormalize
                    currentPath.lineTo(point.x * canvasWidth, point.y * canvasHeight)
                }
            }

            // Draw the final path with playful color
            drawPath(
                path = currentPath,
                color = GameColors.DrawingColor,
                style = Stroke(
                    width = 10f, // Slightly thicker for better visibility
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/**
 * Draw dots to form the shape of a letter or number.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLetterDots(
    character: Char,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2
    val size = minOf(canvasWidth, canvasHeight) * 0.6f
    val dotRadius = 4f

    // Standard positions
    val topY = centerY - size / 2
    val bottomY = centerY + size / 2
    val leftX = centerX - size / 3
    val rightX = centerX + size / 3
    val midY = (topY + bottomY) / 2
    val quarterY = topY + (bottomY - topY) / 4
    val threeQuarterY = bottomY - (bottomY - topY) / 4

    // Implementation that draws dots in the shape of each character
    when (character) {
        'A' -> {
            // Left diagonal
            drawDotsAlongLine(centerX, topY, leftX, bottomY, dotRadius)
            // Right diagonal
            drawDotsAlongLine(centerX, topY, rightX, bottomY, dotRadius)
            // Crossbar
            drawDotsAlongLine(
                leftX + (centerX - leftX) * 0.3f, 
                midY, 
                rightX - (rightX - centerX) * 0.3f, 
                midY, 
                dotRadius
            )
        }
        'B' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Top curve
            drawDotsAlongLine(leftX, topY, rightX - size / 10, topY, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 10, rightX, midY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, midY, leftX, midY, dotRadius)
            // Bottom curve
            drawDotsAlongLine(leftX, midY, rightX - size / 10, midY, dotRadius)
            drawDotsAlongLine(rightX, midY + size / 10, rightX, bottomY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, bottomY, leftX, bottomY, dotRadius)
        }
        'C' -> {
            // Curve
            drawDotsAlongLine(rightX, topY + size / 10, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, leftX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 10, dotRadius)
        }
        'D' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Curve
            drawDotsAlongLine(leftX, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, leftX, bottomY, dotRadius)
        }
        'E' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Horizontal lines
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'F' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Horizontal lines
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX - size / 6, midY, dotRadius)
        }
        'G' -> {
            // Curve
            drawDotsAlongLine(rightX, topY + size / 10, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, leftX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 10, dotRadius)
            // G's tail
            drawDotsAlongLine(rightX, bottomY - size / 10, rightX, midY, dotRadius)
            drawDotsAlongLine(rightX, midY, centerX, midY, dotRadius)
        }
        'H' -> {
            // Vertical lines
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            // Horizontal line
            drawDotsAlongLine(leftX, midY, rightX, midY, dotRadius)
        }
        'I' -> {
            // Vertical line
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
            // Horizontal lines
            drawDotsAlongLine(centerX - size / 6, topY, centerX + size / 6, topY, dotRadius)
            drawDotsAlongLine(centerX - size / 6, bottomY, centerX + size / 6, bottomY, dotRadius)
        }
        'J' -> {
            // Vertical line
            drawDotsAlongLine(rightX - size / 10, topY, rightX - size / 10, bottomY - size / 6, dotRadius)
            // Curve
            drawDotsAlongLine(rightX - size / 10, bottomY - size / 6, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, leftX, bottomY - size / 6, dotRadius)
            // Top line
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY, dotRadius)
        }
        'K' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Diagonals
            drawDotsAlongLine(leftX, midY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX, bottomY, dotRadius)
        }
        'L' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Horizontal line
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'M' -> {
            // Vertical lines
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            // Diagonals
            drawDotsAlongLine(leftX, topY, centerX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, midY + size / 6, dotRadius)
        }
        'N' -> {
            // Vertical lines
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            // Diagonal
            drawDotsAlongLine(leftX, topY, rightX, bottomY, dotRadius)
        }
        'O' -> {
            // Top curve
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            // Bottom curve
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            // Left curve
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            // Right curve
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            // Top-left corner
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            // Top-right corner
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            // Bottom-left corner
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            // Bottom-right corner
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
        }
        'P' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Top curve
            drawDotsAlongLine(leftX, topY, rightX - size / 10, topY, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 10, rightX, midY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, midY, leftX, midY, dotRadius)
        }
        'Q' -> {
            // O shape
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            // Q's tail
            drawDotsAlongLine(centerX, bottomY - size / 10, rightX + size / 10, bottomY + size / 10, dotRadius)
        }
        'R' -> {
            // Vertical line
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            // Top curve
            drawDotsAlongLine(leftX, topY, rightX - size / 10, topY, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 10, rightX, midY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, midY, leftX, midY, dotRadius)
            // Diagonal
            drawDotsAlongLine(leftX, midY, rightX, bottomY, dotRadius)
        }
        'S' -> {
            // Top curve
            drawDotsAlongLine(rightX, topY + size / 6, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, leftX, topY + size / 6, dotRadius)
            // Middle curve
            drawDotsAlongLine(leftX, topY + size / 6, leftX, midY - size / 10, dotRadius)
            drawDotsAlongLine(leftX, midY - size / 10, leftX + size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, rightX, midY + size / 10, dotRadius)
            // Bottom curve
            drawDotsAlongLine(rightX, midY + size / 10, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, leftX, bottomY - size / 6, dotRadius)
        }
        'T' -> {
            // Horizontal line
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            // Vertical line
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
        }
        'U' -> {
            // Vertical lines
            drawDotsAlongLine(leftX, topY, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY - size / 6, dotRadius)
            // Bottom curve
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
        }
        'V' -> {
            // Diagonals
            drawDotsAlongLine(leftX, topY, centerX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, bottomY, dotRadius)
        }
        'W' -> {
            // Outer diagonals
            drawDotsAlongLine(leftX, topY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX - size / 6, bottomY, dotRadius)
            // Inner diagonals
            drawDotsAlongLine(leftX + size / 6, bottomY, centerX, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, centerX, midY, dotRadius)
        }
        'X' -> {
            // Diagonals
            drawDotsAlongLine(leftX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, leftX, bottomY, dotRadius)
        }
        'Y' -> {
            // Upper diagonals
            drawDotsAlongLine(leftX, topY, centerX, midY, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, midY, dotRadius)
            // Lower vertical
            drawDotsAlongLine(centerX, midY, centerX, bottomY, dotRadius)
        }
        'Z' -> {
            // Horizontal lines
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
            // Diagonal
            drawDotsAlongLine(rightX, topY, leftX, bottomY, dotRadius)
        }
        '0' -> {
            // Similar to O but with a diagonal
            // Top curve
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            // Bottom curve
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            // Left curve
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            // Right curve
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            // Top-left corner
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            // Top-right corner
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            // Bottom-left corner
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            // Bottom-right corner
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            // Diagonal (optional for stylized zero)
            // drawDotsAlongLine(leftX, topY, rightX, bottomY, dotRadius)
        }
        '1' -> {
            // Vertical line
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
            // Diagonal for the top
            drawDotsAlongLine(centerX - size / 6, topY + size / 6, centerX, topY, dotRadius)
            // Base
            drawDotsAlongLine(centerX - size / 6, bottomY, centerX + size / 6, bottomY, dotRadius)
        }
        '2' -> {
            // Top curve
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            // Right to middle
            drawDotsAlongLine(rightX, topY + size / 6, rightX, midY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY - size / 6, rightX - size / 6, midY, dotRadius)
            // Middle to bottom-left
            drawDotsAlongLine(rightX - size / 6, midY, leftX + size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, leftX, midY + size / 6, dotRadius)
            // Bottom-left to bottom
            drawDotsAlongLine(leftX, midY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            // Bottom line
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX, bottomY, dotRadius)
        }
        '3' -> {
            // Top curve
            drawDotsAlongLine(leftX, topY + size / 10, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            // Right side
            drawDotsAlongLine(rightX, topY + size / 6, rightX, midY - size / 6, dotRadius)
            // Middle curve
            drawDotsAlongLine(rightX, midY - size / 6, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, leftX + size / 6, midY, dotRadius)
            // Right side (bottom half)
            drawDotsAlongLine(rightX - size / 6, midY, rightX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY + size / 6, rightX, bottomY - size / 6, dotRadius)
            // Bottom curve
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, leftX, bottomY - size / 10, dotRadius)
        }
        '4' -> {
            // Vertical line on right
            drawDotsAlongLine(rightX - size / 6, topY, rightX - size / 6, bottomY, dotRadius)
            // Horizontal line
            drawDotsAlongLine(leftX, midY, rightX, midY, dotRadius)
            // Diagonal
            drawDotsAlongLine(leftX, midY, rightX - size / 6, topY, dotRadius)
        }
        '5' -> {
            // Top horizontal
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            // Left vertical (top half)
            drawDotsAlongLine(leftX, topY, leftX, midY, dotRadius)
            // Middle horizontal
            drawDotsAlongLine(leftX, midY, rightX - size / 6, midY, dotRadius)
            // Bottom curve
            drawDotsAlongLine(rightX - size / 6, midY, rightX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, leftX, bottomY - size / 6, dotRadius)
        }
        '6' -> {
            // Left curve
            drawDotsAlongLine(rightX, topY, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, leftX, topY + size / 6, dotRadius)
            // Left vertical
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            // Bottom curve
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            // Right vertical (bottom half)
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX, midY + size / 6, dotRadius)
            // Middle curve
            drawDotsAlongLine(rightX, midY + size / 6, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, leftX, midY, dotRadius)
        }
        '7' -> {
            // Top horizontal
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            // Diagonal
            drawDotsAlongLine(rightX, topY, leftX + size / 6, bottomY, dotRadius)
            // Optional middle horizontal
            // drawDotsAlongLine(leftX + size / 3, midY, rightX - size / 6, midY, dotRadius)
        }
        '8' -> {
            // Top circle
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, midY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY - size / 6, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, leftX + size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, leftX, midY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, midY - size / 6, leftX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)

            // Bottom circle
            drawDotsAlongLine(leftX + size / 6, midY, leftX, midY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, midY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, bottomY - size / 6, rightX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY + size / 6, rightX - size / 6, midY, dotRadius)
        }
        '9' -> {
            // Top circle
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, midY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, midY - size / 6, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, leftX + size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, leftX, midY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, midY - size / 6, leftX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)

            // Right vertical
            drawDotsAlongLine(rightX, midY, rightX, bottomY, dotRadius)
        }
        // Greek Letters
        'Α' -> { // Alpha - Same as A
            drawDotsAlongLine(centerX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(centerX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(leftX + (centerX - leftX) * 0.3f, midY, rightX - (rightX - centerX) * 0.3f, midY, dotRadius)
        }
        'Β' -> { // Beta - Same as B
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY, rightX - size / 10, topY, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 10, rightX, midY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, midY, leftX, midY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX - size / 10, midY, dotRadius)
            drawDotsAlongLine(rightX, midY + size / 10, rightX, bottomY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, bottomY, leftX, bottomY, dotRadius)
        }
        'Γ' -> { // Gamma
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
        }
        'Δ' -> { // Delta - Triangle
            drawDotsAlongLine(centerX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(centerX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'Ε' -> { // Epsilon - Same as E
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'Ζ' -> { // Zeta - Same as Z
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(rightX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'Η' -> { // Eta - Same as H
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX, midY, dotRadius)
        }
        'Θ' -> { // Theta
            // O part
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            // Middle line
            drawDotsAlongLine(leftX + size / 6, midY, rightX - size / 6, midY, dotRadius)
        }
        'Ι' -> { // Iota - Simple vertical line
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
        }
        'Κ' -> { // Kappa - Same as K
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX, midY, rightX, bottomY, dotRadius)
        }
        'Λ' -> { // Lambda
            drawDotsAlongLine(centerX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(centerX, topY, rightX, bottomY, dotRadius)
        }
        'Μ' -> { // Mu - Same as M
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY, centerX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, midY + size / 6, dotRadius)
        }
        'Ν' -> { // Nu - Same as N
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY, rightX, bottomY, dotRadius)
        }
        'Ξ' -> { // Xi
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'Ο' -> { // Omicron - Same as O
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
        }
        'Π' -> { // Pi
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, topY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX - size / 6, bottomY, dotRadius)
        }
        'Ρ' -> { // Rho - Same as P
            drawDotsAlongLine(leftX, topY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY, rightX - size / 10, topY, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 10, rightX, midY - size / 10, dotRadius)
            drawDotsAlongLine(rightX - size / 10, midY, leftX, midY, dotRadius)
        }
        'Σ' -> { // Sigma
            drawDotsAlongLine(rightX, topY, leftX, topY, dotRadius)
            drawDotsAlongLine(leftX, topY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, midY, leftX, bottomY, dotRadius)
            drawDotsAlongLine(leftX, bottomY, rightX, bottomY, dotRadius)
        }
        'Τ' -> { // Tau - Same as T
            drawDotsAlongLine(leftX, topY, rightX, topY, dotRadius)
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
        }
        'Υ' -> { // Upsilon - Same as Y
            drawDotsAlongLine(leftX, topY, centerX, midY, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, midY, dotRadius)
            drawDotsAlongLine(centerX, midY, centerX, bottomY, dotRadius)
        }
        'Φ' -> { // Phi
            // Circle
            drawDotsAlongLine(leftX + size / 6, topY, rightX - size / 6, topY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, bottomY, rightX - size / 6, bottomY, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY + size / 6, rightX, bottomY - size / 6, dotRadius)
            drawDotsAlongLine(leftX, topY + size / 6, leftX + size / 6, topY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, topY, rightX, topY + size / 6, dotRadius)
            drawDotsAlongLine(leftX, bottomY - size / 6, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY - size / 6, dotRadius)
            // Vertical line
            drawDotsAlongLine(centerX, topY - size / 6, centerX, bottomY + size / 6, dotRadius)
        }
        'Χ' -> { // Chi - Same as X
            drawDotsAlongLine(leftX, topY, rightX, bottomY, dotRadius)
            drawDotsAlongLine(rightX, topY, leftX, bottomY, dotRadius)
        }
        'Ψ' -> { // Psi
            drawDotsAlongLine(leftX, topY, centerX, midY + size / 6, dotRadius)
            drawDotsAlongLine(rightX, topY, centerX, midY + size / 6, dotRadius)
            drawDotsAlongLine(centerX, topY, centerX, bottomY, dotRadius)
        }
        'Ω' -> { // Omega
            drawDotsAlongLine(leftX, bottomY, leftX + size / 6, bottomY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX, bottomY, dotRadius)
            // Curve
            drawDotsAlongLine(leftX + size / 6, bottomY, leftX + size / 6, midY, dotRadius)
            drawDotsAlongLine(rightX - size / 6, bottomY, rightX - size / 6, midY, dotRadius)
            drawDotsAlongLine(leftX + size / 6, midY, centerX, topY, dotRadius)
            drawDotsAlongLine(centerX, topY, rightX - size / 6, midY, dotRadius)
        }
        else -> {
            // For any other characters, draw a simple shape
            val radius = size / 3
            val numDots = 24

            for (i in 0 until numDots) {
                val angle = (i.toFloat() / numDots) * 2 * PI
                val x = centerX + (radius * cos(angle)).toFloat()
                val y = centerY + (radius * sin(angle)).toFloat()

                drawCircle(
                    color = GameColors.DottedLineColor,
                    radius = dotRadius * 1.2f, // Slightly larger for better visibility
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Draw dots along a line from (x1, y1) to (x2, y2).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDotsAlongLine(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    dotRadius: Float
) {
    val distance = kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    val numDots = (distance / (dotRadius * 4)).toInt().coerceAtLeast(2)

    for (i in 0 until numDots) {
        val t = i.toFloat() / (numDots - 1)
        val x = x1 + t * (x2 - x1)
        val y = y1 + t * (y2 - y1)

        drawCircle(
            color = GameColors.DottedLineColor,
            radius = dotRadius * 1.2f, // Slightly larger for better visibility
            center = Offset(x, y)
        )
    }
}
