package net.miksoft.kidsario.presentation.jigsawPuzzle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class JigsawPuzzlePiecePlacementTest {
    @Test
    fun shortHeightLayoutPlacesPiecesInSideTray() {
        val boardWidth = 1000f
        val boardHeight = 500f
        val layout = calculatePuzzleLayout(boardWidth, boardHeight)
        val pieces = List(6) { index ->
            PuzzlePiece(
                id = index,
                correctRow = 0,
                correctCol = 0,
                currentX = 0.5f,
                currentY = 0.5f,
                isPlaced = false
            )
        }

        val positioned = positionPiecesForLayout(pieces, layout, boardWidth, boardHeight, Random(1))

        positioned.forEach { piece ->
            val x = piece.currentX * boardWidth
            val y = piece.currentY * boardHeight
            assertTrue(x >= layout.trayStartX && x <= layout.trayStartX + layout.trayWidth)
            assertTrue(y >= layout.trayStartY && y <= layout.trayStartY + layout.trayHeight)
        }
    }

    @Test
    fun tallLayoutPlacesPiecesInBottomTray() {
        val boardWidth = 600f
        val boardHeight = 900f
        val layout = calculatePuzzleLayout(boardWidth, boardHeight)
        val pieces = List(4) { index ->
            PuzzlePiece(
                id = index,
                correctRow = 0,
                correctCol = 0,
                currentX = 0.5f,
                currentY = 0.5f,
                isPlaced = false
            )
        }

        val positioned = positionPiecesForLayout(pieces, layout, boardWidth, boardHeight, Random(2))

        positioned.forEach { piece ->
            val x = piece.currentX * boardWidth
            val y = piece.currentY * boardHeight
            assertTrue(x >= layout.trayStartX && x <= layout.trayStartX + layout.trayWidth)
            assertTrue(y >= layout.trayStartY && y <= layout.trayStartY + layout.trayHeight)
        }
    }
}