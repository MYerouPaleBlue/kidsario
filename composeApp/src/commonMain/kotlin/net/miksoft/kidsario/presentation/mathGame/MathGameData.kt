package net.miksoft.kidsario.presentation.mathGame

import kotlin.random.Random

enum class MathOperation(val symbol: String, val displayName: String) {
    ADD("+", "Addition"),
    SUBTRACT("−", "Subtraction")
}

enum class MathRangeOption(val displayName: String, val range: IntRange) {
    SMALL("0-10", 0..10),
    MEDIUM("0-20", 0..20),
    LARGE("0-50", 0..50)
}

data class MathProblem(
    val left: Int,
    val right: Int,
    val operation: MathOperation
) {
    val answer: Int
        get() = when (operation) {
            MathOperation.ADD -> left + right
            MathOperation.SUBTRACT -> left - right
        }

    fun format(): String = "$left ${operation.symbol} $right = ?"
}

data class MathOption(
    val value: Int,
    val isCorrect: Boolean
)

object MathGameData {
    fun pickOperation(
        allowed: Set<MathOperation>,
        random: Random = Random.Default
    ): MathOperation {
        val safeOperations = if (allowed.isEmpty()) MathOperation.values().toSet() else allowed
        return safeOperations.random(random)
    }

    fun generateProblem(
        range: IntRange,
        allowed: Set<MathOperation>,
        random: Random = Random.Default
    ): MathProblem {
        val operation = pickOperation(allowed, random)
        val left = random.nextInt(range.first, range.last + 1)
        val right = random.nextInt(range.first, range.last + 1)
        return if (operation == MathOperation.SUBTRACT && right > left) {
            MathProblem(right, left, operation)
        } else {
            MathProblem(left, right, operation)
        }
    }

    fun buildOptions(
        problem: MathProblem,
        optionCount: Int,
        range: IntRange,
        random: Random = Random.Default
    ): List<MathOption> {
        val maxOption = when (problem.operation) {
            MathOperation.ADD -> range.last + range.last
            MathOperation.SUBTRACT -> range.last
        }
        val minOption = 0
        val targetCount = optionCount.coerceAtLeast(2)
        val values = mutableSetOf(problem.answer)

        while (values.size < targetCount) {
            val candidate = random.nextInt(minOption, maxOption + 1)
            values.add(candidate)
        }

        return values.shuffled(random).map { value ->
            MathOption(value = value, isCorrect = value == problem.answer)
        }
    }
}