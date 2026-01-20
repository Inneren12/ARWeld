package com.example.arweld.feature.supervisor.viewmodel

import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class ExportPeriodType {
    SHIFT,
    DAY,
}

enum class ShiftSelection {
    CURRENT,
    PREVIOUS,
}

data class ShiftLabels(
    val current: String,
    val previous: String,
)

object PeriodSelectionHelper {
    fun buildShiftLabels(nowMillis: Long, zoneId: ZoneId): ShiftLabels {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val shift = resolveCurrentShift(now)
        val currentLabel = "Current ${shift.name} · ${SHIFT_TIME_FORMAT.format(shift.start)}–" +
            SHIFT_TIME_FORMAT.format(shift.start.plusHours(SHIFT_DURATION_HOURS).minusMillis(1))
        val previousStart = shift.start.minusHours(SHIFT_DURATION_HOURS)
        val previousName = if (shift.name == SHIFT_A_NAME) SHIFT_B_NAME else SHIFT_A_NAME
        val previousLabel = "Previous ${previousName} · ${SHIFT_TIME_FORMAT.format(previousStart)}–" +
            SHIFT_TIME_FORMAT.format(previousStart.plusHours(SHIFT_DURATION_HOURS).minusMillis(1))
        return ShiftLabels(current = currentLabel, previous = previousLabel)
    }

    fun buildPeriod(
        periodType: ExportPeriodType,
        selectedDate: LocalDate,
        shiftSelection: ShiftSelection,
        nowMillis: Long,
        zoneId: ZoneId,
    ): ExportPeriod {
        return when (periodType) {
            ExportPeriodType.DAY -> buildDayPeriod(selectedDate, zoneId)
            ExportPeriodType.SHIFT -> buildShiftPeriod(nowMillis, shiftSelection, zoneId)
        }
    }

    private fun buildDayPeriod(date: LocalDate, zoneId: ZoneId): ExportPeriod {
        val start = date.atStartOfDay(zoneId).toInstant()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)
        val label = "Day · ${DAY_LABEL_FORMAT.format(date)}"
        return ExportPeriod(
            startMillis = start.toEpochMilli(),
            endMillis = end.toEpochMilli(),
            label = label,
        )
    }

    private fun buildShiftPeriod(nowMillis: Long, selection: ShiftSelection, zoneId: ZoneId): ExportPeriod {
        val nowZoned = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val shift = resolveCurrentShift(nowZoned)
        val shiftStart = when (selection) {
            ShiftSelection.CURRENT -> shift.start
            ShiftSelection.PREVIOUS -> shift.start.minusHours(SHIFT_DURATION_HOURS)
        }
        val shiftName = when (selection) {
            ShiftSelection.CURRENT -> shift.name
            ShiftSelection.PREVIOUS -> if (shift.name == SHIFT_A_NAME) SHIFT_B_NAME else SHIFT_A_NAME
        }
        val shiftEnd = shiftStart.plusHours(SHIFT_DURATION_HOURS).minusMillis(1)
        val label = "${shiftName} · ${SHIFT_TIME_FORMAT.format(shiftStart)}–${SHIFT_TIME_FORMAT.format(shiftEnd)}"
        return ExportPeriod(
            startMillis = shiftStart.toInstant().toEpochMilli(),
            endMillis = shiftEnd.toInstant().toEpochMilli(),
            label = label,
        )
    }

    private fun resolveCurrentShift(now: ZonedDateTime): ShiftWindow {
        val localTime = now.toLocalTime()
        return if (localTime >= SHIFT_A_START && localTime < SHIFT_A_END) {
            val start = now.toLocalDate().atTime(SHIFT_A_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_A_NAME, start = start)
        } else if (localTime >= SHIFT_B_START) {
            val start = now.toLocalDate().atTime(SHIFT_B_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_B_NAME, start = start)
        } else {
            val start = now.toLocalDate().minusDays(1).atTime(SHIFT_B_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_B_NAME, start = start)
        }
    }

    private data class ShiftWindow(
        val name: String,
        val start: ZonedDateTime,
    )

    private const val SHIFT_DURATION_HOURS = 12L
    private val SHIFT_A_START: LocalTime = LocalTime.of(6, 0)
    private val SHIFT_A_END: LocalTime = LocalTime.of(18, 0)
    private val SHIFT_B_START: LocalTime = LocalTime.of(18, 0)
    private const val SHIFT_A_NAME = "Shift A"
    private const val SHIFT_B_NAME = "Shift B"
    private val DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val SHIFT_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d HH:mm")
}
