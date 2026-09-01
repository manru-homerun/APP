package com.manruhomerun.yadanbeopseok.travel.course.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditUiState
import com.manruhomerun.yadanbeopseok.travel.util.TravelCourseTimelineItem
import com.manruhomerun.yadanbeopseok.travel.util.toSchedulePlace
import com.manruhomerun.yadanbeopseok.travel.util.toTimelineItems
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDateRangeText
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDayDateText
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDayAddPlaceRow
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDayHeader
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelDaySelector
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelHeader
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPlaceItem
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPlaceItemMode
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * C01 여행 일정 편집 화면입니다.
 *
 * 모든 날짜의 일정을 하나의 목록으로 표시합니다.
 * 관광지는 같은 날짜 안에서 또는 다른 날짜로 이동할 수 있습니다.
 * 야구 경기는 직접 이동하거나 삭제할 수 없습니다.
 *
 * 데이터 변경과 화면 이동은 콜백으로 전달합니다.
 * onMovePlace는 ViewModel.moveTravelSpot처럼 상태를 동기적으로 갱신해야 합니다.
 */
@Composable
fun TravelCourseEditScreen(
    uiState: TravelCourseEditUiState,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    onAddPlaceClick: (day: Int) -> Unit,
    onRemovePlaceClick: (day: Int, travelSpotId: String) -> Unit,
    onMovePlace: (travelSpotId: String, targetDay: Int, targetIndex: Int) -> Unit,
    onAlignClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingKey by remember { mutableStateOf<String?>(null) }
    val blocksNavigation = uiState.isRequestInProgress || draggingKey != null

    if (!LocalInspectionMode.current) {
        BackHandler {
            if (!blocksNavigation) {
                onBackClick()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "여행 일정",
            onNavigationClick = {
                if (!blocksNavigation) {
                    onBackClick()
                }
            },
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = YadanPrimary)
                }
            }

            !uiState.hasContent -> {
                CourseEditErrorContent(
                    message = uiState.errorMessage ?: "여행 일정을 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            else -> {
                CourseEditContent(
                    uiState = uiState,
                    draggingKey = draggingKey,
                    onDraggingKeyChange = { draggingKey = it },
                    onRenameClick = onRenameClick,
                    onAddPlaceClick = onAddPlaceClick,
                    onRemovePlaceClick = onRemovePlaceClick,
                    onMovePlace = onMovePlace,
                    onAlignClick = onAlignClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        style = YadanTypography.bodyMedium,
                        color = YadanError,
                    )
                }

                YadanButton(
                    text = "일정 저장",
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    enabled = (uiState.canSave || uiState.isSaving) && draggingKey == null,
                    isLoading = uiState.isSaving,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}

/**
 * 날짜 제목, 관광지, 경기, 추가 버튼을 각각 LazyColumn 항목으로 배치합니다.
 * 일차 전체를 하나의 항목으로 묶지 않아 날짜 사이에서도 드래그할 수 있습니다.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CourseEditContent(
    uiState: TravelCourseEditUiState,
    draggingKey: String?,
    onDraggingKeyChange: (String?) -> Unit,
    onRenameClick: () -> Unit,
    onAddPlaceClick: (day: Int) -> Unit,
    onRemovePlaceClick: (day: Int, travelSpotId: String) -> Unit,
    onMovePlace: (travelSpotId: String, targetDay: Int, targetIndex: Int) -> Unit,
    onAlignClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val course = uiState.course ?: return
    val game = uiState.baseballGame ?: return
    val startDate = uiState.startDate ?: return
    val endDate = uiState.endDate ?: return

    val rows = remember(course, game) { course.toEditRows(game) }
    val dayNumbers = remember(course) { course.days.map { it.day }.sorted() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var tabsHeightPx by remember { mutableIntStateOf(0) }
    var requestedDay by remember { mutableStateOf<Int?>(null) }

    val tabsKey = "course_day_tabs"
    val canEdit = !uiState.isLoading && !uiState.isRequestInProgress
    val controlsEnabled = canEdit && draggingKey == null && requestedDay == null
    val scrollPadding = with(density) { tabsHeightPx.toDp() }

    val gameTimeText = remember(game.gameDateTime) {
        val time = game.gameDateTime.toJavaLocalDateTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        "$time 경기 시작"
    }

    val selectedDay by remember(rows, dayNumbers, listState) {
        derivedStateOf {
            val scrollingToDay = requestedDay

            when {
                scrollingToDay != null -> scrollingToDay

                !listState.canScrollForward && listState.canScrollBackward -> {
                    dayNumbers.lastOrNull() ?: 1
                }

                else -> {
                    val layout = listState.layoutInfo
                    val tabs = layout.visibleItemsInfo.firstOrNull { it.key == tabsKey }
                    val visibleTop = maxOf(
                        layout.viewportStartOffset,
                        tabs?.let { it.offset + it.size } ?: layout.viewportStartOffset,
                    )

                    val firstVisibleRow = layout.visibleItemsInfo
                        .asSequence()
                        .filter { it.offset + it.size > visibleTop }
                        .mapNotNull { item -> rows.firstOrNull { it.key == item.key } }
                        .firstOrNull()

                    firstVisibleRow?.day ?: dayNumbers.firstOrNull() ?: 1
                }
            }
        }
    }

    val reorderState = rememberReorderableLazyListState(
        lazyListState = listState,
        scrollThresholdPadding = PaddingValues(top = scrollPadding),
    ) { from, to ->
        if (canEdit) {
            val move = rows.resolveMove(from.key, to.key)

            if (move != null) {
                onMovePlace(move.spotId, move.day, move.index)
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
            bottom = 20.dp,
        ),
    ) {
        item(key = "course_header") {
            YadanTravelHeader(
                title = uiState.travelName,
                dateText = startDate.toTravelDateRangeText(endDate),
                isLeader = true,
                onRenameClick = onRenameClick,
                enabled = controlsEnabled,
            )
        }

        stickyHeader(key = tabsKey) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .onSizeChanged { tabsHeightPx = it.height }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                YadanTravelDaySelector(
                    dayNumbers = dayNumbers,
                    selectedDay = selectedDay,
                    onDaySelected = { day ->
                        val rowIndex = rows.indexOfFirst {
                            it is CourseEditRow.Header && it.day == day
                        }

                        if (controlsEnabled && requestedDay == null && rowIndex >= 0) {
                            requestedDay = day

                            scope.launch {
                                try {
                                    // 앞의 여행 헤더와 고정 날짜 탭 두 항목을 포함합니다.
                                    listState.animateScrollToItem(
                                        index = rowIndex + 2,
                                        scrollOffset = -tabsHeightPx,
                                    )
                                } finally {
                                    requestedDay = null
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = controlsEnabled,
                )

                TextButton(
                    onClick = onAlignClick,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = controlsEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = YadanPrimaryInk,
                    ),
                ) {
                    if (uiState.isAligning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = YadanPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "거리기반 재정렬",
                        style = YadanTypography.labelMedium,
                    )
                }
            }
        }

        items(items = rows, key = { it.key }) { row ->
            val acceptsDrop = canEdit && (
                draggingKey == null ||
                    draggingKey == row.key ||
                    rows.resolveMove(draggingKey, row.key) != null
                )

            ReorderableItem(
                state = reorderState,
                key = row.key,
                modifier = Modifier.fillMaxWidth(),
                enabled = acceptsDrop,
            ) { isDragging ->
                when (row) {
                    is CourseEditRow.Header -> {
                        Box(
                            modifier = Modifier.padding(
                                top = if (row.day == dayNumbers.firstOrNull()) 8.dp else 20.dp,
                            ),
                        ) {
                            YadanTravelDayHeader(
                                day = row.day,
                                dateText = startDate.toTravelDayDateText(row.day),
                                placeCount = row.placeCount,
                            )
                        }
                    }

                    is CourseEditRow.Stop -> {
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.025f else 1f,
                            label = "coursePlaceScale",
                        )

                        val moveActions = if (controlsEnabled && !row.isGame) {
                            rows.moveActions(row, onMovePlace)
                        } else {
                            emptyList()
                        }

                        val handleModifier = if (row.isGame) {
                            Modifier
                        } else {
                            Modifier
                                .longPressDraggableHandle(
                                    enabled = canEdit && requestedDay == null,
                                    onDragStarted = { onDraggingKeyChange(row.key) },
                                    onDragStopped = { onDraggingKeyChange(null) },
                                )
                                .semantics {
                                    contentDescription = "${row.place.spot.name} 순서 변경"
                                    customActions = moveActions
                                }
                        }

                        YadanTravelPlaceItem(
                            place = row.place,
                            isLast = false,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                            mode = YadanTravelPlaceItemMode.EDIT,
                            displayOrder = row.displayOrder,
                            supportingText = if (row.isGame) gameTimeText else null,
                            isFixed = row.isGame,
                            onRemoveClick = if (row.isGame) {
                                null
                            } else {
                                { onRemovePlaceClick(row.day, row.place.spot.id) }
                            },
                            dragHandleModifier = handleModifier,
                            enabled = controlsEnabled,
                        )
                    }

                    is CourseEditRow.Add -> {
                        YadanTravelDayAddPlaceRow(
                            day = row.day,
                            onClick = { onAddPlaceClick(row.day) },
                            enabled = controlsEnabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseEditErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = message,
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
        )
    }
}

/**
 * 편집 목록의 화면 전용 항목입니다.
 * 관광지 키에는 날짜와 순서를 넣지 않아 날짜 간 이동 중에도 동일 항목으로 유지합니다.
 */
private sealed interface CourseEditRow {
    val key: String
    val day: Int

    data class Header(
        override val day: Int,
        val placeCount: Int,
    ) : CourseEditRow {
        override val key: String = "day:$day"
    }

    data class Stop(
        override val key: String,
        override val day: Int,
        val place: TravelPlace,
        val timelineIndex: Int,
        val displayOrder: Int,
        val isGame: Boolean,
    ) : CourseEditRow

    data class Add(
        override val day: Int,
        val timelineSize: Int,
    ) : CourseEditRow {
        override val key: String = "add:$day"
    }
}

/**
 * 실제 경기 정보를 공통 일정 카드에 표시하고, 경기 배치 인덱스에 삽입합니다.
 * 경기 표시용 TravelPlace는 화면에서만 사용하며 course.days에는 추가하지 않습니다.
 */
private fun TravelCourse.toEditRows(game: BaseballGame): List<CourseEditRow> = buildList {

    val gamePlace = game.toSchedulePlace()

    days.sortedBy { it.day }.forEach { travelDay ->
        val timelineItems = toTimelineItems(travelDay.day) ?: return@forEach
        val timeline = timelineItems.map { item ->
            when (item) {
                is TravelCourseTimelineItem.Place -> item.place to false
                TravelCourseTimelineItem.BaseballGame -> gamePlace to true
            }
        }

        val placeCount = timeline.count { (place, _) ->
            place.spot.category != TravelSpotCategory.ACCOMMODATION
        }

        add(CourseEditRow.Header(travelDay.day, placeCount))

        var displayOrder = 0

        timeline.forEachIndexed { index, (place, isGame) ->
            if (place.spot.category != TravelSpotCategory.ACCOMMODATION) {
                displayOrder += 1
            }

            add(
                CourseEditRow.Stop(
                    key = if (isGame) "game:${game.id}" else "spot:${place.spot.id}",
                    day = travelDay.day,
                    place = place,
                    timelineIndex = index,
                    displayOrder = displayOrder,
                    isGame = isGame,
                ),
            )
        }

        add(CourseEditRow.Add(travelDay.day, timeline.size))
    }
}

private data class CourseEditMove(
    val spotId: String,
    val day: Int,
    val index: Int,
)

/**
 * 드래그 대상 행을 ViewModel이 사용하는 타임라인 삽입 위치로 변환합니다.
 *
 * 같은 날짜에서는 원본 항목을 제거한 뒤의 인덱스를 전달합니다.
 * 다른 날짜의 제목은 맨 앞, 추가 버튼은 맨 뒤 삽입 위치로 처리합니다.
 */
private fun List<CourseEditRow>.resolveMove(
    fromKey: Any?,
    toKey: Any?,
): CourseEditMove? {
    val sourcePosition = indexOfFirst { it.key == fromKey }
    val targetPosition = indexOfFirst { it.key == toKey }

    if (sourcePosition < 0 || targetPosition < 0 || sourcePosition == targetPosition) {
        return null
    }

    val source = this[sourcePosition] as? CourseEditRow.Stop ?: return null
    if (source.isGame) return null

    val target = this[targetPosition]
    val targetEnd = filterIsInstance<CourseEditRow.Add>()
        .firstOrNull { it.day == target.day } ?: return null

    val sameDay = source.day == target.day
    val lastInsertionIndex = targetEnd.timelineSize - if (sameDay) 1 else 0

    val targetIndex = when (target) {
        is CourseEditRow.Header -> 0
        is CourseEditRow.Add -> lastInsertionIndex
        is CourseEditRow.Stop -> {
            val insertAfter = !sameDay && sourcePosition < targetPosition
            target.timelineIndex + if (insertAfter) 1 else 0
        }
    }

    if (targetIndex !in 0..lastInsertionIndex) return null
    if (sameDay && targetIndex == source.timelineIndex) return null

    return CourseEditMove(
        spotId = source.place.spot.id,
        day = target.day,
        index = targetIndex,
    )
}

/** 드래그를 사용하기 어려운 경우 접근성 메뉴에서 순서와 날짜를 변경합니다. */
private fun List<CourseEditRow>.moveActions(
    row: CourseEditRow.Stop,
    onMovePlace: (travelSpotId: String, targetDay: Int, targetIndex: Int) -> Unit,
): List<CustomAccessibilityAction> {
    val dayEnds = filterIsInstance<CourseEditRow.Add>()
    val currentDayEnd = dayEnds.firstOrNull { it.day == row.day } ?: return emptyList()

    return buildList {
        if (row.timelineIndex > 0) {
            add(
                CustomAccessibilityAction("앞으로 이동") {
                    onMovePlace(row.place.spot.id, row.day, row.timelineIndex - 1)
                    true
                },
            )
        }

        if (row.timelineIndex < currentDayEnd.timelineSize - 1) {
            add(
                CustomAccessibilityAction("뒤로 이동") {
                    onMovePlace(row.place.spot.id, row.day, row.timelineIndex + 1)
                    true
                },
            )
        }

        dayEnds.filter { it.day != row.day }.forEach { target ->
            add(
                CustomAccessibilityAction("DAY ${target.day}로 이동") {
                    onMovePlace(row.place.spot.id, target.day, target.timelineSize)
                    true
                },
            )
        }
    }
}

@Preview(name = "C01 - 편집", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditPreview() {
    CourseEditPreviewContent(previewCourseEditState())
}

@Preview(name = "C01 - 빈 날짜", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditEmptyDayPreview() {
    CourseEditPreviewContent(previewCourseEditState(emptySecondDay = true))
}

@Preview(name = "C01 - 불러오는 중", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditLoadingPreview() {
    CourseEditPreviewContent(TravelCourseEditUiState(isLoading = true))
}

@Preview(name = "C01 - 조회 실패", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditErrorPreview() {
    CourseEditPreviewContent(
        TravelCourseEditUiState(
            errorMessage = "여행 일정을 불러오지 못했습니다.",
        ),
    )
}

@Preview(name = "C01 - 재정렬 중", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditAligningPreview() {
    CourseEditPreviewContent(previewCourseEditState().copy(isAligning = true))
}

@Preview(name = "C01 - 저장 중", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditSavingPreview() {
    CourseEditPreviewContent(previewCourseEditState().copy(isSaving = true))
}

@Preview(name = "C01 - 저장 실패", widthDp = 390, heightDp = 844)
@Composable
private fun TravelCourseEditSaveErrorPreview() {
    CourseEditPreviewContent(
        previewCourseEditState().copy(
            errorMessage = "일정을 저장하지 못했습니다. 다시 시도해 주세요.",
        ),
    )
}

/** Preview에서는 네트워크나 ViewModel 없이 화면 상태만 표시합니다. */
@Composable
private fun CourseEditPreviewContent(uiState: TravelCourseEditUiState) {
    YadanbeopseokTheme {
        TravelCourseEditScreen(
            uiState = uiState,
            onBackClick = {},
            onRenameClick = {},
            onAddPlaceClick = {},
            onRemovePlaceClick = { _, _ -> },
            onMovePlace = { _, _, _ -> },
            onAlignClick = {},
            onSaveClick = {},
            onRetryClick = {},
        )
    }
}

private fun previewCourseEditState(
    emptySecondDay: Boolean = false,
): TravelCourseEditUiState {
    val game = BaseballGame(
        id = "123",
        stadium = BaseballStadium(
            id = "preview-stadium",
            name = "사직야구장",
            region = Region.BUSAN,
            latitude = 35.194,
            longitude = 129.061,
        ),
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(2026, 5, 22, 17, 0),
        gameType = BaseballGameType.REGULAR,
    )

    val secondDayPlaces = if (emptySecondDay) {
        emptyList()
    } else {
        listOf(
            previewCoursePlace("4", "해운대 해수욕장", TravelSpotCategory.NATURE, 1),
            previewCoursePlace("5", "자갈치시장", TravelSpotCategory.SHOPPING, 2),
        )
    }

    return TravelCourseEditUiState(
        travelName = "부산 사직 직관 여행",
        startDate = LocalDate(2026, 5, 22),
        endDate = LocalDate(2026, 5, 23),
        companionCount = 1,
        baseballGame = game,
        course = TravelCourse(
            baseballGame = TravelBaseballGame(
                id = game.id,
                day = 1,
                baseballGameAfterIdx = 1,
            ),
            days = listOf(
                TravelDay(
                    day = 1,
                    places = listOf(
                        previewCoursePlace("1", "돼지국밥 거리", TravelSpotCategory.FOOD, 1),
                        previewCoursePlace("2", "감천문화마을", TravelSpotCategory.CULTURE, 2),
                        previewCoursePlace("3", "스테이 광안", TravelSpotCategory.ACCOMMODATION, 3),
                    ),
                ),
                TravelDay(day = 2, places = secondDayPlaces),
            ),
        ),
    )
}

private fun previewCoursePlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
): TravelPlace {
    return TravelPlace(
        spot = TravelSpot(
            id = id,
            name = name,
            region = Region.BUSAN,
            category = category,
        ),
        order = order,
    )
}
