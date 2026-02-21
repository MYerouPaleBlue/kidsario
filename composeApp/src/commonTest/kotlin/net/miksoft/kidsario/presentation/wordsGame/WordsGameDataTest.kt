package net.miksoft.kidsario.presentation.wordsGame

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordsGameDataTest {
    @Test
    fun pickTargetLetterUsesWordList() {
        val words = listOf("Apple", "Bear", "Cat")
        val letter = WordsGameData.pickTargetLetter(words, Random(3))

        assertTrue(letter == 'A' || letter == 'B' || letter == 'C')
    }

    @Test
    fun buildOptionsIncludesCorrectChoice() {
        val words = listOf("Apple", "Ant", "Bear", "Car")
        val options = WordsGameData.buildOptions('A', words, optionCount = 3, random = Random(1))

        assertEquals(3, options.size)
        assertEquals(1, options.count { it.isCorrect })
        assertTrue(options.any { it.word.startsWith("A") })
    }

    @Test
    fun buildOptionsRespectsOptionCount() {
        val words = listOf("Apple", "Ant", "Axe", "Bear", "Boat", "Car", "Cat")
        val options = WordsGameData.buildOptions('A', words, optionCount = 5, random = Random(2))

        assertEquals(5, options.size)
        assertEquals(1, options.count { it.isCorrect })
        assertTrue(options.any { it.word.startsWith("A") })
    }

    @Test
    fun startsWithLetterHandlesGreek() {
        assertTrue(WordsGameData.startsWithLetter("Αρκούδα", 'Α'))
        assertFalse(WordsGameData.startsWithLetter("Βάρκα", 'Α'))
    }
}