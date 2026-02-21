package net.miksoft.kidsario.presentation.jigsawPuzzle

data class PuzzleLayout(
    val isShortHeight: Boolean,
    val puzzleAreaStartX: Float,
    val puzzleAreaStartY: Float,
    val puzzleSize: Float,
    val trayStartX: Float,
    val trayStartY: Float,
    val trayWidth: Float,
    val trayHeight: Float,
    val trayColumns: Int
)

fun calculatePuzzleLayout(boardWidth: Float, boardHeight: Float): PuzzleLayout {
    val isShortHeight = boardHeight < boardWidth * 0.75f

    return if (isShortHeight) {
        val horizontalPadding = boardWidth * 0.05f
        val verticalPadding = boardHeight * 0.05f
        val maxPuzzleWidth = boardWidth * 0.6f
        val maxPuzzleHeight = boardHeight * 0.85f
        val puzzleSize = minOf(maxPuzzleWidth, maxPuzzleHeight)
        val puzzleAreaStartX = horizontalPadding
        val puzzleAreaStartY = (boardHeight - puzzleSize) / 2f
        val trayStartX = puzzleAreaStartX + puzzleSize + horizontalPadding
        val trayWidth = (boardWidth - trayStartX - horizontalPadding).coerceAtLeast(boardWidth * 0.15f)
        val trayStartY = verticalPadding
        val trayHeight = boardHeight - (verticalPadding * 2f)

        PuzzleLayout(
            isShortHeight = true,
            puzzleAreaStartX = puzzleAreaStartX,
            puzzleAreaStartY = puzzleAreaStartY,
            puzzleSize = puzzleSize,
            trayStartX = trayStartX,
            trayStartY = trayStartY,
            trayWidth = trayWidth,
            trayHeight = trayHeight,
            trayColumns = 2
        )
    } else {
        val maxPuzzleWidth = boardWidth * 0.8f
        val maxPuzzleHeight = boardHeight * 0.5f
        val puzzleSize = minOf(maxPuzzleWidth, maxPuzzleHeight)
        val puzzleAreaStartX = (boardWidth - puzzleSize) / 2f
        val puzzleAreaStartY = boardHeight * 0.05f
        val trayStartX = 0f
        val trayStartY = boardHeight * 0.6f
        val trayWidth = boardWidth
        val trayHeight = boardHeight - trayStartY

        PuzzleLayout(
            isShortHeight = false,
            puzzleAreaStartX = puzzleAreaStartX,
            puzzleAreaStartY = puzzleAreaStartY,
            puzzleSize = puzzleSize,
            trayStartX = trayStartX,
            trayStartY = trayStartY,
            trayWidth = trayWidth,
            trayHeight = trayHeight,
            trayColumns = 0
        )
    }
}