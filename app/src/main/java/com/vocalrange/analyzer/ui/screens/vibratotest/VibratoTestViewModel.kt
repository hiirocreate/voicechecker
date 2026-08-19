package com.vocalrange.analyzer.ui.screens.vibratotest

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocalrange.analyzer.audio.PitchTracker
import com.vocalrange.analyzer.core.VibratoAnalyzer
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.core.VolumeSmoother
import com.vocalrange.analyzer.data.VoiceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VibratoTestUiState(
    val isRecording: Boolean = false,
    val currentNoteLabel: String? = null,
    val currentVolumeDb: Double = -90.0,
    val elapsedMs: Long = 0L,
    val targetDurationMs: Long = TARGET_DURATION_MS,
    val isSaving: Boolean = false,
    val savedSessionId: Long? = null,
    val errorMessage: String? = null
) {
    companion object {
        const val TARGET_DURATION_MS = 6000L
    }
}

class VibratoTestViewModel(private val repository: VoiceRepository) : ViewModel() {

    private val pitchTracker = PitchTracker()
    private val volumeSmoother = VolumeSmoother()
    private var trackingJob: Job? = null
    private val samples = mutableListOf<Pair<Long, Double>>()
    private val noteLabelCounts = mutableMapOf<String, Int>()
    private var startTimeMs = 0L

    private val _uiState = MutableStateFlow(VibratoTestUiState())
    val uiState: StateFlow<VibratoTestUiState> = _uiState.asStateFlow()

    /** マイク権限は呼び出し元のUI(MicrophonePermissionGate)で許可済みであることを前提とする */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (trackingJob?.isActive == true) return
        samples.clear()
        noteLabelCounts.clear()
        volumeSmoother.reset()
        startTimeMs = System.currentTimeMillis()
        _uiState.value = VibratoTestUiState(isRecording = true)

        trackingJob = viewModelScope.launch {
            try {
                pitchTracker.track().collect { frame ->
                    val elapsed = System.currentTimeMillis() - startTimeMs

                    if (frame.isVoiced && frame.frequencyHz != null) {
                        samples.add(frame.timestampMs to frame.frequencyHz)
                        frame.noteInfo?.label?.let { label ->
                            noteLabelCounts[label] = (noteLabelCounts[label] ?: 0) + 1
                        }
                    }

                    val displayVolumeDb = volumeSmoother.next(frame.volumeDb)
                    _uiState.update {
                        it.copy(
                            currentNoteLabel = frame.noteInfo?.label,
                            currentVolumeDb = displayVolumeDb,
                            elapsedMs = elapsed
                        )
                    }

                    if (elapsed >= VibratoTestUiState.TARGET_DURATION_MS) {
                        finishAndSave()
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isRecording = false, errorMessage = t.message ?: "録音中にエラーが発生しました")
                }
            }
        }
    }

    fun stopEarly() {
        if (trackingJob?.isActive == true) {
            finishAndSave()
        }
    }

    private fun finishAndSave() {
        trackingJob?.cancel()
        trackingJob = null

        val result = VibratoAnalyzer.analyze(samples)
        if (result == null) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "十分なデータを取得できませんでした。同じ高さの声をもう少し長く(5秒以上)伸ばしてみてください。"
                )
            }
            return
        }

        val targetLabel = noteLabelCounts.maxByOrNull { it.value }?.key ?: "-"
        _uiState.update { it.copy(isRecording = false, isSaving = true) }

        viewModelScope.launch {
            val summary = VibratoSessionSummary(
                timestamp = System.currentTimeMillis(),
                targetNoteLabel = targetLabel,
                result = result
            )
            val id = repository.saveVibratoSession(summary)
            _uiState.update { it.copy(isSaving = false, savedSessionId = id) }
        }
    }

    fun cancelRecording() {
        trackingJob?.cancel()
        trackingJob = null
        samples.clear()
        noteLabelCounts.clear()
        volumeSmoother.reset()
        _uiState.value = VibratoTestUiState()
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}
