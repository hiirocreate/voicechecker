package com.vocalrange.analyzer.core

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 周波数(Hz)と音名(例: "C4", "A#3")の相互変換ユーティリティ。
 * A4 = 440Hz を基準としたMIDIノート番号ベースの計算を行う。
 */
object NoteUtils {

    private val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private const val A4_MIDI = 69
    private const val A4_FREQUENCY = 440.0

    data class NoteInfo(
        val midiNote: Int,
        val noteName: String,
        val octave: Int,
        val frequencyHz: Double,
        val nearestNoteFrequencyHz: Double,
        val centsOffset: Double
    ) {
        /** "C4" のような表示用ラベル */
        val label: String get() = "$noteName$octave"
    }

    /** 周波数から小数MIDIノート番号(平均律基準)を計算 */
    fun frequencyToMidi(frequencyHz: Double): Double {
        require(frequencyHz > 0.0) { "frequencyHz must be positive" }
        return A4_MIDI + 12.0 * (ln(frequencyHz / A4_FREQUENCY) / ln(2.0))
    }

    /** MIDIノート番号(小数可)から周波数を計算 */
    fun midiToFrequency(midiNote: Double): Double {
        return A4_FREQUENCY * 2.0.pow((midiNote - A4_MIDI) / 12.0)
    }

    /** 整数MIDIノート番号から音名+オクターブを取得 (C4=60, A4=69 の科学的ピッチ表記) */
    fun midiToNoteName(midiNoteInt: Int): Pair<String, Int> {
        val name = NOTE_NAMES[((midiNoteInt % 12) + 12) % 12]
        val octave = midiNoteInt / 12 - 1
        return name to octave
    }

    /** 音名+オクターブ(例: "C", 4)からMIDIノート番号を取得 */
    fun noteNameToMidi(noteName: String, octave: Int): Int {
        val index = NOTE_NAMES.indexOf(noteName)
        require(index >= 0) { "Unknown note name: $noteName" }
        return (octave + 1) * 12 + index
    }

    /** 周波数から詳細な音情報を取得する */
    fun analyze(frequencyHz: Double): NoteInfo {
        val exactMidi = frequencyToMidi(frequencyHz)
        val nearestMidi = exactMidi.roundToInt()
        val (name, octave) = midiToNoteName(nearestMidi)
        val nearestFreq = midiToFrequency(nearestMidi.toDouble())
        val cents = (exactMidi - nearestMidi) * 100.0
        return NoteInfo(
            midiNote = nearestMidi,
            noteName = name,
            octave = octave,
            frequencyHz = frequencyHz,
            nearestNoteFrequencyHz = nearestFreq,
            centsOffset = cents
        )
    }

    /** 2つの周波数間の半音差(セント/100) */
    fun semitoneDifference(freqA: Double, freqB: Double): Double {
        return frequencyToMidi(freqA) - frequencyToMidi(freqB)
    }

    /** よく使う音名の周波数(参照用) */
    fun noteLabel(midiNoteInt: Int): String {
        val (name, octave) = midiToNoteName(midiNoteInt)
        return "$name$octave"
    }
}
