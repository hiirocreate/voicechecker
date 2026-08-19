package com.vocalrange.analyzer.core

/** 声域測定1回分の、保存・表示両方に使うドメインモデル */
data class RangeSessionSummary(
    val id: Long = 0,
    val timestamp: Long,
    val lowestMidi: Int,
    val highestMidi: Int,
    val lowestLabel: String,
    val highestLabel: String,
    val semitoneSpan: Int,
    val voiceTypeName: String,
    val voiceTypeOverlapRatio: Double,
    val maleComparison: RangeComparison,
    val femaleComparison: RangeComparison,
    val volumeProfile: List<NoteVolume>,
    val overallAverageDb: Double,
    val strongZoneMidiRange: IntRange?,
    val weakZoneMidiRange: IntRange?,
    val recommendedSingMidiRange: IntRange?,
    val recommendedPracticeMidiRange: IntRange?
) {
    companion object {
        fun from(timestamp: Long, analysis: RangeAnalysisResult): RangeSessionSummary {
            val classification = VoiceTypeClassifier.classify(analysis.lowestMidi, analysis.highestMidi)
            return RangeSessionSummary(
                timestamp = timestamp,
                lowestMidi = analysis.lowestMidi,
                highestMidi = analysis.highestMidi,
                lowestLabel = analysis.lowestLabel,
                highestLabel = analysis.highestLabel,
                semitoneSpan = analysis.semitoneSpan,
                voiceTypeName = classification.closestType,
                voiceTypeOverlapRatio = classification.overlapRatio,
                maleComparison = RangeComparator.compareToMale(analysis.lowestMidi, analysis.highestMidi),
                femaleComparison = RangeComparator.compareToFemale(analysis.lowestMidi, analysis.highestMidi),
                volumeProfile = analysis.volumeProfile,
                overallAverageDb = analysis.overallAverageDb,
                strongZoneMidiRange = analysis.strongZoneMidiRange,
                weakZoneMidiRange = analysis.weakZoneMidiRange,
                recommendedSingMidiRange = analysis.recommendedSingMidiRange,
                recommendedPracticeMidiRange = analysis.recommendedPracticeMidiRange
            )
        }
    }
}

/** ビブラート/音程安定性テスト1回分のドメインモデル */
data class VibratoSessionSummary(
    val id: Long = 0,
    val timestamp: Long,
    val targetNoteLabel: String,
    val result: VibratoResult
)
