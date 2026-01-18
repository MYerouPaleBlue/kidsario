package net.miksoft.kidsario.presentation.freeDrawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.miksoft.kidsario.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun FreeDrawingComponent(
    modifier: Modifier = Modifier,
    viewModel: FreeDrawingViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleClearDialog() },
            title = { Text("Clear Drawing?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear everything?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAll() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.IncorrectColor)
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleClearDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Free Drawing", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
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
                elevation = 4.dp
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GameColors.GameBackgroundColor)
        ) {
            // Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .pointerInput(uiState.currentTool) {
                        if (uiState.currentTool == DrawingTool.STAMP) {
                            detectTapGestures(
                                onTap = { offset -> viewModel.addStamp(offset) }
                            )
                        } else {
                            detectDragGestures(
                                onDragStart = { offset -> viewModel.startDrawing(offset) },
                                onDrag = { change, _ -> 
                                    change.consume()
                                    viewModel.updateDrawing(change.position) 
                                },
                                onDragEnd = { viewModel.finishDrawing() }
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw completed elements
                    uiState.elements.forEach { element ->
                        when (element) {
                            is DrawingElement.Path -> {
                                drawPath(
                                    path = element.path.toUiPath(),
                                    color = element.path.color,
                                    style = Stroke(
                                        width = element.path.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                            is DrawingElement.StampData -> {
                                drawStamp(element)
                            }
                        }
                    }
                    
                    // Draw current path
                    uiState.currentPath?.let { path ->
                        drawPath(
                            path = path.toUiPath(),
                            color = path.color,
                            style = Stroke(
                                width = path.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Side Panel
            SidePanel(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
                uiState = uiState,
                onToolSelected = viewModel::selectTool,
                onColorSelected = viewModel::selectColor,
                onStampSelected = viewModel::selectStamp,
                onStrokeWidthChanged = viewModel::setStrokeWidth,
                onClear = viewModel::toggleClearDialog
            )
        }
    }
}

@Composable
fun SidePanel(
    modifier: Modifier,
    uiState: FreeDrawingUiState,
    onToolSelected: (DrawingTool) -> Unit,
    onColorSelected: (Color) -> Unit,
    onStampSelected: (StampType) -> Unit,
    onStrokeWidthChanged: (Float) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tools
            Text("Tools", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolButton(
                    text = "✏️",
                    isSelected = uiState.currentTool == DrawingTool.PEN,
                    onClick = { onToolSelected(DrawingTool.PEN) }
                )
                ToolButton(
                    text = "🧽",
                    isSelected = uiState.currentTool == DrawingTool.ERASER,
                    onClick = { onToolSelected(DrawingTool.ERASER) }
                )
            }
            
            ToolButton(
                text = "⭐",
                isSelected = uiState.currentTool == DrawingTool.STAMP,
                onClick = { onToolSelected(DrawingTool.STAMP) }
            )

            Divider()

            // Size Slider - Always visible
            Text("Size", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Slider(
                value = uiState.currentStrokeWidth,
                onValueChange = onStrokeWidthChanged,
                valueRange = 5f..100f, // Increased range for better stamp sizing
                colors = SliderDefaults.colors(
                    thumbColor = uiState.currentColor,
                    activeTrackColor = uiState.currentColor
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Divider()

            // Content based on tool
            if (uiState.currentTool == DrawingTool.STAMP) {
                // Colors (Horizontal Grid for space efficiency)
                Text("Colors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                     items(listOf(Purple, Pink, Yellow, Green, Blue, Orange, Red, Teal, Color.Black, Color.Gray)) { color ->
                        ColorButton(
                            color = color,
                            isSelected = uiState.currentColor == color,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
                
                Divider()
                
                // Stamps (Vertical List)
                Text("Stamps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    items(StampType.values()) { stamp ->
                        StampButton(
                            type = stamp,
                            isSelected = uiState.selectedStamp == stamp,
                            color = uiState.currentColor,
                            onClick = { onStampSelected(stamp) }
                        )
                    }
                }
            } else {
                 // Colors (Vertical List filling space)
                Text("Colors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val colors = listOf(
                        Purple, Pink, Yellow, Green, Blue, Orange, Red, Teal, Color.Black, Color.Gray
                    )
                    items(colors) { color ->
                        ColorButton(
                            color = color,
                            isSelected = uiState.currentColor == color,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
            }

            // Clear Button
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(backgroundColor = GameColors.IncorrectColor),
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("🗑️", fontSize = 20.sp)
            }
        }
    }
}


@Composable
fun ToolButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LightPurple else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Purple else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 20.sp)
    }
}

@Composable
fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.DarkGray else Color.LightGray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun StampButton(
    type: StampType,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LightPurple else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Purple else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val stamp = DrawingElement.StampData(
                position = Offset(size.width/2, size.height/2), 
                type = type, 
                color = color,
                size = size.width
            )
            drawStamp(stamp)
        }
    }
}

private fun DrawingPath.toUiPath(): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    return path
}

private fun DrawScope.drawStamp(stamp: DrawingElement.StampData) {
    val size = stamp.size
    val half = size / 2
    val x = stamp.position.x
    val y = stamp.position.y
    val color = stamp.color

    when (stamp.type) {
        StampType.STAR -> {
            val path = Path().apply {
                var angle = -PI / 2
                val step = PI * 4 / 5
                moveTo(
                    (x + cos(angle) * half).toFloat(),
                    (y + sin(angle) * half).toFloat()
                )
                repeat(5) {
                    angle += step
                    lineTo(
                        (x + cos(angle) * half).toFloat(),
                        (y + sin(angle) * half).toFloat()
                    )
                }
                close()
            }
            drawPath(path, color)
        }
        StampType.HEART -> {
            val path = Path().apply {
                 moveTo(x, y + half * 0.5f)
                 cubicTo(
                     x - half, y - half * 0.5f,
                     x - half, y - half,
                     x, y - half * 0.3f
                 )
                 cubicTo(
                     x + half, y - half,
                     x + half, y - half * 0.5f,
                     x, y + half * 0.5f
                 )
            }
            drawPath(path, color)
        }
        StampType.SMILE -> {
            val stroke = size * 0.08f
            drawCircle(color, half, center = stamp.position, style = Stroke(width = stroke))
            val eyeOffset = half * 0.4f
            val eyeY = y - half * 0.25f
            val eyeRadius = size * 0.05f
            drawCircle(color, eyeRadius, center = Offset(x - eyeOffset, eyeY))
            drawCircle(color, eyeRadius, center = Offset(x + eyeOffset, eyeY))
            
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - half * 0.5f, y - half * 0.25f),
                size = Size(half, half),
                style = Stroke(width = stroke)
            )
        }
        StampType.FLOWER -> {
            drawCircle(color, size * 0.15f, center = stamp.position)
            for (i in 0 until 5) {
                val angle = i * PI * 2 / 5
                drawCircle(
                    color = color.copy(alpha = 0.6f),
                    radius = size * 0.2f,
                    center = Offset(
                        (x + cos(angle) * size * 0.3f).toFloat(),
                        (y + sin(angle) * size * 0.3f).toFloat()
                    )
                )
            }
        }
        StampType.SUN -> {
            drawCircle(color, size * 0.25f, center = stamp.position)
            for (i in 0 until 8) {
                val angle = i * PI * 2 / 8
                drawLine(
                    color = color,
                    start = Offset(
                        (x + cos(angle) * size * 0.35f).toFloat(),
                        (y + sin(angle) * size * 0.35f).toFloat()
                    ),
                    end = Offset(
                        (x + cos(angle) * size * 0.55f).toFloat(),
                        (y + sin(angle) * size * 0.55f).toFloat()
                    ),
                    strokeWidth = size * 0.08f
                )
            }
        }
        StampType.CLOUD -> {
             val r1 = size * 0.25f
             val r2 = size * 0.35f
             drawCircle(color, r1, center = Offset(x - size * 0.2f, y + size * 0.1f))
             drawCircle(color, r1, center = Offset(x + size * 0.2f, y + size * 0.1f))
             drawCircle(color, r2, center = Offset(x, y - size * 0.1f))
        }
        StampType.MOON -> {
             val path = Path().apply {
                moveTo(x + half * 0.5f, y - half)
                quadraticBezierTo(x - half, y, x + half * 0.5f, y + half)
                quadraticBezierTo(x, y, x + half * 0.5f, y - half)
             }
             drawPath(path, color)
        }
        StampType.TREE -> {
             val trunkW = size * 0.2f
             val trunkH = size * 0.4f
             drawRect(
                 color = color,
                 topLeft = Offset(x - trunkW/2, y + size * 0.1f),
                 size = Size(trunkW, trunkH)
             )
             val path = Path().apply {
                 moveTo(x, y - half)
                 lineTo(x + half, y + size * 0.1f)
                 lineTo(x - half, y + size * 0.1f)
                 close()
             }
             drawPath(path, color)
        }
        StampType.CAR -> {
            val bodyH = size * 0.25f
            val bodyW = size * 0.8f
            val topH = size * 0.2f
            val topW = size * 0.4f
            val wheelR = size * 0.1f
            drawRect(
                color = color,
                topLeft = Offset(x - bodyW/2, y),
                size = Size(bodyW, bodyH)
            )
            drawRect(
                color = color,
                topLeft = Offset(x - topW/2, y - topH),
                size = Size(topW, topH)
            )
            drawCircle(color, wheelR, center = Offset(x - bodyW * 0.25f, y + bodyH))
            drawCircle(color, wheelR, center = Offset(x + bodyW * 0.25f, y + bodyH))
        }
        StampType.HOUSE -> {
            val bodyW = size * 0.5f
            val bodyH = size * 0.4f
            drawRect(
                color = color,
                topLeft = Offset(x - bodyW/2, y),
                size = Size(bodyW, bodyH)
            )
            val path = Path().apply {
                moveTo(x - bodyW * 0.6f, y)
                lineTo(x, y - size * 0.4f)
                lineTo(x + bodyW * 0.6f, y)
                close()
            }
            drawPath(path, color)
        }
        StampType.APPLE -> {
            drawCircle(color, size * 0.25f, center = Offset(x, y + size * 0.05f))
            drawLine(
                color = color,
                start = Offset(x, y - size * 0.2f),
                end = Offset(x, y - size * 0.3f),
                strokeWidth = size * 0.08f
            )
            drawOval(
                color = color,
                topLeft = Offset(x + size * 0.05f, y - size * 0.3f),
                size = Size(size * 0.15f, size * 0.1f)
            )
        }
        StampType.FISH -> {
            drawOval(
                color = color,
                topLeft = Offset(x - size * 0.4f, y - size * 0.25f),
                size = Size(size * 0.6f, size * 0.5f)
            )
            val path = Path().apply {
                 moveTo(x + size * 0.2f, y)
                 lineTo(x + size * 0.5f, y - size * 0.2f)
                 lineTo(x + size * 0.5f, y + size * 0.2f)
                 close()
            }
            drawPath(path, color)
        }
        StampType.BUTTERFLY -> {
            val wingSize = size * 0.35f
            drawOval(color, topLeft = Offset(x - wingSize, y - wingSize), size = Size(wingSize, wingSize))
            drawOval(color, topLeft = Offset(x, y - wingSize), size = Size(wingSize, wingSize))
            val smallWing = wingSize * 0.8f
            drawOval(color, topLeft = Offset(x - smallWing, y - size * 0.1f), size = Size(smallWing, smallWing))
            drawOval(color, topLeft = Offset(x, y - size * 0.1f), size = Size(smallWing, smallWing))
             drawLine(
                color = color,
                start = Offset(x, y - size * 0.25f),
                end = Offset(x, y + size * 0.25f),
                strokeWidth = size * 0.08f
            )
        }
        StampType.LIGHTNING -> {
             val path = Path().apply {
                 moveTo(x + half * 0.2f, y - half)
                 lineTo(x - half * 0.2f, y - half * 0.1f)
                 lineTo(x + half * 0.4f, y - half * 0.1f)
                 lineTo(x - half * 0.2f, y + half)
                 lineTo(x + half * 0.2f, y + half * 0.1f)
                 lineTo(x - half * 0.4f, y + half * 0.1f)
                 close()
             }
             drawPath(path, color)
        }
        StampType.DIAMOND -> {
            val path = Path().apply {
                moveTo(x, y - half)
                lineTo(x + half * 0.7f, y)
                lineTo(x, y + half)
                lineTo(x - half * 0.7f, y)
                close()
            }
             drawPath(path, color)
        }
        StampType.NOTE -> {
             drawOval(color, topLeft = Offset(x - half * 0.4f, y + half * 0.4f), size = Size(half * 0.6f, half * 0.5f))
             drawLine(color, start = Offset(x + half * 0.2f, y + half * 0.6f), end = Offset(x + half * 0.2f, y - half * 0.6f), strokeWidth = size * 0.1f)
             drawLine(color, start = Offset(x + half * 0.2f, y - half * 0.6f), end = Offset(x + half * 0.6f, y - half * 0.4f), strokeWidth = size * 0.1f)
        }
        StampType.LEAF -> {
             val path = Path().apply {
                 moveTo(x, y + half)
                 quadraticBezierTo(x + half, y, x, y - half)
                 quadraticBezierTo(x - half, y, x, y + half)
             }
             drawPath(path, color)
        }
        StampType.DROP -> {
             val path = Path().apply {
                 moveTo(x, y - half)
                 quadraticBezierTo(x + half, y + half * 0.5f, x, y + half)
                 quadraticBezierTo(x - half, y + half * 0.5f, x, y - half)
             }
             drawPath(path, color)
        }
        StampType.BALLOON -> {
             drawOval(color, topLeft = Offset(x - half * 0.7f, y - half), size = Size(size * 0.7f, size * 0.8f))
             drawLine(color, start = Offset(x, y + half * 0.3f), end = Offset(x, y + half), strokeWidth = size * 0.05f)
        }
        StampType.ICE_CREAM -> {
             val path = Path().apply {
                 moveTo(x, y + half)
                 lineTo(x + half * 0.5f, y)
                 lineTo(x - half * 0.5f, y)
                 close()
             }
             drawPath(path, color)
             drawCircle(color, radius = half * 0.5f, center = Offset(x, y - half * 0.2f))
        }
        StampType.BOAT -> {
             val path = Path().apply {
                 moveTo(x - half * 0.8f, y + half * 0.2f)
                 lineTo(x + half * 0.8f, y + half * 0.2f)
                 lineTo(x + half * 0.5f, y + half * 0.8f)
                 lineTo(x - half * 0.5f, y + half * 0.8f)
                 close()
             }
             drawPath(path, color)
             val sail = Path().apply {
                 moveTo(x, y + half * 0.1f)
                 lineTo(x, y - half * 0.8f)
                 lineTo(x + half * 0.6f, y + half * 0.1f)
                 close()
             }
             drawPath(sail, color)
        }
    }
}
