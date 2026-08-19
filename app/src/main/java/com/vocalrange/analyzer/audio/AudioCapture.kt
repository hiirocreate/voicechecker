package com.vocalrange.analyzer.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.vocalrange.analyzer.core.VolumeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * マイクからPCM音声を継続的にキャプチャし、解析用の固定長ウィンドウ(スライディングウィンドウ)を
 * Flowとして発行するクラス。
 *
 * @param sampleRate 録音サンプリングレート
 * @param frameSize  1回の解析に使うウィンドウサイズ(サンプル数)
 * @param hopSize    ウィンドウを何サンプルずつスライドさせるか(小さいほど時間分解能が上がる)
 */
class AudioCapture(
    private val sampleRate: Int = 44100,
    private val frameSize: Int = 2048,
    private val hopSize: Int = 1024
) {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun captureFrames(): Flow<FloatArray> = flow {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        check(minBuf != AudioRecord.ERROR && minBuf != AudioRecord.ERROR_BAD_VALUE) {
            "この端末は指定したサンプリングレートでの録音に対応していません"
        }

        val internalBufferSize = maxOf(minBuf, frameSize * 4)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            internalBufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            error("マイクの初期化に失敗しました")
        }

        // リングバッファ(直近 frameSize サンプルを保持)
        val ring = ShortArray(frameSize)
        val readChunk = ShortArray(hopSize)
        var filled = 0

        try {
            audioRecord.startRecording()
            while (currentCoroutineContext().isActive) {
                val readCount = audioRecord.read(readChunk, 0, hopSize)
                if (readCount <= 0) continue

                if (readCount >= frameSize) {
                    System.arraycopy(readChunk, readCount - frameSize, ring, 0, frameSize)
                    filled = frameSize
                } else {
                    System.arraycopy(ring, readCount, ring, 0, frameSize - readCount)
                    System.arraycopy(readChunk, 0, ring, frameSize - readCount, readCount)
                    filled = minOf(frameSize, filled + readCount)
                }

                if (filled < frameSize) continue

                emit(VolumeUtils.shortsToNormalizedFloats(ring, frameSize))
            }
        } finally {
            runCatching { audioRecord.stop() }
            audioRecord.release()
        }
    }.flowOn(Dispatchers.IO)
}
