package com.vocalrange.analyzer.core

import com.vocalrange.analyzer.audio.PitchFrame
import kotlin.math.sqrt

/** 特定の音(MIDIノート番号)における音量サンプルの集計結果 */
data class NoteVolume(
    val midi: Int,
    val label: String,
    val averageDb: Double,
    val sampleCount: Int
)

/** 声域測定セッション1回分の解析結果 */
data class RangeAnalysisResult(
    val lowestMidi: Int,
    val highestMidi: Int,
    val lowestLabel: String,
    val highestLabel: String,
    val semitoneSpan: Int,
    val volumeProfile: List<NoteVolume>,
    val overallAverageDb: Double,
    val strongZoneMidiRange: IntRange?,
    val weakZoneMidiRange: IntRange?,
    val recommendedSingMidiRange: IntRange?,
    val recommendedPracticeMidiRange: IntRange?
)

/**
 * 声域測定中にリアルタイムで届く [PitchFrame] を蓄積し、
 * 最低音〜最高音、音域ごとの声量プロファイル、得意/苦手ゾーン、おすすめ音域を算出する。
 *
 * このクラスはUIスレッド外でも呼び出せるようスレッドセーフではないシンプルな実装のため、
 * 呼び出し元(ViewModel)で単一のコルーチンから逐次 [addFrame] すること。
 */
class RangeAnalyzer {

    private val samplesByNote = HashMap<Int, MutableList<Double>>()
    private var liveLowestMidi: Int? = null
    private var liveHighestMidi: Int? = null

    companion object {
        /** この回数未満しか検出されなかった音は「一瞬かすった」ノイズとみなし範囲判定から除外 */
        private const val MIN_SAMPLES_FOR_RANGE = 3

        /** 声量プロファイル(得意/苦手判定)に採用する最低サンプル数 */
        private const val MIN_SAMPLES_FOR_PROFILE = 5

        private const val STRONG_OFFSET_DB = 3.0
        private const val WEAK_OFFSET_DB = 4.0
        private const val MIN_ZONE_LENGTH_SEMITONES = 2
    }

    fun addFrame(frame: PitchFrame) {
        val note = frame.noteInfo ?: return
        val values = samplesByNote.getOrPut(note.midiNote) { mutableListOf() }
        values.add(frame.volumeDb)

        if (values.size >= MIN_SAMPLES_FOR_RANGE) {
            liveLowestMidi = liveLowestMidi?.let { minOf(it, note.midiNote) } ?: note.midiNote
            liveHighestMidi = liveHighestMidi?.let { maxOf(it, note.midiNote) } ?: note.midiNote
        }
    }

    fun reset() {
        samplesByNote.clear()
        liveLowestMidi = null
        liveHighestMidi = null
    }

    /** 測定中のリアルタイム表示用: 現時点で確定している最低音ラベル */
    fun currentLowestLabel(): String? = liveLowestMidi?.let { NoteUtils.noteLabel(it) }

    /** 測定中のリアルタイム表示用: 現時点で確定している最高音ラベル */
    fun currentHighestLabel(): String? = liveHighestMidi?.let { NoteUtils.noteLabel(it) }

    fun hasEnoughData(): Boolean {
        val low = liveLowestMidi
        val high = liveHighestMidi
        return low != null && high != null && high > low
    }

    fun buildResult(): RangeAnalysisResult? {
        val lowestMidi = liveLowestMidi ?: return null
        val highestMidi = liveHighestMidi ?: return null
        if (highestMidi <= lowestMidi) return null

        val profile = samplesByNote
            .filterValues { it.size >= MIN_SAMPLES_FOR_PROFILE }
            .map { (midi, values) ->
                NoteVolume(
                    midi = midi,
                    label = NoteUtils.noteLabel(midi),
                    averageDb = values.average(),
                    sampleCount = values.size
                )
            }
            .sortedBy { it.midi }

        val overallAverageDb = if (profile.isNotEmpty()) profile.map { it.averageDb }.average() else 0.0

        val strongZone = longestContiguousRun(profile) { it.averageDb >= overallAverageDb + STRONG_OFFSET_DB }
        val weakZone = longestContiguousRun(profile) { it.averageDb <= overallAverageDb - WEAK_OFFSET_DB }

        val recommendedSing = strongZone ?: fallbackComfortableRange(lowestMidi, highestMidi)
        val recommendedPractice = weakZone ?: fallbackWeakEdgeRange(profile, lowestMidi, highestMidi)

        return RangeAnalysisResult(
            lowestMidi = lowestMidi,
            highestMidi = highestMidi,
            lowestLabel = NoteUtils.noteLabel(lowestMidi),
            highestLabel = NoteUtils.noteLabel(highestMidi),
            semitoneSpan = highestMidi - lowestMidi,
            volumeProfile = profile,
            overallAverageDb = overallAverageDb,
            strongZoneMidiRange = strongZone,
            weakZoneMidiRange = weakZone,
            recommendedSingMidiRange = recommendedSing,
            recommendedPracticeMidiRange = recommendedPractice
        )
    }

    /** [predicate] を満たす音が半音単位で連続している最長区間を探す */
    private fun longestContiguousRun(
        profile: List<NoteVolume>,
        predicate: (NoteVolume) -> Boolean
    ): IntRange? {
        var bestRange: IntRange? = null
        var bestLength = 0
        var i = 0
        while (i < profile.size) {
            if (!predicate(profile[i])) {
                i++
                continue
            }
            var j = i
            while (j + 1 < profile.size &&
                profile[j + 1].midi == profile[j].midi + 1 &&
                predicate(profile[j + 1])
            ) {
                j++
            }
            val length = profile[j].midi - profile[i].midi + 1
            if (length > bestLength && length >= MIN_ZONE_LENGTH_SEMITONES) {
                bestLength = length
                bestRange = profile[i].midi..profile[j].midi
            }
            i = j + 1
        }
        return bestRange
    }

    /** 声量データから明確な得意ゾーンが検出できない場合、無理のない中央60%の音域を代替として提示 */
    private fun fallbackComfortableRange(lowestMidi: Int, highestMidi: Int): IntRange? {
        val span = highestMidi - lowestMidi
        if (span < 4) return null
        val margin = (span * 0.2).toInt().coerceAtLeast(1)
        return (lowestMidi + margin)..(highestMidi - margin)
    }

    /** 明確な苦手ゾーンが検出できない場合、音域の上下端(声量が落ちやすい領域)を代替として提示 */
    private fun fallbackWeakEdgeRange(profile: List<NoteVolume>, lowestMidi: Int, highestMidi: Int): IntRange? {
        val span = highestMidi - lowestMidi
        if (span < 4 || profile.isEmpty()) return null

        val edgeWidth = (span * 0.15).toInt().coerceIn(1, 3)
        val lowEdgeAvg = profile.filter { it.midi <= lowestMidi + edgeWidth }.map { it.averageDb }.average()
        val highEdgeAvg = profile.filter { it.midi >= highestMidi - edgeWidth }.map { it.averageDb }.average()

        return if (lowEdgeAvg.isNaN() && highEdgeAvg.isNaN()) {
            null
        } else if (highEdgeAvg.isNaN() || (!lowEdgeAvg.isNaN() && lowEdgeAvg <= highEdgeAvg)) {
            lowestMidi..(lowestMidi + edgeWidth)
        } else {
            (highestMidi - edgeWidth)..highestMidi
        }
    }
}

/** サンプルの標準偏差(母集団ではなく標本標準偏差) */
fun List<Double>.standardDeviation(): Double {
    if (size < 2) return 0.0
    val mean = average()
    val variance = sumOf { (it - mean) * (it - mean) } / (size - 1)
    return sqrt(variance)
}
