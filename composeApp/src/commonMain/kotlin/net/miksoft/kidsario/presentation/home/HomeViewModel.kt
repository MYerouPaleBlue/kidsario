package net.miksoft.kidsario.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel : ViewModel() {
    
    // State for the Home screen
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    /**
     * Handle click on a mini game button
     * 
     * @param gameId The ID of the mini game that was clicked
     */
    fun onMiniGameClicked(gameId: String) {
        viewModelScope.launch {
            // In the future, this could navigate to the selected mini game
            // or update the state based on the selected game
            _uiState.value = _uiState.value.copy(
                lastClickedGameId = gameId
            )
        }
    }
}

/**
 * Data class representing the UI state for the Home screen
 */
data class HomeUiState(
    val lastClickedGameId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)