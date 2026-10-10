package com.manruhomerun.yadanbeopseok.travel.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.STICKER_REQUIRED_VERIFIED_SPOT_COUNT
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySection
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySelector
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelHeader
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPlaceItemMode
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelProgress
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

private const val TRAVEL_DAY_KEY_PREFIX = "travel_day_"
private const val TRAVEL_DAY_SELECTOR_KEY = "travel_day_selector"

/**
 * 여행 결과, 여행 상세와 일정 편집 화면에서 재사용하는 일정 본문입니다.
 *
 * 상단 앱 바, 로딩·오류 화면과 하단 작업 버튼은 각 Screen에서 관리합니다.
 * 이 컴포넌트는 여행 정보 헤더와 일차별 일정 표시를 담당합니다.
 *
 * [visibleTravelDays]에 전체 일차를 전달하면 모든 일정을 하나의 목록으로 표시할 수 있습니다.
 * 특정 일차만 전달하면 여행 중 화면처럼 선택한 일차만 표시할 수도 있습니다.
 *
 * [pinDaySelector]가 true이면 일차 선택기를 상단에 고정합니다.
 * 선택기에는 전달받은 선택 콜백을 연결하여 탭 클릭과 스크롤 갱신을 구분합니다.
 * [scrollToSelectedDay]가 true이면 탭 선택 시 해당 일차의 시작 위치로 이동합니다.
 * 고정하지 않는 기존 화면은 [selectedDay] 변경에 따른 이동 방식을 유지합니다.
 *
 * [onVisibleDayChanged]를 전달하면 사용자가 직접 스크롤했을 때
 * 현재 화면에 보이는 일차 번호를 전달합니다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TravelScheduleContent(
    title: String,
    dateText: String?,
    isLeader: Boolean,
    visibleTravelDays: List<TravelDay>,
    dateTextForDay: (Int) -> String,
    modifier: Modifier = Modifier,
    sectionTitle: String? = null,
    onRenameClick: (() -> Unit)? = null,
    progressContent: (@Composable () -> Unit)? = null,
    daySelectorContent: (@Composable (onDaySelected: (Int) -> Unit) -> Unit)? = null,
    selectedDay: Int? = null,
    scrollToSelectedDay: Boolean = false,
    onVisibleDayChanged: ((Int) -> Unit)? = null,
    pinDaySelector: Boolean = false,
    onDaySelected: ((Int) -> Unit)? = onVisibleDayChanged,
    placeItemMode: YadanTravelPlaceItemMode = YadanTravelPlaceItemMode.VIEW,
    onPlaceClick: ((TravelPlace) -> Unit)? = null,
    supportingText: (TravelPlace) -> String? = { null },
    isPlaceFixed: (TravelPlace) -> Boolean = { place ->
        place.spot.category == TravelSpotCategory.STADIUM
    },
    onVerifyClick: ((TravelPlace) -> Unit)? = null,
    onRemoveClick: ((day: Int, place: TravelPlace) -> Unit)? = null,
    dragHandleModifier: (day: Int, place: TravelPlace) -> Modifier = { _, _ ->
        Modifier
    },
    onAddPlaceClick: ((day: Int) -> Unit)? = null,
    enabled: Boolean = true,
    emptyMessage: String = "등록된 여행 일정이 없습니다",
) {
    val orderedTravelDays = remember(visibleTravelDays) {
        visibleTravelDays.sortedBy { travelDay ->
            travelDay.day
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var daySelectorHeightPx by remember { mutableIntStateOf(0) }
    var dayScrollJob by remember { mutableStateOf<Job?>(null) }
    var requestedDay by remember(visibleTravelDays) {
        mutableStateOf<Int?>(null)
    }
    var lastObservedDay by remember(visibleTravelDays) {
        mutableStateOf(selectedDay)
    }

    val currentSelectedDay by rememberUpdatedState(selectedDay)
    val currentOnDaySelected by rememberUpdatedState(onDaySelected)
    val currentOnVisibleDayChanged by rememberUpdatedState(onVisibleDayChanged)

    val daySectionStartIndex =
        1 +
            (if (progressContent != null) 1 else 0) +
            (if (daySelectorContent != null) 1 else 0) +
            (if (!sectionTitle.isNullOrBlank()) 1 else 0)

    /** 고정 탭의 클릭만 자동 이동으로 처리하며 상태 복원은 이동을 발생시키지 않습니다. */
    fun requestDay(day: Int) {
        if (!pinDaySelector || !scrollToSelectedDay) {
            currentOnDaySelected?.invoke(day)
            return
        }

        if (!enabled || day == currentSelectedDay) return
        val dayIndex = orderedTravelDays.indexOfFirst { it.day == day }
        if (dayIndex < 0) return

        dayScrollJob?.cancel()
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            val requestJob = currentCoroutineContext().job
            dayScrollJob = requestJob
            requestedDay = day

            try {
                currentOnDaySelected?.invoke(day)
                val selectorHeight = snapshotFlow { daySelectorHeightPx }.first { it > 0 }
                listState.animateScrollToItem(
                    index = daySectionStartIndex + dayIndex,
                    scrollOffset = -selectorHeight,
                )
            } finally {
                // 취소된 이전 이동이 새 이동의 선택 상태를 해제하지 않게 합니다.
                if (dayScrollJob === requestJob) {
                    dayScrollJob = null
                    requestedDay = null
                }
            }
        }
    }

    DisposableEffect(orderedTravelDays, daySectionStartIndex, pinDaySelector, scrollToSelectedDay) {
        onDispose { dayScrollJob?.cancel() }
    }

    /*
     * 탭 선택으로 selectedDay가 변경되면 해당 일차의 LazyColumn 항목으로 이동합니다.
     *
     * lastObservedDay와 비교하는 이유는 사용자가 직접 스크롤해서
     * selectedDay가 변경된 경우에는 같은 위치로 다시 애니메이션하지 않기 위해서입니다.
     */
    LaunchedEffect(
        selectedDay,
        scrollToSelectedDay,
        orderedTravelDays,
        daySectionStartIndex,
    ) {
        if (pinDaySelector || !scrollToSelectedDay || selectedDay == null) {
            return@LaunchedEffect
        }

        if (selectedDay == lastObservedDay) {
            return@LaunchedEffect
        }

        val dayIndex = orderedTravelDays.indexOfFirst { travelDay ->
            travelDay.day == selectedDay
        }

        if (dayIndex < 0) {
            return@LaunchedEffect
        }

        requestedDay = selectedDay

        try {
            listState.animateScrollToItem(
                index = daySectionStartIndex + dayIndex,
            )
        } finally {
            requestedDay = null
            lastObservedDay = selectedDay
        }
    }

    /*
     * LazyColumn에서 현재 화면에 보이는 일차 항목을 감지합니다.
     *
     * 프로그램에 의한 탭 이동 중에는 중간에 보이는 일차로 선택 상태가
     * 바뀌지 않도록 requestedDay가 null인 경우에만 콜백을 호출합니다.
     */
    if (onVisibleDayChanged != null) {
        LaunchedEffect(listState, orderedTravelDays, pinDaySelector) {
            if (pinDaySelector) {
                var wasUserScrolling = false

                snapshotFlow {
                    val visibleDay = if (!listState.canScrollForward && listState.canScrollBackward) {
                        orderedTravelDays.lastOrNull()?.day
                    } else {
                        listState.layoutInfo.visibleTravelDayBelowSelector()
                    }
                    Triple(requestedDay, listState.isScrollInProgress, visibleDay)
                }
                    .distinctUntilChanged()
                    .collect { (scrollingToDay, isScrolling, visibleDay) ->
                        // 최초 레이아웃·복귀·자동 이동 완료는 수동 스크롤로 취급하지 않습니다.
                        val shouldUpdateDay = scrollingToDay == null && (isScrolling || wasUserScrolling)
                        wasUserScrolling = scrollingToDay == null && isScrolling

                        if (shouldUpdateDay && visibleDay != null && visibleDay != currentSelectedDay) {
                            currentOnVisibleDayChanged?.invoke(visibleDay)
                        }
                    }
            } else {
                snapshotFlow {
                    requestedDay to listState.layoutInfo.visibleTravelDay()
                }
                    .distinctUntilChanged()
                    .collect { (scrollingToDay, visibleDay) ->
                        if (
                            scrollingToDay == null &&
                            visibleDay != null &&
                            visibleDay != currentSelectedDay
                        ) {
                            lastObservedDay = visibleDay
                            currentOnVisibleDayChanged?.invoke(visibleDay)
                        }
                    }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(key = "travel_header") {
            YadanTravelHeader(
                title = title,
                dateText = dateText,
                isLeader = isLeader,
                onRenameClick = onRenameClick,
                enabled = enabled,
            )
        }

        if (progressContent != null) {
            item(key = "travel_progress") {
                progressContent()
            }
        }

        if (daySelectorContent != null) {
            if (pinDaySelector) {
                stickyHeader(key = TRAVEL_DAY_SELECTOR_KEY) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YadanBackground)
                            .onSizeChanged { daySelectorHeightPx = it.height }
                            .padding(vertical = 8.dp),
                    ) {
                        daySelectorContent(::requestDay)
                    }
                }
            } else {
                item(key = TRAVEL_DAY_SELECTOR_KEY) {
                    daySelectorContent(::requestDay)
                }
            }
        }

        if (!sectionTitle.isNullOrBlank()) {
            item(key = "travel_schedule_header") {
                YadanSectionHeader(title = sectionTitle)
            }
        }

        if (orderedTravelDays.isEmpty()) {
            item(key = "empty_schedule") {
                Text(
                    text = emptyMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    style = YadanTypography.bodyMedium,
                    color = YadanTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            orderedTravelDays.forEach { travelDay ->
                item(key = travelDayKey(travelDay.day)) {
                    YadanTravelDaySection(
                        travelDay = travelDay,
                        dateText = dateTextForDay(travelDay.day),
                        onPlaceClick = onPlaceClick,
                        mode = placeItemMode,
                        supportingText = supportingText,
                        isPlaceFixed = isPlaceFixed,
                        onVerifyClick = onVerifyClick,
                        onRemoveClick = onRemoveClick?.let { callback ->
                            { place ->
                                callback(travelDay.day, place)
                            }
                        },
                        dragHandleModifier = { place ->
                            dragHandleModifier(travelDay.day, place)
                        },
                        onAddPlaceClick = onAddPlaceClick?.let { callback ->
                            {
                                callback(travelDay.day)
                            }
                        },
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

/**
 * 일차 번호로 LazyColumn 항목 키를 생성합니다.
 */
private fun travelDayKey(day: Int): String = "$TRAVEL_DAY_KEY_PREFIX$day"

/**
 * LazyColumn의 키에서 일차 번호를 읽습니다.
 *
 * 일차 항목이 아닌 키는 null을 반환합니다.
 */
private fun Any.toTravelDayNumber(): Int? {
    val key = this as? String ?: return null

    if (!key.startsWith(TRAVEL_DAY_KEY_PREFIX)) {
        return null
    }

    return key.removePrefix(TRAVEL_DAY_KEY_PREFIX).toIntOrNull()
}

/**
 * 현재 보이는 LazyColumn 항목 중 가장 먼저 보이는 일차를 반환합니다.
 */
private fun LazyListLayoutInfo.visibleTravelDay(): Int? =
    visibleItemsInfo
        .asSequence()
        .mapNotNull { item ->
            item.key.toTravelDayNumber()
        }
        .firstOrNull()

/** 고정 탭에 가려지지 않은 일정 중 가장 위에 보이는 일차를 반환합니다. */
private fun LazyListLayoutInfo.visibleTravelDayBelowSelector(): Int? {
    val selector = visibleItemsInfo.firstOrNull { it.key == TRAVEL_DAY_SELECTOR_KEY }
    val visibleTop = maxOf(viewportStartOffset, selector?.let { it.offset + it.size } ?: viewportStartOffset)

    return visibleItemsInfo
        .asSequence()
        .filter { it.offset + it.size > visibleTop && it.offset < viewportEndOffset }
        .mapNotNull { it.key.toTravelDayNumber() }
        .firstOrNull()
}

@Preview(
    name = "공통 일정 본문 - 전체 일정",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelScheduleContentPreview() {
    YadanbeopseokTheme {
        TravelScheduleContent(
            title = "부산 사직 직관 여행",
            dateText = "5.22~5.23",
            isLeader = true,
            visibleTravelDays = previewTravelDays(),
            dateTextForDay = { day ->
                when (day) {
                    1 -> "5.22 (금)"
                    2 -> "5.23 (토)"
                    else -> ""
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(YadanBackground),
            sectionTitle = "방문 순서",
            onRenameClick = {},
        )
    }
}

@Preview(
    name = "공통 일정 본문 - 일차 스크롤",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelScheduleDayScrollPreview() {
    val travelDays = previewTravelDays()
    var selectedDay by remember {
        mutableIntStateOf(1)
    }

    YadanbeopseokTheme {
        TravelScheduleContent(
            title = "부산 사직 직관 여행",
            dateText = "5.22~5.23",
            isLeader = false,
            visibleTravelDays = travelDays,
            dateTextForDay = { day ->
                when (day) {
                    1 -> "5.22 (금)"
                    2 -> "5.23 (토)"
                    else -> ""
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(YadanBackground),
            daySelectorContent = { onDaySelected ->
                YadanTravelDaySelector(
                    dayNumbers = travelDays.map { travelDay ->
                        travelDay.day
                    },
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            selectedDay = selectedDay,
            scrollToSelectedDay = true,
            onVisibleDayChanged = { day ->
                selectedDay = day
            },
        )
    }
}

@Preview(
    name = "공통 일정 본문 - 여행 중",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun ActiveTravelScheduleContentPreview() {
    val travelDays = previewTravelDays()
    var selectedDay by remember {
        mutableIntStateOf(1)
    }

    YadanbeopseokTheme {
        TravelScheduleContent(
            title = "부산 사직 직관 여행",
            dateText = "5.22~5.23",
            isLeader = true,
            visibleTravelDays = travelDays.filter { travelDay ->
                travelDay.day == selectedDay
            },
            dateTextForDay = { day ->
                when (day) {
                    1 -> "5.22 (금)"
                    2 -> "5.23 (토)"
                    else -> ""
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(YadanBackground),
            progressContent = {
                YadanTravelProgress(
                    verifiedPlaceCount = 1,
                    totalPlaceCount = STICKER_REQUIRED_VERIFIED_SPOT_COUNT,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            daySelectorContent = { onDaySelected ->
                YadanTravelDaySelector(
                    dayNumbers = travelDays.map { travelDay ->
                        travelDay.day
                    },
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            placeItemMode = YadanTravelPlaceItemMode.ACTIVE,
            onDaySelected = { selectedDay = it },
            onVerifyClick = {},
        )
    }
}

private fun previewTravelDays(): List<TravelDay> =
    listOf(
        TravelDay(
            day = 1,
            places = listOf(
                previewTravelPlace(
                    id = "spot-1",
                    name = "감천문화마을",
                    category = TravelSpotCategory.CULTURE,
                    order = 1,
                    isVerified = true,
                ),
                previewTravelPlace(
                    id = "spot-2",
                    name = "광안리 해수욕장",
                    category = TravelSpotCategory.NATURE,
                    order = 2,
                ),
            ),
        ),
        TravelDay(
            day = 2,
            places = listOf(
                previewTravelPlace(
                    id = "spot-3",
                    name = "해운대 오션뷰 호텔",
                    category = TravelSpotCategory.ACCOMMODATION,
                    order = 1,
                ),
                previewTravelPlace(
                    id = "spot-4",
                    name = "부평깡통시장",
                    category = TravelSpotCategory.FOOD,
                    order = 2,
                ),
            ),
        ),
    )

private fun previewTravelPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
    isVerified: Boolean = false,
): TravelPlace =
    TravelPlace(
        spot = TravelSpot(
            id = id,
            name = name,
            category = category,
        ),
        order = order,
        isVerified = isVerified,
    )
