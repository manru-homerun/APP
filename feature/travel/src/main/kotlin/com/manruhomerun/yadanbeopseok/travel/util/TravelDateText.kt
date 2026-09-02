package com.manruhomerun.yadanbeopseok.travel.util

import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate

/**
 * 여행 기간을 헤더에 표시할 짧은 날짜 문구로 변환합니다.
 */
internal fun LocalDate.toTravelDateRangeText(endDate: LocalDate): String {
    val startText = toMonthDayText()

    return if (this == endDate) {
        startText
    } else {
        "$startText~${endDate.toMonthDayText()}"
    }
}

/**
 * 여행 시작일부터 해당 일차의 날짜와 요일을 계산합니다.
 */
internal fun LocalDate.toTravelDayDateText(day: Int): String {
    val dayOffset = (day - 1).coerceAtLeast(0)
    val date = plus(dayOffset, DateTimeUnit.DAY)

    return date.toJavaLocalDate().format(travelDayDateFormatter)
}

private fun LocalDate.toMonthDayText(): String = "${month.number}.$day"

private val travelDayDateFormatter =
    DateTimeFormatter.ofPattern("M.d (E)", Locale.KOREAN)
