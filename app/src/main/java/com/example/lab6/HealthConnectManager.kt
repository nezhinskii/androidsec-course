package com.example.lab6

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectManager(private val context: Context) {

    val healthClient = HealthConnectClient.getOrCreate(context.applicationContext)

    companion object {
        fun create(context: Context) = HealthConnectManager(context.applicationContext)
    }

    suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord> =
        withContext(Dispatchers.IO) {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
            healthClient.readRecords(request).records
                .sortedByDescending { it.startTime }
        }

    suspend fun getTotalSteps(start: Instant, end: Instant): Long =
        withContext(Dispatchers.IO) {
            val result = healthClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            result[StepsRecord.COUNT_TOTAL] ?: 0L
        }

    suspend fun insertSteps(
        count: Long,
        startTime: Instant,
        endTime: Instant
    ) = withContext(Dispatchers.IO) {
        val record = StepsRecord(
            count = count,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime),
            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime),
            metadata = Metadata.manualEntry(),
        )
        healthClient.insertRecords(listOf(record))
    }

    suspend fun updateSteps(
        id: String,
        count: Long,
        startTime: Instant,
        endTime: Instant
    ) = withContext(Dispatchers.IO) {
        deleteStepsById(id)
        insertSteps(count, startTime, endTime)
    }

    suspend fun deleteStepsById(id: String) = withContext(Dispatchers.IO) {
        healthClient.deleteRecords(
            recordType = StepsRecord::class,
            recordIdsList = listOf(id),
            clientRecordIdsList = emptyList()
        )
    }
}