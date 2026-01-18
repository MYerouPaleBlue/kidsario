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
import net.miksoft.kidsario.theme.KidsarioTheme

// Enum to represent different screens in the app
enum class Screen {
    HOME,
    COUNTING_OBJECTS,
    DRAWING_LETTERS,
    MAZE,
    FREE_DRAWING,
    JIGSAW_PUZZLE
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
        val mazeViewModel = remember { MazeViewModel() }
        val freeDrawingViewModel = remember { FreeDrawingViewModel() }
        val jigsawPuzzleViewModel = remember { JigsawPuzzleViewModel() }

        // Handle navigation from home to mini games
        val onMiniGameClicked = { gameId: String ->
            when (gameId) {
                "counting_objects" -> currentScreen = Screen.COUNTING_OBJECTS
                "drawing_letters" -> currentScreen = Screen.DRAWING_LETTERS
                "maze" -> currentScreen = Screen.MAZE
                "free_drawing" -> currentScreen = Screen.FREE_DRAWING
                "jigsaw_puzzle" -> currentScreen = Screen.JIGSAW_PUZZLE
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
        }
    }
}
