package com.vocalrange.analyzer.core

/**
 * 声楽で一般的に使われる声種区分(参考値)。実際の声種判定はブレイクポイントや声質など
 * 音域だけでは決まらない要素も多いため、ここでは「音域が最も近い/重なりが大きい声種」を
 * 簡易的な目安として提示する。
 */
data class VoiceTypeReference(val name: String, val range: IntRange)

object VoiceTypeClassifier {

    private val REFERENCE_TYPES = listOf(
        VoiceTypeReference("バス", NoteUtils.noteNameToMidi("E", 2)..NoteUtils.noteNameToMidi("E", 4)),
        VoiceTypeReference("バリトン", NoteUtils.noteNameToMidi("A", 2)..NoteUtils.noteNameToMidi("A", 4)),
        VoiceTypeReference("テノール", NoteUtils.noteNameToMidi("C", 3)..NoteUtils.noteNameToMidi("C", 5)),
        VoiceTypeReference("アルト", NoteUtils.noteNameToMidi("E", 3)..NoteUtils.noteNameToMidi("E", 5)),
        VoiceTypeReference("メゾソプラノ", NoteUtils.noteNameToMidi("A", 3)..NoteUtils.noteNameToMidi("A", 5)),
        VoiceTypeReference("ソプラノ", NoteUtils.noteNameToMidi("C", 4)..NoteUtils.noteNameToMidi("C", 6))
    )

    data class ClassificationResult(val closestType: String, val overlapRatio: Double)

    /** 測定した音域(MIDI)から最も近い声種を推定する */
    fun classify(lowestMidi: Int, highestMidi: Int): ClassificationResult {
        val userRange = lowestMidi..highestMidi
        val userSpan = (highestMidi - lowestMidi + 1).coerceAtLeast(1)

        var best: VoiceTypeReference? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for (type in REFERENCE_TYPES) {
            val overlapStart = maxOf(userRange.first, type.range.first)
            val overlapEnd = minOf(userRange.last, type.range.last)
            val overlap = (overlapEnd - overlapStart + 1).coerceAtLeast(0)

            val score = if (overlap > 0) {
                overlap.toDouble()
            } else {
                // 重なりがない場合は中心間の距離が近いほど高スコア(負の値)にする
                val userMid = (userRange.first + userRange.last) / 2.0
                val typeMid = (type.range.first + type.range.last) / 2.0
                -kotlin.math.abs(userMid - typeMid)
            }

            if (score > bestScore) {
                bestScore = score
                best = type
            }
        }

        val chosen = best ?: REFERENCE_TYPES.first()
        val overlapStart = maxOf(userRange.first, chosen.range.first)
        val overlapEnd = minOf(userRange.last, chosen.range.last)
        val overlap = (overlapEnd - overlapStart + 1).coerceAtLeast(0)
        val overlapRatio = overlap.toDouble() / userSpan

        return ClassificationResult(chosen.name, overlapRatio.coerceIn(0.0, 1.0))
    }
}
