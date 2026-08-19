package com.vocalrange.analyzer.core

/**
 * 一般的な成人の平均的な歌唱可能音域の参考値。
 *
 * 声楽的な声種分類(バス〜テノール、アルト〜ソプラノ)で広く使われている音域を基に、
 * 「平均的な男性/女性の歌唱音域」として単純化した参考範囲。医学的・声楽的に厳密な
 * 個人差(訓練の有無、年齢等)は考慮していないため、あくまで自己比較の目安として扱う。
 */
object AverageRanges {
    /** 平均的な男性の歌唱音域の目安: 概ね E2〜C5 */
    val AVERAGE_MALE_RANGE: IntRange = NoteUtils.noteNameToMidi("E", 2)..NoteUtils.noteNameToMidi("C", 5)

    /** 平均的な女性の歌唱音域の目安: 概ね F3〜C6 */
    val AVERAGE_FEMALE_RANGE: IntRange = NoteUtils.noteNameToMidi("F", 3)..NoteUtils.noteNameToMidi("C", 6)
}

/** 平均音域との差分(半音数)。正の値 = 平均より高い/狭い側、負の値 = 平均より低い/広い側 */
data class RangeComparison(
    val deltaLowSemitones: Int,
    val deltaHighSemitones: Int
)

object RangeComparator {
    fun compareToMale(lowestMidi: Int, highestMidi: Int): RangeComparison =
        RangeComparison(
            deltaLowSemitones = lowestMidi - AverageRanges.AVERAGE_MALE_RANGE.first,
            deltaHighSemitones = highestMidi - AverageRanges.AVERAGE_MALE_RANGE.last
        )

    fun compareToFemale(lowestMidi: Int, highestMidi: Int): RangeComparison =
        RangeComparison(
            deltaLowSemitones = lowestMidi - AverageRanges.AVERAGE_FEMALE_RANGE.first,
            deltaHighSemitones = highestMidi - AverageRanges.AVERAGE_FEMALE_RANGE.last
        )
}
