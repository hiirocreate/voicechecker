package com.vocalrange.analyzer.ui.screens.rangetest

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocalrange.analyzer.audio.PitchTracker
import com.vocalrange.analyzer.core.RangeAnalyzer
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RangeTestUiState(
    val isRecording: Boolean = false,
    val currentNoteLabel: String? = null,
    val currentVolumeDb: Double = -90.0,
    val isVoiced: Boolean = false,
    val lowestLabel: String? = null,
    val highestLabel: String? = null,
    val canFinish: Boolean = false,
    val isSaving: Boolean = false,
    val savedSessionId: Long? = null,
    val errorMessage: String? = null
)

class RangeTestViewModel(private val repository: VoiceRepository) : ViewModel() {

    private val analyzer = RangeAnalyzer()
    private val pitchTracker = PitchTracker()
    private var trackingJob: Job? = null

    private val _uiState = MutableStateFlow(RangeTestUiState())
    val uiState: StateFlow<RangeTestUiState> = _uiState.asStateFlow()

    /** マイク権限は呼び出し元のUI(MicrophonePermissionGate)で許可済みであることを前提とする */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (trackingJob?.isActive == true) return
        analyzer.reset()
        _uiState.value = RangeTestUiState(isRecording = true)

        trackingJob = viewModelScope.launch {
            try {
                pitchTracker.track().collect { frame ->
                    analyzer.addFrame(frame)
                    _uiState.update { current ->
                        current.copy(
                            currentNoteLabel = frame.noteInfo?.label,
                            currentVolumeDb = frame.volumeDb,
                            isVoiced = frame.isVoiced,
                            lowestLabel = analyzer.currentLowestLabel() ?: current.lowestLabel,
                            highestLabel = analyzer.currentHighestLabel() ?: current.highestLabel,
                            canFinish = analyzer.hasEnoughData()
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isRecording = false, errorMessage = t.message ?: "録音中にエラーが発生しました")
                }
            }
        }
    }

    fun stopRecordingAndSave() {
        trackingJob?.cancel()
        trackingJob = null

        val result = analyzer.buildResult()
        if (result == null) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "十分なデータを取得できませんでした。低い声から高い声まで、ゆっくり途切れずに発声してみてください。"
                )
            }
            return
        }

        _uiState.update { it.copy(isRecording = false, isSaving = true) }
        viewModelScope.launch {
            val summary = RangeSessionSummary.from(System.currentTimeMillis(), result)
            val id = repository.saveRangeSession(summary)
            _uiState.update { it.copy(isSaving = false, savedSessionId = id) }
        }
    }

    fun cancelRecording() {
        trackingJob?.cancel()
        trackingJob = null
        analyzer.reset()
        _uiState.value = RangeTestUiState()
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}
