package net.miksoft.kidsario.presentation.jigsawPuzzle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JigsawPuzzleLayoutTest {
    @Test
    fun shortHeightLayoutUsesSideTrayWithTwoColumns() {
        val layout = calculatePuzzleLayout(boardWidth = 1000f, boardHeight = 500f)

        assertTrue(layout.isShortHeight)
        assertEquals(2, layout.trayColumns)
        assertTrue(layout.trayStartX > layout.puzzleAreaStartX + layout.puzzleSize)
    }

    @Test
    fun tallLayoutUsesBottomTray() {
        val boardWidth = 600f
        val boardHeight = 900f
        val layout = calculatePuzzleLayout(boardWidth = boardWidth, boardHeight = boardHeight)

        assertTrue(!layout.isShortHeight)
        assertEquals(0, layout.trayColumns)
        assertEquals(boardHeight * 0.6f, layout.trayStartY)
        assertEquals(boardWidth, layout.trayWidth)
    }
}