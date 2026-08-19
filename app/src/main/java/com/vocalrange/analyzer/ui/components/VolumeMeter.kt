package com.vocalrange.analyzer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 相対的な音量(dBFS相当)を表示するシンプルなメーター。
 * -60dB(ほぼ無音)〜0dB(フルスケール)を 0.0〜1.0 にマッピングして表示する。
 * 端末マイクの感度差があるため、絶対的な音圧(dB SPL)ではなく相対的な強弱の目安として扱うこと。
 */
@Composable
fun VolumeMeter(
    volumeDb: Double,
    modifier: Modifier = Modifier,
    label: String = "声量(相対値)"
) {
    val minDb = -60.0
    val maxDb = 0.0
    val fraction = ((volumeDb - minDb) / (maxDb - minDb)).toFloat().coerceIn(0f, 1f)
    // 値自体は ViewModel 側の VolumeSmoother で平滑化済みだが、表示の滑らかさをさらに上げるため
    // ピクセル単位の遷移もアニメーションで補間する
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 120),
        label = "volumeMeterFraction"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )
    }
}
