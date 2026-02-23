package net.miksoft.kidsario.presentation.wordsGame

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordsGameLayoutTest {
    @Test
    fun shortHeightLayoutUsesCompactSizing() {
        val layout = calculateWordsGameLayout(500.dp)

        assertTrue(layout.isShortHeight)
        assertEquals(110.dp, layout.animalSize)
        assertEquals(18.sp, layout.wordFontSize)
        assertEquals(4.dp, layout.wordPadding)
        assertEquals(8.dp, layout.wordSpacing)
        assertEquals(225.dp, layout.optionsMaxHeight)
    }

    @Test
    fun tallHeightLayoutUsesRegularSizing() {
        val layout = calculateWordsGameLayout(800.dp)

        assertTrue(!layout.isShortHeight)
        assertEquals(140.dp, layout.animalSize)
        assertEquals(20.sp, layout.wordFontSize)
        assertEquals(6.dp, layout.wordPadding)
        assertEquals(12.dp, layout.wordSpacing)
        assertEquals(800.dp, layout.optionsMaxHeight)
    }
}