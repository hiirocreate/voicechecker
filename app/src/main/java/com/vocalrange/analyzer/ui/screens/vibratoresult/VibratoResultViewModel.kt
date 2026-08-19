package com.vocalrange.analyzer.ui.screens.vibratoresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VibratoResultViewModel(
    private val repository: VoiceRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _summary = MutableStateFlow<VibratoSessionSummary?>(null)
    val summary: StateFlow<VibratoSessionSummary?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _summary.value = repository.getVibratoSession(sessionId)
            _isLoading.value = false
        }
    }
}
