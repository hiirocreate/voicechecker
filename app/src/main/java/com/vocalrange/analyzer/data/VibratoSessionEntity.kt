package com.vocalrange.analyzer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vibrato_sessions")
data class VibratoSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val targetNoteLabel: String,
    val vibratoDetected: Boolean,
    val rateHz: Double?,
    val extentCents: Double?,
    val jitterCents: Double,
    val stability: String
)
