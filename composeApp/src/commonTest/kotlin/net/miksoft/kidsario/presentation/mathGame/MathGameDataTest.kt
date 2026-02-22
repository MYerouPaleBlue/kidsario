package net.miksoft.kidsario.presentation.mathGame

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathGameDataTest {
    @Test
    fun generateProblemRespectsOperationsAndRange() {
        val range = 0..10
        val problem = MathGameData.generateProblem(range, setOf(MathOperation.SUBTRACT), Random(1))

        assertEquals(MathOperation.SUBTRACT, problem.operation)
        assertTrue(problem.left in range)
        assertTrue(problem.right in range)
        assertTrue(problem.left >= problem.right)
    }

    @Test
    fun buildOptionsIncludesCorrectAnswer() {
        val problem = MathProblem(4, 3, MathOperation.ADD)
        val options = MathGameData.buildOptions(problem, optionCount = 4, range = 0..10, random = Random(2))

        assertEquals(4, options.size)
        assertEquals(1, options.count { it.isCorrect })
        assertTrue(options.any { it.value == 7 })
    }

    @Test
    fun pickOperationHonorsAllowedSet() {
        val operation = MathGameData.pickOperation(setOf(MathOperation.ADD), Random(3))

        assertEquals(MathOperation.ADD, operation)
    }
}