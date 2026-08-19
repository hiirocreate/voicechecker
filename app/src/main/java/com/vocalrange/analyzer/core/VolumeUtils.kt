package com.vocalrange.analyzer.core

import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 音量(音圧)関連の計算ユーティリティ。
 *
 * 注意: スマートフォン内蔵マイクはデバイスごとに感度が異なり、キャリブレーションなしでは
 * 絶対的な dB SPL (音圧レベル) を測定することはできない。このアプリが示す値は
 * 16bit PCM フルスケールを基準とした相対値 (dBFS 相当) であり、
 * 「自分自身の声の中での相対的な強弱」を比較するための指標として利用する。
 */
object VolumeUtils {

    private const val SILENCE_FLOOR_DB = -90.0

    /** PCM(-1.0〜1.0に正規化済み)バッファのRMS(二乗平均平方根)を計算 */
    fun rms(samples: FloatArray): Double {
        if (samples.isEmpty()) return 0.0
        var sum = 0.0
        for (s in samples) {
            sum += (s * s).toDouble()
        }
        return sqrt(sum / samples.size)
    }

    /** RMS値をdBFS(フルスケール相対)に変換。無音時はSILENCE_FLOOR_DBを返す */
    fun rmsToDbFs(rmsValue: Double): Double {
        if (rmsValue <= 0.0) return SILENCE_FLOOR_DB
        val db = 20.0 * log10(rmsValue)
        return db.coerceAtLeast(SILENCE_FLOOR_DB)
    }

    /** 16bit PCM ShortArray を -1.0〜1.0 に正規化した FloatArray に変換 */
    fun shortsToNormalizedFloats(shorts: ShortArray, length: Int = shorts.size): FloatArray {
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = shorts[i] / 32768.0f
        }
        return result
    }
}
