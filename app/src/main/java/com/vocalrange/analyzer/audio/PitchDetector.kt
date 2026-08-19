package com.vocalrange.analyzer.audio

/**
 * YIN アルゴリズムによる基本周波数(ピッチ)検出。
 * de Cheveigne & Kawahara, "YIN, a fundamental frequency estimator for speech and music" (2002) を実装。
 *
 * @param sampleRate 入力サンプルのサンプリングレート(Hz)
 * @param threshold  絶対閾値法のしきい値(小さいほど厳密。0.10〜0.20が一般的)
 * @param minFrequency 検出したい最低周波数。バッファサイズとの整合性チェックに使用
 */
class PitchDetector(
    private val sampleRate: Int,
    private val threshold: Double = 0.15,
    private val minFrequency: Double = 55.0
) {

    data class Result(val frequencyHz: Double, val confidence: Double)

    /**
     * [buffer] (正規化済み PCM, -1.0〜1.0) からピッチを検出する。
     * バッファサイズは少なくとも 2 * (sampleRate / minFrequency) 以上を推奨。
     * 無声(ピッチが見つからない)場合は null を返す。
     */
    fun detectPitch(buffer: FloatArray): Result? {
        val maxTau = buffer.size / 2
        if (maxTau < 2) return null

        val minDetectableFreq = sampleRate.toDouble() / maxTau
        // バッファが短すぎて minFrequency をカバーできない場合でも、可能な範囲で処理は続行する

        // 1. 差分関数 d(tau)
        val diff = DoubleArray(maxTau)
        for (tau in 1 until maxTau) {
            var sum = 0.0
            for (j in 0 until maxTau) {
                val delta = (buffer[j] - buffer[j + tau]).toDouble()
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // 2. 累積平均正規化差分関数 (CMND)
        val cmnd = DoubleArray(maxTau)
        cmnd[0] = 1.0
        var runningSum = 0.0
        for (tau in 1 until maxTau) {
            runningSum += diff[tau]
            cmnd[tau] = if (runningSum <= 1e-12) 1.0 else diff[tau] * tau / runningSum
        }

        // 3. 絶対閾値法によるtau候補探索
        var tauEstimate = -1
        var tau = 2
        while (tau < maxTau) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 < maxTau && cmnd[tau + 1] < cmnd[tau]) {
                    tau++
                }
                tauEstimate = tau
                break
            }
            tau++
        }

        if (tauEstimate == -1) {
            // 閾値以下の谷が見つからない = 明確な周期性なし(無声/ノイズ)
            return null
        }

        // 3.5 オクターブ下エラーの補正: 倍音の影響で、本来の基音周期(より短いtau)ではなく
        // その2倍・3倍の周期を誤って選んでしまうことがある(検出音が実際より1オクターブ前後低くなる)。
        // tauEstimateの約数にも十分深い極小点があれば、より短い周期(=より高い周波数)を優先する。
        tauEstimate = correctOctaveError(cmnd, tauEstimate, maxTau)

        // 4. 放物線補間でtauを精緻化
        val betterTau = parabolicInterpolate(cmnd, tauEstimate)
        if (betterTau <= 0.0) return null

        val frequency = sampleRate / betterTau
        if (frequency.isNaN() || frequency.isInfinite() || frequency <= 0.0) return null
        if (frequency < minDetectableFreq * 0.5) return null

        val confidence = (1.0 - cmnd[tauEstimate]).coerceIn(0.0, 1.0)
        return Result(frequency, confidence)
    }

    /**
     * [tauEstimate] の1/2, 1/3 の位置(=2倍・3倍の周波数)にも独立して閾値を満たす極小点があれば、
     * そちらを基音候補として採用する(オクターブ下エラーの補正)。
     */
    private fun correctOctaveError(cmnd: DoubleArray, tauEstimate: Int, maxTau: Int): Int {
        for (divisor in intArrayOf(2, 3)) {
            val candidate = tauEstimate / divisor
            if (candidate < 2 || candidate >= maxTau - 1) continue

            val isLocalMin = cmnd[candidate] <= cmnd[candidate - 1] && cmnd[candidate] <= cmnd[candidate + 1]
            if (isLocalMin && cmnd[candidate] < threshold) {
                return candidate
            }
        }
        return tauEstimate
    }

    private fun parabolicInterpolate(array: DoubleArray, tauEstimate: Int): Double {
        val x0 = if (tauEstimate < 1) tauEstimate else tauEstimate - 1
        val x2 = if (tauEstimate + 1 < array.size) tauEstimate + 1 else tauEstimate

        if (x0 == tauEstimate) {
            return if (array[tauEstimate] <= array[x2]) tauEstimate.toDouble() else x2.toDouble()
        }
        if (x2 == tauEstimate) {
            return if (array[tauEstimate] <= array[x0]) tauEstimate.toDouble() else x0.toDouble()
        }

        val s0 = array[x0]
        val s1 = array[tauEstimate]
        val s2 = array[x2]
        val denominator = 2.0 * (2.0 * s1 - s2 - s0)
        if (denominator == 0.0) return tauEstimate.toDouble()
        return tauEstimate + (s2 - s0) / denominator
    }
}
