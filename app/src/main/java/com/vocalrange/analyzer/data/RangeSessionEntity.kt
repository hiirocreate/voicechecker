package com.vocalrange.analyzer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "range_sessions")
data class RangeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val lowestMidi: Int,
    val highestMidi: Int,
    val lowestLabel: String,
    val highestLabel: String,
    val semitoneSpan: Int,
    val voiceTypeName: String,
    val voiceTypeOverlapRatio: Double,
    val maleDeltaLow: Int,
    val maleDeltaHigh: Int,
    val femaleDeltaLow: Int,
    val femaleDeltaHigh: Int,
    val overallAverageDb: Double,
    val strongZoneLow: Int?,
    val strongZoneHigh: Int?,
    val weakZoneLow: Int?,
    val weakZoneHigh: Int?,
    val recommendedSingLow: Int?,
    val recommendedSingHigh: Int?,
    val recommendedPracticeLow: Int?,
    val recommendedPracticeHigh: Int?,
    /** "midi:avgDb:count;midi:avgDb:count;..." 形式でシリアライズした声量プロファイル */
    val volumeProfileCsv: String
)
