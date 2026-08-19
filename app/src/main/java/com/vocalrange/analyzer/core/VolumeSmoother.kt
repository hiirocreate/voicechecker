package com.vocalrange.analyzer.core

/**
 * リアルタイム表示用に音量(dB)へVUメーター風の「弾道特性(ballistics)」を適用するクラス。
 *
 * 生のフレームごとの音量値は細かく揺れ動くため、そのまま表示すると視認しづらい。
 * 立ち上がり(attack)は素早く追従し、下降(release)はゆっくり追従させることで、
 * 発声の変化には敏感なまま、見た目のガタつきだけを抑える。
 *
 * 注意: これは表示専用の平滑化であり、声域測定やビブラート解析に使う生の音量値
 * ([com.vocalrange.analyzer.audio.PitchFrame.volumeDb]) には一切影響しない。
 */
class VolumeSmoother(
    private val attackFactor: Double = 0.6,
    private val releaseFactor: Double = 0.15,
    initialDb: Double = -90.0
) {
    private var current = initialDb

    fun next(rawDb: Double): Double {
        current = if (rawDb > current) {
            current + (rawDb - current) * attackFactor
        } else {
            current + (rawDb - current) * releaseFactor
        }
        return current
    }

    fun reset(db: Double = -90.0) {
        current = db
    }
}
