package net.miksoft.kidsario.presentation.maze

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import net.miksoft.kidsario.theme.GameColors

/**
 * Maze game component that displays a maze for kids to solve.
 */
@Composable
fun MazeComponent(
    modifier: Modifier = Modifier,
    viewModel: MazeViewModel,
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
                    MazeDifficulty.values().forEach { difficulty ->
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
                                text = difficulty.displayName,
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
                        "Maze Game",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Button to generate a new maze
                    IconButton(onClick = { viewModel.generateNewMaze() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Maze",
                            tint = Color.White
                        )
                    }
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameColors.CardBackgroundColor,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Draw a line from the green dot to the red dot without crossing any walls!",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Maze canvas
                MazeCanvas(
                    maze = uiState.maze,
                    userDrawingPoints = uiState.userDrawingPoints,
                    startPoint = uiState.startPoint,
                    endPoint = uiState.endPoint,
                    onDrawingPointAdded = { x, y, isNewStroke ->
                        viewModel.addDrawingPoint(x, y, isNewStroke)
                    },
                    isEnabled = uiState.isGameActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                )

                // Success message
                AnimatedVisibility(
                    visible = uiState.message != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        backgroundColor = if (uiState.isSuccess) GameColors.CorrectColor else GameColors.IncorrectColor,
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = uiState.message ?: "",
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas component for drawing the maze and the user's path.
 */
@Composable
fun MazeCanvas(
    maze: Array<Array<MazeCell>>,
    userDrawingPoints: List<DrawingPoint>,
    startPoint: Pair<Int, Int>,
    endPoint: Pair<Int, Int>,
    onDrawingPointAdded: (Float, Float, Boolean) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .pointerInput(isEnabled) {
                if (isEnabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val x = offset.x / canvasSize.width
                            val y = offset.y / canvasSize.height
                            onDrawingPointAdded(x, y, true)
                        },
                        onDrag = { change, _ ->
                            val x = change.position.x / canvasSize.width
                            val y = change.position.y / canvasSize.height
                            onDrawingPointAdded(x, y, false)
                        }
                    )
                }
            }
    ) {
        canvasSize = size

        val cellWidth = size.width / maze[0].size
        val cellHeight = size.height / maze.size

        // Draw the maze
        for (y in maze.indices) {
            for (x in maze[y].indices) {
                if (maze[y][x] == MazeCell.WALL) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * cellWidth, y * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
        }

        // Draw the user's path
        if (userDrawingPoints.isNotEmpty()) {
            val path = Path()
            var currentPoint = userDrawingPoints.first()
            path.moveTo(currentPoint.x * size.width, currentPoint.y * size.height)

            for (point in userDrawingPoints.drop(1)) {
                if (point.isNewStroke) {
                    path.moveTo(point.x * size.width, point.y * size.height)
                } else {
                    path.lineTo(point.x * size.width, point.y * size.height)
                }
                currentPoint = point
            }

            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw the start point (green dot)
        drawCircle(
            color = Color.Green,
            radius = 12f,
            center = Offset(
                (startPoint.first + 0.5f) * cellWidth,
                (startPoint.second + 0.5f) * cellHeight
            )
        )

        // Draw the end point (red dot) - increased radius for better visibility
        drawCircle(
            color = Color.Red,
            radius = 20f, // Increased from 16f to make it more visible
            center = Offset(
                (endPoint.first + 0.5f) * cellWidth,
                (endPoint.second + 0.5f) * cellHeight
            )
        )
    }
}
