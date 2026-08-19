package com.vocalrange.analyzer.data

import com.vocalrange.analyzer.core.NoteUtils
import com.vocalrange.analyzer.core.NoteVolume
import com.vocalrange.analyzer.core.RangeComparison
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.core.VibratoResult
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.core.VoiceStability

fun RangeSessionSummary.toEntity(): RangeSessionEntity = RangeSessionEntity(
    id = id,
    timestamp = timestamp,
    lowestMidi = lowestMidi,
    highestMidi = highestMidi,
    lowestLabel = lowestLabel,
    highestLabel = highestLabel,
    semitoneSpan = semitoneSpan,
    voiceTypeName = voiceTypeName,
    voiceTypeOverlapRatio = voiceTypeOverlapRatio,
    maleDeltaLow = maleComparison.deltaLowSemitones,
    maleDeltaHigh = maleComparison.deltaHighSemitones,
    femaleDeltaLow = femaleComparison.deltaLowSemitones,
    femaleDeltaHigh = femaleComparison.deltaHighSemitones,
    overallAverageDb = overallAverageDb,
    strongZoneLow = strongZoneMidiRange?.first,
    strongZoneHigh = strongZoneMidiRange?.last,
    weakZoneLow = weakZoneMidiRange?.first,
    weakZoneHigh = weakZoneMidiRange?.last,
    recommendedSingLow = recommendedSingMidiRange?.first,
    recommendedSingHigh = recommendedSingMidiRange?.last,
    recommendedPracticeLow = recommendedPracticeMidiRange?.first,
    recommendedPracticeHigh = recommendedPracticeMidiRange?.last,
    volumeProfileCsv = volumeProfile.joinToString(";") { "${it.midi}:${it.averageDb}:${it.sampleCount}" }
)

fun RangeSessionEntity.toDomain(): RangeSessionSummary = RangeSessionSummary(
    id = id,
    timestamp = timestamp,
    lowestMidi = lowestMidi,
    highestMidi = highestMidi,
    lowestLabel = lowestLabel,
    highestLabel = highestLabel,
    semitoneSpan = semitoneSpan,
    voiceTypeName = voiceTypeName,
    voiceTypeOverlapRatio = voiceTypeOverlapRatio,
    maleComparison = RangeComparison(maleDeltaLow, maleDeltaHigh),
    femaleComparison = RangeComparison(femaleDeltaLow, femaleDeltaHigh),
    volumeProfile = parseVolumeProfile(volumeProfileCsv),
    overallAverageDb = overallAverageDb,
    strongZoneMidiRange = rangeOrNull(strongZoneLow, strongZoneHigh),
    weakZoneMidiRange = rangeOrNull(weakZoneLow, weakZoneHigh),
    recommendedSingMidiRange = rangeOrNull(recommendedSingLow, recommendedSingHigh),
    recommendedPracticeMidiRange = rangeOrNull(recommendedPracticeLow, recommendedPracticeHigh)
)

fun VibratoSessionSummary.toEntity(): VibratoSessionEntity = VibratoSessionEntity(
    id = id,
    timestamp = timestamp,
    targetNoteLabel = targetNoteLabel,
    vibratoDetected = result.vibratoDetected,
    rateHz = result.rateHz,
    extentCents = result.extentCentsPeakToPeak,
    jitterCents = result.jitterCents,
    stability = result.stability.name
)

fun VibratoSessionEntity.toDomain(): VibratoSessionSummary = VibratoSessionSummary(
    id = id,
    timestamp = timestamp,
    targetNoteLabel = targetNoteLabel,
    result = VibratoResult(
        vibratoDetected = vibratoDetected,
        rateHz = rateHz,
        extentCentsPeakToPeak = extentCents,
        jitterCents = jitterCents,
        stability = VoiceStability.valueOf(stability)
    )
)

private fun rangeOrNull(low: Int?, high: Int?): IntRange? =
    if (low != null && high != null) low..high else null

private fun parseVolumeProfile(csv: String): List<NoteVolume> {
    if (csv.isBlank()) return emptyList()
    return csv.split(";").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size != 3) return@mapNotNull null
        val midi = parts[0].toIntOrNull() ?: return@mapNotNull null
        val avgDb = parts[1].toDoubleOrNull() ?: return@mapNotNull null
        val count = parts[2].toIntOrNull() ?: return@mapNotNull null
        NoteVolume(midi = midi, label = NoteUtils.noteLabel(midi), averageDb = avgDb, sampleCount = count)
    }
}
