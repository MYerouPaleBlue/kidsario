package net.miksoft.kidsario

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import net.miksoft.kidsario.presentation.home.HomeComponent
import net.miksoft.kidsario.presentation.home.HomeViewModel
import net.miksoft.kidsario.presentation.countingObjects.CountingObjectsComponent
import net.miksoft.kidsario.presentation.countingObjects.CountingObjectsViewModel
import net.miksoft.kidsario.presentation.drawingLetters.DrawingLettersComponent
import net.miksoft.kidsario.presentation.drawingLetters.DrawingLettersViewModel
import net.miksoft.kidsario.presentation.maze.MazeComponent
import net.miksoft.kidsario.presentation.maze.MazeViewModel
import net.miksoft.kidsario.presentation.freeDrawing.FreeDrawingComponent
import net.miksoft.kidsario.presentation.freeDrawing.FreeDrawingViewModel
import net.miksoft.kidsario.presentation.jigsawPuzzle.JigsawPuzzleComponent
import net.miksoft.kidsario.presentation.jigsawPuzzle.JigsawPuzzleViewModel
import net.miksoft.kidsario.presentation.jumpingGame.JumpingGameComponent
import net.miksoft.kidsario.presentation.jumpingGame.JumpingGameViewModel
import net.miksoft.kidsario.presentation.mathGame.MathGameComponent
import net.miksoft.kidsario.presentation.mathGame.MathGameViewModel
import net.miksoft.kidsario.presentation.wordsGame.WordsGameComponent
import net.miksoft.kidsario.presentation.wordsGame.WordsGameViewModel
import net.miksoft.kidsario.theme.KidsarioTheme

// Enum to represent different screens in the app
enum class Screen {
    HOME,
    COUNTING_OBJECTS,
    DRAWING_LETTERS,
    WORDS_GAME,
    MATH_GAME,
    MAZE,
    FREE_DRAWING,
    JIGSAW_PUZZLE,
    JUMPING_GAME
}

@Composable
@Preview
fun App() {
    KidsarioTheme {
        // State to track the current screen
        var currentScreen by remember { mutableStateOf(Screen.HOME) }

        // Create ViewModel instances
        val homeViewModel = remember { HomeViewModel() }
        val countingObjectsViewModel = remember { CountingObjectsViewModel() }
        val drawingLettersViewModel = remember { DrawingLettersViewModel() }
        val wordsGameViewModel = remember { WordsGameViewModel() }
        val mathGameViewModel = remember { MathGameViewModel() }
        val mazeViewModel = remember { MazeViewModel() }
        val freeDrawingViewModel = remember { FreeDrawingViewModel() }
        val jigsawPuzzleViewModel = remember { JigsawPuzzleViewModel() }
        val jumpingGameViewModel = remember { JumpingGameViewModel() }

        // Handle navigation from home to mini games
        val onMiniGameClicked = { gameId: String ->
            when (gameId) {
                "counting_objects" -> currentScreen = Screen.COUNTING_OBJECTS
                "drawing_letters" -> currentScreen = Screen.DRAWING_LETTERS
                "words_game" -> currentScreen = Screen.WORDS_GAME
                "math_game" -> currentScreen = Screen.MATH_GAME
                "maze" -> currentScreen = Screen.MAZE
                "free_drawing" -> currentScreen = Screen.FREE_DRAWING
                "jigsaw_puzzle" -> currentScreen = Screen.JIGSAW_PUZZLE
                "jumping_game" -> currentScreen = Screen.JUMPING_GAME
                else -> homeViewModel.onMiniGameClicked(gameId)
            }
        }

        // Handle navigation back to home
        val onNavigateBack = {
            currentScreen = Screen.HOME
        }

        BackHandler(enabled = currentScreen != Screen.HOME) {
            onNavigateBack()
        }

        val modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())

        // Display the current screen
        when (currentScreen) {
            Screen.HOME -> {
                HomeComponent(
                    modifier = modifier,
                    viewModel = homeViewModel,
                    onMiniGameClicked = onMiniGameClicked
                )
            }
            Screen.COUNTING_OBJECTS -> {
                CountingObjectsComponent(
                    modifier = modifier,
                    viewModel = countingObjectsViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.DRAWING_LETTERS -> {
                DrawingLettersComponent(
                    modifier = modifier,
                    viewModel = drawingLettersViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.WORDS_GAME -> {
                WordsGameComponent(
                    modifier = modifier,
                    viewModel = wordsGameViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.MATH_GAME -> {
                MathGameComponent(
                    modifier = modifier,
                    viewModel = mathGameViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.MAZE -> {
                MazeComponent(
                    modifier = modifier,
                    viewModel = mazeViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.FREE_DRAWING -> {
                FreeDrawingComponent(
                    modifier = modifier,
                    viewModel = freeDrawingViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.JIGSAW_PUZZLE -> {
                JigsawPuzzleComponent(
                    modifier = modifier,
                    viewModel = jigsawPuzzleViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.JUMPING_GAME -> {
                JumpingGameComponent(
                    modifier = modifier,
                    viewModel = jumpingGameViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}
