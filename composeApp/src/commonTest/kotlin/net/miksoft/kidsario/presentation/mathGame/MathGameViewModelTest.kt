package net.miksoft.kidsario.presentation.mathGame

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}