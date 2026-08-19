package com.vocalrange.analyzer.ui.screens.rangeresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RangeResultViewModel(
    private val repository: VoiceRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _summary = MutableStateFlow<RangeSessionSummary?>(null)
    val summary: StateFlow<RangeSessionSummary?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _summary.value = repository.getRangeSession(sessionId)
            _isLoading.value = false
        }
    }
}
