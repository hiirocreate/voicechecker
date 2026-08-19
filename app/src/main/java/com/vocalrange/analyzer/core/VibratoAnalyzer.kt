package com.vocalrange.analyzer.core

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

enum class VoiceStability {
    STABLE, SLIGHTLY_UNSTABLE, UNSTABLE
}

data class VibratoResult(
    val vibratoDetected: Boolean,
    val rateHz: Double?,
    val extentCentsPeakToPeak: Double?,
    val jitterCents: Double,
    val stability: VoiceStability
)

/**
 * 一定時間伸ばした声のピッチ時系列からビブラート(周期的な音程のゆらぎ)と
 * 音程の安定性を簡易的に解析する。
 *
 * 注意: これは臨床・声楽の専門機器のような厳密なジッター解析ではなく、
 * ピッチトラッキング結果から推定する簡易的な指標。
 */
object VibratoAnalyzer {

    private const val MIN_SAMPLES = 25
    private const val PEAK_PROMINENCE_CENTS = 2.0
    private const val MIN_VIBRATO_RATE_HZ = 3.0
    private const val MAX_VIBRATO_RATE_HZ = 8.0
    private const val MIN_EXTENT_CENTS = 15.0
    private const val MAX_EXTENT_CENTS = 400.0

    /**
     * @param samples (タイムスタンプms, 周波数Hz) のリスト。持続音テスト中に検出できた
     *                有声フレームのみを時系列順に渡すこと。
     */
    fun analyze(samples: List<Pair<Long, Double>>): VibratoResult? {
        if (samples.size < MIN_SAMPLES) return null

        val frequencies = samples.map { it.second }
        val sortedFreq = frequencies.sorted()
        val medianFreq = sortedFreq[sortedFreq.size / 2]
        if (medianFreq <= 0.0) return null

        val cents = frequencies.map { 1200.0 * (ln(it / medianFreq) / ln(2.0)) }

        // ジッター: フレーム間のピッチ変動量(セント)の平均絶対値
        val jitterCents = cents.zipWithNext { a, b -> abs(b - a) }.average()

        // 平均サンプリング間隔からムービングアベレージのウィンドウ幅(約150ms相当)を決定
        val totalDurationMs = (samples.last().first - samples.first().first).coerceAtLeast(1)
        val avgDtMs = totalDurationMs.toDouble() / (samples.size - 1)
        val windowSize = (150.0 / avgDtMs).toInt().coerceIn(3, samples.size / 2)

        val trend = movingAverage(cents, windowSize)
        val detrended = cents.indices.map { cents[it] - trend[it] }

        // 極大点(ピーク)検出
        val peakIndices = mutableListOf<Int>()
        val minSpacingMs = 1000.0 / MAX_VIBRATO_RATE_HZ
        for (i in 1 until detrended.size - 1) {
            val isPeak = detrended[i] > detrended[i - 1] &&
                detrended[i] >= detrended[i + 1] &&
                detrended[i] > PEAK_PROMINENCE_CENTS
            if (!isPeak) continue

            val lastPeakTime = peakIndices.lastOrNull()?.let { samples[it].first }
            val currentTime = samples[i].first
            if (lastPeakTime == null || (currentTime - lastPeakTime) >= minSpacingMs) {
                peakIndices.add(i)
            }
        }

        val extentStdDev = detrended.standardDeviation()
        val extentPeakToPeak = extentStdDev * sqrt(2.0) * 2.0

        var rateHz: Double? = null
        if (peakIndices.size >= 2) {
            val firstT = samples[peakIndices.first()].first
            val lastT = samples[peakIndices.last()].first
            val durationSec = (lastT - firstT) / 1000.0
            if (durationSec > 0) {
                rateHz = (peakIndices.size - 1) / durationSec
            }
        }

        val vibratoDetected = rateHz != null &&
            rateHz in MIN_VIBRATO_RATE_HZ..MAX_VIBRATO_RATE_HZ &&
            extentPeakToPeak in MIN_EXTENT_CENTS..MAX_EXTENT_CENTS

        val stability = when {
            jitterCents < 12.0 && extentPeakToPeak < 60.0 -> VoiceStability.STABLE
            jitterCents < 30.0 && extentPeakToPeak < 150.0 -> VoiceStability.SLIGHTLY_UNSTABLE
            else -> VoiceStability.UNSTABLE
        }

        return VibratoResult(
            vibratoDetected = vibratoDetected,
            rateHz = if (vibratoDetected) rateHz else null,
            extentCentsPeakToPeak = if (vibratoDetected) extentPeakToPeak else null,
            jitterCents = jitterCents,
            stability = stability
        )
    }

    private fun movingAverage(values: List<Double>, windowSize: Int): List<Double> {
        if (windowSize <= 1) return values
        val half = windowSize / 2
        return values.indices.map { i ->
            val from = (i - half).coerceAtLeast(0)
            val to = (i + half).coerceAtMost(values.size - 1)
            var sum = 0.0
            for (k in from..to) sum += values[k]
            sum / (to - from + 1)
        }
    }
}
