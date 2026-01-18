package net.miksoft.kidsario.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.miksoft.kidsario.theme.GameColors

/**
 * Data class representing a mini game
 */
data class MiniGame(
    val id: String,
    val name: String,
    val color: Color,
    val iconDrawer: @Composable (Modifier) -> Unit
)

/**
 * Home screen component that displays a scrollable grid of mini games with a kid-friendly design.
 */
@Composable
fun HomeComponent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onMiniGameClicked: (String) -> Unit = { gameId -> viewModel.onMiniGameClicked(gameId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    // Define mini games with their icons
    val miniGames = listOf(
        MiniGame(
            id = "counting_objects",
            name = "Counting Objects",
            color = GameColors.CircleColor,
            iconDrawer = { mod ->
                Canvas(modifier = mod) {
                    // Draw simple counting objects icon (circles)
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 6,
                        center = Offset(size.width * 0.3f, size.height * 0.3f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 6,
                        center = Offset(size.width * 0.7f, size.height * 0.3f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 6,
                        center = Offset(size.width * 0.3f, size.height * 0.7f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 6,
                        center = Offset(size.width * 0.7f, size.height * 0.7f)
                    )
                }
            }
        ),
        MiniGame(
            id = "drawing_letters",
            name = "Drawing Letters",
            color = GameColors.RectangleColor,
            iconDrawer = { mod ->
                Canvas(modifier = mod) {
                    // Draw simple letter A
                    val path = Path().apply {
                        moveTo(size.width * 0.3f, size.height * 0.8f)
                        lineTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width * 0.7f, size.height * 0.8f)
                        moveTo(size.width * 0.35f, size.height * 0.6f)
                        lineTo(size.width * 0.65f, size.height * 0.6f)
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = size.minDimension / 10)
                    )
                }
            }
        ),
        MiniGame(
            id = "maze",
            name = "Maze Game",
            color = GameColors.TriangleColor,
            iconDrawer = { mod ->
                Canvas(modifier = mod) {
                    // Draw simple maze
                    val strokeWidth = size.minDimension / 20

                    // Outer square
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                        size = Size(size.width * 0.6f, size.height * 0.6f),
                        style = Stroke(width = strokeWidth)
                    )

                    // Inner lines
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.2f, size.height * 0.5f),
                        end = Offset(size.width * 0.5f, size.height * 0.5f),
                        strokeWidth = strokeWidth
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.5f, size.height * 0.5f),
                        end = Offset(size.width * 0.5f, size.height * 0.8f),
                        strokeWidth = strokeWidth
                    )
                }
            }
        ),
        MiniGame(
            id = "free_drawing",
            name = "Free Drawing",
            color = GameColors.StarColor,
            iconDrawer = { mod ->
                Canvas(modifier = mod) {
                     val width = size.width
                     val height = size.height
                     // Draw simple pencil
                     rotate(45f, pivot = center) {
                         // Pencil body
                         drawRect(
                             color = Color.White,
                             topLeft = Offset(width * 0.4f, height * 0.3f),
                             size = Size(width * 0.2f, height * 0.4f)
                         )
                         // Tip
                         val path = Path().apply {
                             moveTo(width * 0.4f, height * 0.3f)
                             lineTo(width * 0.6f, height * 0.3f)
                             lineTo(width * 0.5f, height * 0.15f)
                             close()
                         }
                         drawPath(path, Color.White)
                     }
                }
            }
        ),
        MiniGame(
            id = "jigsaw_puzzle",
            name = "Jigsaw Puzzle",
            color = GameColors.HeartColor,
            iconDrawer = { mod ->
                Canvas(modifier = mod) {
                    val width = size.width
                    val height = size.height
                    val strokeWidth = size.minDimension / 20
                    
                    // Draw puzzle pieces grid
                    val pieceWidth = width * 0.35f
                    val pieceHeight = height * 0.35f
                    
                    // Top-left piece
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(width * 0.15f, height * 0.15f),
                        size = Size(pieceWidth, pieceHeight),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Top-right piece
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(width * 0.5f, height * 0.15f),
                        size = Size(pieceWidth, pieceHeight),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Bottom-left piece
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(width * 0.15f, height * 0.5f),
                        size = Size(pieceWidth, pieceHeight),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Bottom-right piece (slightly offset to show puzzle effect)
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(width * 0.55f, height * 0.55f),
                        size = Size(pieceWidth, pieceHeight),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = { 
                    Text(
                        "Kidsario",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Decorative elements - colorful circles at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GameColors.CircleColor)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GameColors.SquareColor)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GameColors.RectangleColor)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GameColors.TriangleColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome card with playful design
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GameColors.CardBackgroundColor,
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Welcome to Kidsario!",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable grid of mini games (2 per row)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calculate how many rows we need
                val rows = (miniGames.size + 1) / 2

                items(rows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // First item in the row
                        val firstIndex = rowIndex * 2
                        if (firstIndex < miniGames.size) {
                            MiniGameItem(
                                miniGame = miniGames[firstIndex],
                                modifier = Modifier.weight(1f),
                                onClick = { onMiniGameClicked(miniGames[firstIndex].id) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Second item in the row
                        val secondIndex = rowIndex * 2 + 1
                        if (secondIndex < miniGames.size) {
                            MiniGameItem(
                                miniGame = miniGames[secondIndex],
                                modifier = Modifier.weight(1f),
                                onClick = { onMiniGameClicked(miniGames[secondIndex].id) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Display last clicked game (for demonstration purposes) with a nicer style
            if (uiState.lastClickedGameId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = GameColors.AlternateCardBackgroundColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Last played: ${uiState.lastClickedGameId.replace("_", " ").capitalize()}",
                        style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Composable for a mini game item in the grid
 */
@Composable
fun MiniGameItem(
    miniGame: MiniGame,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Make it square
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        backgroundColor = miniGame.color,
        elevation = 0.dp // We're using shadow modifier instead
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon area (60% of the space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                miniGame.iconDrawer(Modifier.fillMaxSize())
            }

            // Text area (40% of the space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = miniGame.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
