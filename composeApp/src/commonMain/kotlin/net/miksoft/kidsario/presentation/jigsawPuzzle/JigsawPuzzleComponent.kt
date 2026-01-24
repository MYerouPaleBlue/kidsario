package net.miksoft.kidsario.presentation.jigsawPuzzle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kidsario.composeapp.generated.resources.*
import net.miksoft.kidsario.theme.GameColors
import org.jetbrains.compose.resources.imageResource
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Jigsaw Puzzle game component that displays a puzzle for kids to solve by dragging pieces.
 */
@Composable
fun JigsawPuzzleComponent(
    modifier: Modifier = Modifier,
    viewModel: JigsawPuzzleViewModel,
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
                    JigsawDifficulty.values().forEach { difficulty ->
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
                                text = "${difficulty.displayName} (${difficulty.gridSize}x${difficulty.gridSize})",
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
        modifier = modifier,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = {
                    Text(
                        "Jigsaw Puzzle - Level ${uiState.level}",
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
                    // Button to generate a new puzzle
                    IconButton(onClick = { viewModel.generateNewPuzzle() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Puzzle",
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameColors.CardBackgroundColor,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Drag the pieces to complete the puzzle!",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Puzzle area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    PuzzleBoard(
                        pieces = uiState.pieces,
                        gridSize = uiState.gridSize,
                        puzzleImage = uiState.puzzleImage,
                        draggedPieceId = uiState.draggedPieceId,
                        onStartDrag = { pieceId -> viewModel.startDragging(pieceId) },
                        onDrag = { pieceId, x, y -> viewModel.updatePiecePosition(pieceId, x, y) },
                        onEndDrag = { pieceId -> viewModel.endDragging(pieceId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Success message
                AnimatedVisibility(
                    visible = uiState.message != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        backgroundColor = GameColors.CorrectColor,
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
 * Get the ImageBitmap for the puzzle image resource
 */
@Composable
fun getPuzzleImageBitmap(puzzleImage: PuzzleImageResource): ImageBitmap = when (puzzleImage) {
    PuzzleImageResource.CRANE -> imageResource(Res.drawable.puzzle_crane)
    PuzzleImageResource.HIPPO -> imageResource(Res.drawable.puzzle_hippo)
    PuzzleImageResource.PHOTO001 -> imageResource(Res.drawable.puzzle_photo001)
    PuzzleImageResource.PHOTO002 -> imageResource(Res.drawable.puzzle_photo002)
    PuzzleImageResource.PHOTO003 -> imageResource(Res.drawable.puzzle_photo003)
    PuzzleImageResource.PHOTO004 -> imageResource(Res.drawable.puzzle_photo004)
    PuzzleImageResource.PHOTO005 -> imageResource(Res.drawable.puzzle_photo005)
    PuzzleImageResource.PHOTO006 -> imageResource(Res.drawable.puzzle_photo006)
    PuzzleImageResource.PHOTO007 -> imageResource(Res.drawable.puzzle_photo007)
    PuzzleImageResource.PHOTO008 -> imageResource(Res.drawable.puzzle_photo008)
    PuzzleImageResource.PHOTO009 -> imageResource(Res.drawable.puzzle_photo009)
    PuzzleImageResource.PHOTO010 -> imageResource(Res.drawable.puzzle_photo010)
    PuzzleImageResource.PHOTO011 -> imageResource(Res.drawable.puzzle_photo011)
    PuzzleImageResource.PHOTO012 -> imageResource(Res.drawable.puzzle_photo012)
    PuzzleImageResource.PHOTO013 -> imageResource(Res.drawable.puzzle_photo013)
    PuzzleImageResource.PHOTO014 -> imageResource(Res.drawable.puzzle_photo014)
    PuzzleImageResource.PHOTO015 -> imageResource(Res.drawable.puzzle_photo015)
    PuzzleImageResource.PHOTO016 -> imageResource(Res.drawable.puzzle_photo016)
    PuzzleImageResource.PHOTO017 -> imageResource(Res.drawable.puzzle_photo017)
    PuzzleImageResource.PHOTO018 -> imageResource(Res.drawable.puzzle_photo018)
    PuzzleImageResource.PHOTO019 -> imageResource(Res.drawable.puzzle_photo019)
    PuzzleImageResource.SQUIRREL -> imageResource(Res.drawable.puzzle_squirrel)
    PuzzleImageResource.WHALE -> imageResource(Res.drawable.puzzle_whale)
}

/**
 * Puzzle board composable that handles the puzzle grid and draggable pieces.
 */
@Composable
fun PuzzleBoard(
    pieces: List<PuzzlePiece>,
    gridSize: Int,
    puzzleImage: PuzzleImageResource,
    draggedPieceId: Int?,
    onStartDrag: (Int) -> Unit,
    onDrag: (Int, Float, Float) -> Unit,
    onEndDrag: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .onSizeChanged { boardSize = it }
    ) {
        if (boardSize.width > 0 && boardSize.height > 0) {
            val boardWidth = boardSize.width.toFloat()
            val boardHeight = boardSize.height.toFloat()

            // Calculate square puzzle area that fits in the top portion
            val maxPuzzleWidth = boardWidth * 0.8f
            val maxPuzzleHeight = boardHeight * 0.5f
            val puzzleSize = minOf(maxPuzzleWidth, maxPuzzleHeight)
            
            // Center the square puzzle area horizontally
            val puzzleAreaStartX = (boardWidth - puzzleSize) / 2f
            val puzzleAreaEndX = puzzleAreaStartX + puzzleSize
            val puzzleAreaStartY = boardHeight * 0.05f
            val puzzleAreaEndY = puzzleAreaStartY + puzzleSize
            val puzzleAreaWidth = puzzleSize
            val puzzleAreaHeight = puzzleSize

            val cellSize = puzzleSize / gridSize
            val cellWidth = cellSize
            val cellHeight = cellSize
            val pieceDisplaySize = cellSize * 0.95f

            // Draw puzzle grid outline
            Box(
                modifier = Modifier
                    .offset { IntOffset(puzzleAreaStartX.roundToInt(), puzzleAreaStartY.roundToInt()) }
                    .size(
                        with(density) { puzzleAreaWidth.toDp() },
                        with(density) { puzzleAreaHeight.toDp() }
                    )
                    .border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            )

            // Draw grid cells
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    val cellX = puzzleAreaStartX + col * cellWidth
                    val cellY = puzzleAreaStartY + row * cellHeight
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(cellX.roundToInt(), cellY.roundToInt()) }
                            .size(
                                with(density) { cellWidth.toDp() },
                                with(density) { cellHeight.toDp() }
                            )
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }

            // Draw tray area indicator
            val trayY = boardHeight * 0.6f
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, trayY.roundToInt()) }
                    .fillMaxWidth()
                    .height(with(density) { (boardHeight - trayY).toDp() })
                    .background(Color.LightGray.copy(alpha = 0.15f))
            )

            // Draw puzzle pieces
            // First draw placed pieces
            pieces.filter { it.isPlaced }.forEach { piece ->
                PuzzlePieceView(
                    piece = piece,
                    puzzleImage = puzzleImage,
                    gridSize = gridSize,
                    boardWidth = boardWidth,
                    boardHeight = boardHeight,
                    pieceDisplaySize = pieceDisplaySize,
                    isBeingDragged = false,
                    onStartDrag = {},
                    onDrag = { _, _ -> },
                    onEndDrag = {}
                )
            }

            // Then draw non-placed, non-dragged pieces
            pieces.filter { !it.isPlaced && it.id != draggedPieceId }.forEach { piece ->
                PuzzlePieceView(
                    piece = piece,
                    puzzleImage = puzzleImage,
                    gridSize = gridSize,
                    boardWidth = boardWidth,
                    boardHeight = boardHeight,
                    pieceDisplaySize = pieceDisplaySize,
                    isBeingDragged = false,
                    onStartDrag = { onStartDrag(piece.id) },
                    onDrag = { x, y -> onDrag(piece.id, x, y) },
                    onEndDrag = { onEndDrag(piece.id) }
                )
            }

            // Finally draw the dragged piece on top
            draggedPieceId?.let { id ->
                pieces.find { it.id == id }?.let { piece ->
                    PuzzlePieceView(
                        piece = piece,
                        puzzleImage = puzzleImage,
                        gridSize = gridSize,
                        boardWidth = boardWidth,
                        boardHeight = boardHeight,
                        pieceDisplaySize = pieceDisplaySize,
                        isBeingDragged = true,
                        onStartDrag = { onStartDrag(piece.id) },
                        onDrag = { x, y -> onDrag(piece.id, x, y) },
                        onEndDrag = { onEndDrag(piece.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual puzzle piece view with image clipping and drag handling.
 * Uses local state for smooth dragging without recomposition on every drag event.
 * Uses Canvas-based drawing to properly center-crop non-square images.
 */
@Composable
fun PuzzlePieceView(
    piece: PuzzlePiece,
    puzzleImage: PuzzleImageResource,
    gridSize: Int,
    boardWidth: Float,
    boardHeight: Float,
    pieceDisplaySize: Float,
    isBeingDragged: Boolean,
    onStartDrag: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onEndDrag: () -> Unit
) {
    val density = LocalDensity.current
    val imageBitmap = getPuzzleImageBitmap(puzzleImage)
    
    // Local state for tracking drag offset - this avoids recomposition during drag
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Calculate puzzle area dimensions (must match PuzzleBoard calculations)
    val maxPuzzleWidth = boardWidth * 0.8f
    val maxPuzzleHeight = boardHeight * 0.5f
    val puzzleSize = minOf(maxPuzzleWidth, maxPuzzleHeight)
    val puzzleAreaStartX = (boardWidth - puzzleSize) / 2f
    val puzzleAreaStartY = boardHeight * 0.05f
    val cellSize = puzzleSize / gridSize
    
    // Calculate base position - for placed pieces, use exact grid position
    val basePieceX: Float
    val basePieceY: Float
    if (piece.isPlaced) {
        // Calculate exact position centered in the correct grid cell
        val cellCenterX = puzzleAreaStartX + (piece.correctCol + 0.5f) * cellSize
        val cellCenterY = puzzleAreaStartY + (piece.correctRow + 0.5f) * cellSize
        basePieceX = cellCenterX - pieceDisplaySize / 2
        basePieceY = cellCenterY - pieceDisplaySize / 2
    } else {
        // For non-placed pieces, use the currentX/Y from ViewModel
        basePieceX = piece.currentX * boardWidth - pieceDisplaySize / 2
        basePieceY = piece.currentY * boardHeight - pieceDisplaySize / 2
    }
    
    // Final position combines base position with local drag offset
    val pieceX = basePieceX + dragOffsetX
    val pieceY = basePieceY + dragOffsetY

    Box(
        modifier = Modifier
            .offset { IntOffset(pieceX.roundToInt(), pieceY.roundToInt()) }
            .size(with(density) { pieceDisplaySize.toDp() })
            .zIndex(if (isDragging || isBeingDragged) 100f else if (piece.isPlaced) 1f else 10f)
            .then(
                if (!piece.isPlaced) {
                    Modifier.pointerInput(piece.id) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                                onStartDrag()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Update local offset for immediate visual feedback
                                dragOffsetX += dragAmount.x
                                dragOffsetY += dragAmount.y
                            },
                            onDragEnd = {
                                // Commit the final position to ViewModel
                                val deltaX = dragOffsetX / boardWidth
                                val deltaY = dragOffsetY / boardHeight
                                onDrag(deltaX, deltaY)
                                // Reset local state
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                                isDragging = false
                                onEndDrag()
                            },
                            onDragCancel = {
                                // Reset without committing
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                                isDragging = false
                                onEndDrag()
                            }
                        )
                    }
                } else Modifier
            )
            .shadow(
                elevation = if (isDragging || isBeingDragged) 16.dp else if (piece.isPlaced) 2.dp else 8.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(
                width = if (piece.isPlaced) 2.dp else 3.dp,
                color = if (piece.isPlaced) Color.Green.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Use Canvas to draw the puzzle piece with proper center-cropping
        // This ensures non-square images are cropped to square before slicing into pieces
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPuzzlePiece(
                imageBitmap = imageBitmap,
                gridSize = gridSize,
                pieceRow = piece.correctRow,
                pieceCol = piece.correctCol,
                canvasSize = size
            )
        }
    }
}

/**
 * Draws a puzzle piece from the image bitmap with proper center-cropping.
 * First center-crops the image to make it square, then extracts the piece portion.
 */
private fun DrawScope.drawPuzzlePiece(
    imageBitmap: ImageBitmap,
    gridSize: Int,
    pieceRow: Int,
    pieceCol: Int,
    canvasSize: Size
) {
    val imageWidth = imageBitmap.width
    val imageHeight = imageBitmap.height
    
    // Calculate the square crop region (center-crop)
    val squareSize = min(imageWidth, imageHeight)
    val cropOffsetX = (imageWidth - squareSize) / 2
    val cropOffsetY = (imageHeight - squareSize) / 2
    
    // Calculate the piece size within the cropped square
    val pieceSizeInImage = squareSize / gridSize
    
    // Calculate source rectangle for this piece (within the center-cropped square)
    val srcLeft = cropOffsetX + pieceCol * pieceSizeInImage
    val srcTop = cropOffsetY + pieceRow * pieceSizeInImage
    
    // Draw the piece portion to fill the entire canvas
    drawImage(
        image = imageBitmap,
        srcOffset = IntOffset(srcLeft, srcTop),
        srcSize = IntSize(pieceSizeInImage, pieceSizeInImage),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(canvasSize.width.roundToInt(), canvasSize.height.roundToInt())
    )
}
