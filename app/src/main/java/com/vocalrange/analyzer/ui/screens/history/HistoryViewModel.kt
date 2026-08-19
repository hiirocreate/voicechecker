package com.vocalrange.analyzer.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repository: VoiceRepository) : ViewModel() {

    val rangeSessions: StateFlow<List<RangeSessionSummary>> = repository.observeRangeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vibratoSessions: StateFlow<List<VibratoSessionSummary>> = repository.observeVibratoSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
