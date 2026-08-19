package com.vocalrange.analyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vocalrange.analyzer.core.NoteVolume

private val StrongZoneColor = Color(0xFF2ECC71)
private val WeakZoneColor = Color(0xFFE05353)

/**
 * 音域(横軸: 最低音〜最高音)ごとの声量(縦軸)を棒グラフで表示する。
 * 緑 = 得意ゾーン、赤 = 苦手ゾーン、それ以外は通常色。
 */
@Composable
fun VolumeProfileChart(
    profile: List<NoteVolume>,
    lowestMidi: Int,
    highestMidi: Int,
    strongZone: IntRange?,
    weakZone: IntRange?,
    modifier: Modifier = Modifier
) {
    val normalColor = MaterialTheme.colorScheme.primary

    if (profile.isEmpty() || highestMidi <= lowestMidi) {
        Box(modifier = modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Text("声量データが不足しています", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val minDb = profile.minOf { it.averageDb }
    val maxDb = profile.maxOf { it.averageDb }
    val dbRange = (maxDb - minDb).let { if (it > 0.5) it else 1.0 }

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val totalNotes = (highestMidi - lowestMidi + 1).coerceAtLeast(1)
        val barSlotWidth = size.width / totalNotes

        profile.forEach { noteVolume ->
            val index = noteVolume.midi - lowestMidi
            if (index < 0 || index >= totalNotes) return@forEach

            val heightFraction = (((noteVolume.averageDb - minDb) / dbRange).toFloat()).coerceIn(0.05f, 1f)
            val barHeight = size.height * heightFraction

            val color = when {
                strongZone != null && noteVolume.midi in strongZone -> StrongZoneColor
                weakZone != null && noteVolume.midi in weakZone -> WeakZoneColor
                else -> normalColor
            }

            drawRect(
                color = color,
                topLeft = Offset(x = index * barSlotWidth, y = size.height - barHeight),
                size = Size(width = barSlotWidth * 0.75f, height = barHeight)
            )
        }
    }
}
