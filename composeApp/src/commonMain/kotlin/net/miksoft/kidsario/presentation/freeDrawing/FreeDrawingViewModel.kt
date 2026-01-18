package net.miksoft.kidsario.presentation.freeDrawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.miksoft.kidsario.theme.*

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

enum class StampType {
    STAR, HEART, SMILE, FLOWER, SUN, CLOUD, MOON, TREE, CAR, HOUSE, APPLE, FISH, BUTTERFLY,
    LIGHTNING, DIAMOND, NOTE, LEAF, DROP, BALLOON, ICE_CREAM, BOAT
}

sealed class DrawingElement {
    data class Path(val path: DrawingPath) : DrawingElement()
    data class StampData(val position: Offset, val type: StampType, val color: Color, val size: Float) : DrawingElement()
}

enum class DrawingTool {
    PEN, ERASER, STAMP
}

data class FreeDrawingUiState(
    val elements: List<DrawingElement> = emptyList(),
    val currentPath: DrawingPath? = null,
    val currentTool: DrawingTool = DrawingTool.PEN,
    val currentColor: Color = Purple,
    val currentStrokeWidth: Float = 10f,
    val selectedStamp: StampType = StampType.STAR,
    val showClearDialog: Boolean = false
)

class FreeDrawingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FreeDrawingUiState())
    val uiState: StateFlow<FreeDrawingUiState> = _uiState.asStateFlow()

    private var currentPathPoints = mutableListOf<Offset>()

    fun selectTool(tool: DrawingTool) {
        _uiState.value = _uiState.value.copy(currentTool = tool)
    }

    fun selectColor(color: Color) {
        _uiState.value = _uiState.value.copy(currentColor = color)
    }

    fun selectStamp(stamp: StampType) {
        _uiState.value = _uiState.value.copy(selectedStamp = stamp)
    }

    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(currentStrokeWidth = width)
    }

    fun toggleClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = !_uiState.value.showClearDialog)
    }

    fun clearAll() {
        _uiState.value = _uiState.value.copy(
            elements = emptyList(), 
            currentPath = null,
            showClearDialog = false
        )
    }

    fun startDrawing(offset: Offset) {
        if (_uiState.value.currentTool == DrawingTool.STAMP) return

        currentPathPoints = mutableListOf(offset)
        updateCurrentPathState()
    }

    fun updateDrawing(offset: Offset) {
        if (_uiState.value.currentTool == DrawingTool.STAMP) return

        currentPathPoints.add(offset)
        updateCurrentPathState()
    }

    fun finishDrawing() {
        if (_uiState.value.currentTool == DrawingTool.STAMP) return

        if (currentPathPoints.isNotEmpty()) {
            val path = createCurrentPath()
            val newElement = DrawingElement.Path(path)
            _uiState.value = _uiState.value.copy(
                elements = _uiState.value.elements + newElement,
                currentPath = null
            )
            currentPathPoints = mutableListOf()
        }
    }

    fun addStamp(offset: Offset) {
        if (_uiState.value.currentTool == DrawingTool.STAMP) {
            val newElement = DrawingElement.StampData(
                position = offset,
                type = _uiState.value.selectedStamp,
                color = _uiState.value.currentColor,
                size = _uiState.value.currentStrokeWidth
            )
            _uiState.value = _uiState.value.copy(
                elements = _uiState.value.elements + newElement
            )
        }
    }

    private fun updateCurrentPathState() {
        _uiState.value = _uiState.value.copy(
            currentPath = createCurrentPath()
        )
    }

    private fun createCurrentPath(): DrawingPath {
        val color = if (_uiState.value.currentTool == DrawingTool.ERASER) Color.White else _uiState.value.currentColor
        val stroke = if (_uiState.value.currentTool == DrawingTool.ERASER) 40f else _uiState.value.currentStrokeWidth
        return DrawingPath(currentPathPoints.toList(), color, stroke)
    }
}
