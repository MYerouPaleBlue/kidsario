package net.miksoft.kidsario.presentation.mathGame

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MathGameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startGameActivatesGame() {
        val viewModel = MathGameViewModel()

        viewModel.startGame()

        assertTrue(viewModel.uiState.value.isGameActive)
    }

    @Test
    fun correctAnswerIncrementsScoreAndHighScore() = runTest {
        val viewModel = MathGameViewModel()

        viewModel.startGame()
        val correctOption = viewModel.uiState.value.options.first { it.isCorrect }

        viewModel.selectOption(correctOption)

        assertEquals(1, viewModel.uiState.value.score)
        assertEquals(1, viewModel.uiState.value.highScore)
        assertTrue(viewModel.uiState.value.isGameActive)
        viewModel.stopGame()
    }

    @Test
    fun correctAnswerClearsResultAfterRefresh() = runTest {
        val viewModel = MathGameViewModel()

        viewModel.startGame()
        val correctOption = viewModel.uiState.value.options.first { it.isCorrect }

        viewModel.selectOption(correctOption)

        assertEquals(true, viewModel.uiState.value.lastResultCorrect)

        advanceTimeBy(400)
        runCurrent()

        assertEquals(null, viewModel.uiState.value.lastResultCorrect)
        viewModel.stopGame()
    }

    @Test
    fun wrongAnswerKeepsSameProblemAndOptions() = runTest {
        val viewModel = MathGameViewModel()

        viewModel.startGame()
        val initialProblem = viewModel.uiState.value.currentProblem
        val initialOptions = viewModel.uiState.value.options
        val wrongOption = initialOptions.first { !it.isCorrect }

        viewModel.selectOption(wrongOption)

        assertEquals(0, viewModel.uiState.value.score)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(false, viewModel.uiState.value.lastResultCorrect)
        assertEquals(initialProblem, viewModel.uiState.value.currentProblem)
        assertEquals(initialOptions, viewModel.uiState.value.options)

        advanceTimeBy(400)
        runCurrent()

        assertEquals(initialProblem, viewModel.uiState.value.currentProblem)
        assertEquals(initialOptions, viewModel.uiState.value.options)
        viewModel.stopGame()
    }
}