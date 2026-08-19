package com.vocalrange.analyzer.audio

import android.Manifest
import androidx.annotation.RequiresPermission
import com.vocalrange.analyzer.core.NoteUtils
import com.vocalrange.analyzer.core.VolumeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** 1解析フレーム分のピッチ/音量情報 */
data class PitchFrame(
    val timestampMs: Long,
    val frequencyHz: Double?,
    val noteInfo: NoteUtils.NoteInfo?,
    val volumeDb: Double,
    val isVoiced: Boolean
)

/**
 * マイク入力からピッチ(基本周波数)と音量をリアルタイムに解析し、[PitchFrame] の Flow として提供する。
 *
 * @param silenceThresholdDb この値未満の音量はピッチ検出をスキップし「無声」として扱う(ノイズ対策)
 */
class PitchTracker(
    sampleRate: Int = 44100,
    private val silenceThresholdDb: Double = -50.0
) {
    private val audioCapture = AudioCapture(sampleRate = sampleRate)
    private val pitchDetector = PitchDetector(sampleRate = sampleRate)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun track(): Flow<PitchFrame> = audioCapture.captureFrames().map { window ->
        val rms = VolumeUtils.rms(window)
        val volumeDb = VolumeUtils.rmsToDbFs(rms)
        val hasEnoughVolume = volumeDb > silenceThresholdDb

        val pitchResult = if (hasEnoughVolume) pitchDetector.detectPitch(window) else null
        val noteInfo = pitchResult?.let { NoteUtils.analyze(it.frequencyHz) }

        PitchFrame(
            timestampMs = System.currentTimeMillis(),
            frequencyHz = pitchResult?.frequencyHz,
            noteInfo = noteInfo,
            volumeDb = volumeDb,
            isVoiced = pitchResult != null
        )
    }.flowOn(Dispatchers.Default) // YIN計算(CPU負荷)をメインスレッド外で実行する
}
