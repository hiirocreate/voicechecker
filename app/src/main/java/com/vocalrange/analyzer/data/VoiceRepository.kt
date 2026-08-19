package com.vocalrange.analyzer.data

import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.core.VibratoSessionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room DBへのアクセスを一箇所にまとめるリポジトリ */
class VoiceRepository(private val database: AppDatabase) {

    fun observeRangeSessions(): Flow<List<RangeSessionSummary>> =
        database.rangeSessionDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun saveRangeSession(summary: RangeSessionSummary): Long =
        database.rangeSessionDao().insert(summary.toEntity())

    suspend fun getRangeSession(id: Long): RangeSessionSummary? =
        database.rangeSessionDao().getById(id)?.toDomain()

    suspend fun deleteRangeSession(summary: RangeSessionSummary) =
        database.rangeSessionDao().delete(summary.toEntity())

    fun observeVibratoSessions(): Flow<List<VibratoSessionSummary>> =
        database.vibratoSessionDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun saveVibratoSession(summary: VibratoSessionSummary): Long =
        database.vibratoSessionDao().insert(summary.toEntity())

    suspend fun getVibratoSession(id: Long): VibratoSessionSummary? =
        database.vibratoSessionDao().getById(id)?.toDomain()

    suspend fun deleteVibratoSession(summary: VibratoSessionSummary) =
        database.vibratoSessionDao().delete(summary.toEntity())
}
