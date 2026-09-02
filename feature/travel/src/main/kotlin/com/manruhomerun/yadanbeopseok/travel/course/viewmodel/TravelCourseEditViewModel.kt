package com.manruhomerun.yadanbeopseok.travel.course.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.data.repository.CreateTravelParams
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.travel.util.TravelCourseTimelineItem
import com.manruhomerun.yadanbeopseok.travel.util.toTimelineItems
import com.manruhomerun.yadanbeopseok.travel.util.toTravelErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotQueryStateHolder
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotSelectionTab
import com.manruhomerun.yadanbeopseok.travel.util.toggleTravelSpotSelection

/**
 * 여행 일정 편집 과정에서 발생하는 일회성 이벤트입니다.
 *
 * 실제 화면 이동은 Route에서 이벤트를 받아 처리합니다.
 */
sealed interface TravelCourseEditEvent {
    /** 신규 여행 저장 또는 기존 여행 수정이 완료됐습니다. */
    data object Saved : TravelCourseEditEvent

    /** 인증 정보가 만료되어 로그인 화면으로 이동해야 합니다. */
    data object SessionExpired : TravelCourseEditEvent
}

/**
 * C01 여행 일정 편집 상태와 서버 요청을 관리합니다.
 *
 * B07에서 전달받은 저장 전 코스와 기존 여행 상세 조회 결과를 모두
 * 동일한 편집 상태로 관리합니다.
 */
@HiltViewModel
class TravelCourseEditViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository,
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelCourseEditUiState())
    val uiState: StateFlow<TravelCourseEditUiState> = _uiState.asStateFlow()

    private val _spotSelectionUiState = MutableStateFlow(TravelCourseSpotSelectionUiState())
    val spotSelectionUiState: StateFlow<TravelCourseSpotSelectionUiState> =
        _spotSelectionUiState.asStateFlow()

    private val _events = Channel<TravelCourseEditEvent>(Channel.BUFFERED)
    val events: Flow<TravelCourseEditEvent> = _events.receiveAsFlow()

    private var newTravelParams: CreateTravelParams? = null
    private var currentTravelId: String? = null

    private var loadJob: Job? = null
    private var alignJob: Job? = null
    private var saveJob: Job? = null

    private val spotQuery = TravelSpotQueryStateHolder(
        repository = travelSpotRepository,
        scope = viewModelScope,
        onSessionExpired = { _events.send(TravelCourseEditEvent.SessionExpired) },
    )

    init {
        viewModelScope.launch {
            // 대상 일차와 임시 선택은 유지하고 공통 조회 결과만 화면 상태에 반영합니다.
            spotQuery.uiState.collect { queryState ->
                _spotSelectionUiState.update {
                    if (it.isActive) it.copy(selectionState = queryState) else it
                }
            }
        }
    }

    /**
     * B07에서 전달받은 저장 전 여행 정보를 C01 편집 상태로 초기화합니다.
     *
     * 날짜, 동행자, 경기와 코스 정보를 화면 상태에 저장하고,
     * 지역과 테마 정보는 신규 여행 저장을 위해 내부에 유지합니다.
     */
    fun initializeNewTravel(params: CreateTravelParams, baseballGame: BaseballGame) {
        val isAlreadyInitialized =
            currentTravelId == null &&
                newTravelParams == params &&
                _uiState.value.baseballGame == baseballGame

        if (isAlreadyInitialized) return

        cancelRequests()
        resetTravelSpotSelection()

        currentTravelId = null
        newTravelParams = params

        _uiState.value = TravelCourseEditUiState(
            travelName = params.name,
            startDate = params.startDate,
            endDate = params.endDate,
            companionCount = params.friendNicknames.size,
            baseballGame = baseballGame,
            course = params.course,
        )
    }

    /**
     * 저장된 여행 ID로 상세 일정을 조회해 C01 편집 상태로 초기화합니다.
     *
     * 방장이면서 여행 시작 전인 경우에만 편집을 허용합니다.
     */
    fun initializeExistingTravel(travelId: String) {
        val normalizedTravelId = travelId.trim()

        if (normalizedTravelId.isEmpty()) {
            cancelRequests()
            resetTravelSpotSelection()

            currentTravelId = null
            newTravelParams = null

            _uiState.value = TravelCourseEditUiState(
                errorMessage = "수정할 여행 정보를 확인할 수 없습니다.",
            )
            return
        }

        val isAlreadyInitialized =
            currentTravelId == normalizedTravelId &&
                (_uiState.value.course != null || _uiState.value.isLoading)

        if (isAlreadyInitialized) return

        cancelRequests()
        resetTravelSpotSelection()

        currentTravelId = normalizedTravelId
        newTravelParams = null

        loadExistingTravel(normalizedTravelId)
    }

    /** 기존 여행 상세 조회를 다시 시도합니다. */
    fun retry() {
        if (_uiState.value.isLoading) return

        val travelId = currentTravelId ?: return
        loadExistingTravel(travelId)
    }

    /**
     * C01에서 선택한 일차의 관광지 추가 화면을 엽니다.
     *
     * 실제 일정에 존재하는 일차만 선택할 수 있습니다.
     *
     * 일정에 존재하는 일차를 대상으로 관광지 추가 상태를 초기화합니다.
     * */
    fun openTravelSpotSelection(day: Int) {
        if (!_uiState.value.canEditContent) return

        val course = _uiState.value.course ?: return
        if (course.days.none { it.day == day }) return

        val region = _uiState.value.baseballGame?.stadium?.region ?: return

        resetTravelSpotSelection()
        _spotSelectionUiState.value = TravelCourseSpotSelectionUiState(targetDay = day)
        spotQuery.initializeTravelSpotSelection(region)
    }

    /**
     * C01b/C01c에서 관광지의 임시 선택 상태를 변경합니다.
     *
     * 이미 여행 일정에 포함된 관광지는 다시 선택하지 않습니다.
     */
    fun toggleTravelSpotSelection(travelSpot: TravelSpot) {
        val currentState = _spotSelectionUiState.value
        if (!_uiState.value.canEditContent || !currentState.isActive) return
        if (spotQuery.uiState.value.isLoading) return

        val course = _uiState.value.course ?: return
        val isAlreadyInCourse = course.days.any { day ->
            day.places.any { it.spot.id == travelSpot.id }
        }
        if (isAlreadyInCourse) return

        _spotSelectionUiState.update {
            val selectedSpots = it.selectedTravelSpots.toggleTravelSpotSelection(travelSpot)
            it.copy(selectedTravelSpots = selectedSpots)
        }
    }

    /**
     * 임시로 선택한 관광지를 대상 일차에 추가하고 C01로 돌아갑니다.
     */
    fun confirmTravelSpotSelection() {
        val currentState = _spotSelectionUiState.value
        val targetDay = currentState.targetDay ?: return

        if (!_uiState.value.canEditContent || spotQuery.uiState.value.isLoading) return

        addTravelSpots(
            day = targetDay,
            travelSpots = currentState.selectedTravelSpots,
        )
        resetTravelSpotSelection()
    }

    /**
     * 관광지를 일정에 반영하지 않고 C01b/C01c 선택 화면을 닫습니다.
     */
    fun closeTravelSpotSelection() {
        resetTravelSpotSelection()
    }

    /** C01b/C01c의 조회 동작을 공통 조회기에 위임합니다. */
    fun selectTravelSpotTab(tab: TravelSpotSelectionTab) = spotQuery.selectTravelSpotTab(tab)
    fun updateTravelSpotSearchQuery(query: String) = spotQuery.updateTravelSpotSearchQuery(query)
    fun searchTravelSpots() = spotQuery.searchTravelSpots()
    fun clearTravelSpotSearch() = spotQuery.clearTravelSpotSearch()
    fun selectTravelSpotCategory(category: TravelSpotCategory?) = spotQuery.selectTravelSpotCategory(category)
    /** 관광지 상세에서 C01b/C01c로 돌아오면 현재 목록을 갱신합니다. */
    fun refreshTravelSpotSelection() = spotQuery.refreshTravelSpotSelection()
    fun retryTravelSpotSelection() = spotQuery.retryTravelSpotSelection()

    /** C01에서 여행 이름을 변경합니다. */
    fun updateTravelName(name: String) {
        if (!_uiState.value.canEditContent) return

        _uiState.update {
            it.copy(
                travelName = name,
                errorMessage = null,
            )
        }
    }

    /**
     * 선택한 일차의 마지막 순서에 관광지를 추가합니다.
     *
     * 이미 일정에 포함된 관광지는 중복으로 추가하지 않습니다.
     * 추가한 관광지는 해당 일차의 야구 경기 뒤에 배치됩니다.
     */
    fun addTravelSpots(day: Int, travelSpots: List<TravelSpot>) {
        if (!_uiState.value.canEditContent) return

        val course = _uiState.value.course ?: return
        val timelineItems = course.toTimelineItems(day) ?: return

        val existingSpotIds = course.days
            .flatMap { travelDay -> travelDay.places }
            .mapTo(mutableSetOf()) { place -> place.spot.id }

        val newSpots = travelSpots
            .distinctBy { spot -> spot.id }
            .filter { spot -> spot.id !in existingSpotIds }

        if (newSpots.isEmpty()) return

        val addedItems = newSpots.map { spot ->
            TravelCourseTimelineItem.Place(
                place = TravelPlace(
                    spot = spot,
                    order = 0,
                ),
            )
        }

        timelineItems.addAll(addedItems)
        updateCourse(course.updateTimeline(day, timelineItems))
    }

    /**
     * 선택한 일차에서 관광지를 삭제합니다.
     *
     * 경기 앞의 관광지를 삭제하면 야구 경기 배치 인덱스도 함께 감소합니다.
     */
    fun removeTravelSpot(day: Int, travelSpotId: String) {
        if (!_uiState.value.canEditContent) return

        val normalizedSpotId = travelSpotId.trim()
        if (normalizedSpotId.isEmpty()) return

        val course = _uiState.value.course ?: return
        val timelineItems = course.toTimelineItems(day) ?: return

        val removeIndex = timelineItems.indexOfFirst { item ->
            item is TravelCourseTimelineItem.Place &&
                item.place.spot.id == normalizedSpotId
        }

        if (removeIndex < 0) return

        timelineItems.removeAt(removeIndex)
        updateCourse(course.updateTimeline(day, timelineItems))
    }

    /**
     * 관광지를 같은 일차 또는 다른 일차의 지정 위치로 이동합니다.
     *
     * 야구 경기 자체는 이동하지 않으며, 관광지 이동에 따라
     * baseballGameAfterIdx를 다시 계산합니다.
     *
     * @param travelSpotId 이동할 관광지 ID입니다.
     * @param targetDay 이동할 대상 일차입니다. 1부터 시작합니다.
     * @param targetIndex 야구 경기를 포함한 목록의 삽입 위치입니다. 0부터 시작합니다.
     * 같은 일차에서는 이동할 관광지를 제거한 뒤의 목록을 기준으로 합니다.
     * 다른 일차에서는 관광지를 추가하기 전 대상 일차의 목록을 기준으로 합니다.
     */
    fun moveTravelSpot(travelSpotId: String, targetDay: Int, targetIndex: Int) {
        if (!_uiState.value.canEditContent) return

        val normalizedSpotId = travelSpotId.trim()
        if (normalizedSpotId.isEmpty()) return

        val course = _uiState.value.course ?: return
        val sourceTravelDay = course.days.firstOrNull { travelDay ->
            travelDay.places.any { place ->
                place.spot.id == normalizedSpotId
            }
        } ?: return

        val sourceDay = sourceTravelDay.day
        val sourceTimeline = course.toTimelineItems(sourceDay) ?: return
        val sourceIndex = sourceTimeline.indexOfFirst { item ->
            item is TravelCourseTimelineItem.Place &&
                item.place.spot.id == normalizedSpotId
        }

        if (sourceIndex < 0) return

        if (sourceDay == targetDay) {
            if (targetIndex !in 0..sourceTimeline.size) return
            if (sourceIndex == targetIndex) return

            val movedItem = sourceTimeline.removeAt(sourceIndex)
            val insertionIndex = targetIndex.coerceAtMost(sourceTimeline.size)

            sourceTimeline.add(insertionIndex, movedItem)
            updateCourse(course.updateTimeline(sourceDay, sourceTimeline))
            return
        }

        val targetTimeline = course.toTimelineItems(targetDay) ?: return
        if (targetIndex !in 0..targetTimeline.size) return

        val movedItem = sourceTimeline.removeAt(sourceIndex)
        targetTimeline.add(targetIndex, movedItem)

        val sourceUpdatedCourse = course.updateTimeline(
            day = sourceDay,
            timelineItems = sourceTimeline,
        )
        val fullyUpdatedCourse = sourceUpdatedCourse.updateTimeline(
            day = targetDay,
            timelineItems = targetTimeline,
        )

        updateCourse(fullyUpdatedCourse)
    }

    /** 현재 편집 중인 코스를 서버의 거리 기준으로 재정렬합니다. */
    fun alignTravelCourse() {
        val currentState = _uiState.value
        if (!currentState.canEditContent) return

        val course = currentState.course ?: return

        _uiState.update {
            it.copy(
                isAligning = true,
                errorMessage = null,
            )
        }

        alignJob = viewModelScope.launch {
            try {
                val alignedCourse = travelRepository.alignTravelCourse(course)

                _uiState.update {
                    it.copy(
                        course = alignedCourse,
                        isAligning = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(isAligning = false)
                }

                _events.send(TravelCourseEditEvent.SessionExpired)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isAligning = false,
                        errorMessage = exception.toTravelErrorMessage(
                            fallbackMessage = "여행 일정을 재정렬하지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /**
     * 현재 편집 내용을 저장합니다.
     *
     * B07에서 진입했다면 신규 여행을 생성하고,
     * 기존 여행 상세에서 진입했다면 저장된 여행을 수정합니다.
     */
    fun saveTravel() {
        val currentState = _uiState.value
        if (!currentState.canSave) return

        val travelName = currentState.travelName.trim()
        val course = currentState.course ?: return
        val travelId = currentState.travelId

        _uiState.update {
            it.copy(
                travelName = travelName,
                isSaving = true,
                errorMessage = null,
            )
        }

        saveJob = viewModelScope.launch {
            try {
                if (travelId == null) {
                    val params = newTravelParams ?: run {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "여행 저장 정보를 확인할 수 없습니다.",
                            )
                        }
                        return@launch
                    }

                    travelRepository.createTravel(
                        params.copy(
                            name = travelName,
                            course = course,
                        ),
                    )
                } else {
                    travelRepository.updateTravel(
                        travelId = travelId,
                        name = travelName,
                        course = course,
                    )
                }

                _uiState.update {
                    it.copy(isSaving = false)
                }

                _events.send(TravelCourseEditEvent.Saved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(isSaving = false)
                }

                _events.send(TravelCourseEditEvent.SessionExpired)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.toTravelErrorMessage(
                            fallbackMessage = "여행 일정을 저장하지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** Snackbar에 오류를 표시한 뒤 저장된 오류 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /** 편집 흐름을 종료하고 보관 중인 입력값과 작업을 초기화합니다. */
    fun reset() {
        cancelRequests()
        resetTravelSpotSelection()

        newTravelParams = null
        currentTravelId = null
        _uiState.value = TravelCourseEditUiState()
    }

    /** 조회 요청을 취소하고 대상 일차와 임시 선택 상태를 초기화합니다. */
    private fun resetTravelSpotSelection() {
        spotQuery.reset()
        _spotSelectionUiState.value = TravelCourseSpotSelectionUiState()
    }

    /** 저장된 여행과 경기 상세 정보를 Repository에서 조회합니다. */
    private fun loadExistingTravel(travelId: String) {
        loadJob?.cancel()

        _uiState.value = TravelCourseEditUiState(
            travelId = travelId,
            isLoading = true,
        )

        loadJob = viewModelScope.launch {
            try {
                val travel = travelRepository.getTravel(travelId)

                if (!travel.isLeader || travel.status != TravelStatus.UPCOMING) {
                    _uiState.value = TravelCourseEditUiState(
                        travelId = travelId,
                        errorMessage = "여행 시작 전에는 방장만 일정을 수정할 수 있습니다.",
                    )
                    return@launch
                }

                val baseballGame = baseballRepository.getGame(travel.baseballGame.id)

                _uiState.value = TravelCourseEditUiState(
                    travelId = travelId,
                    travelName = travel.name.orEmpty(),
                    startDate = travel.startDate,
                    endDate = travel.endDate,
                    companionCount = travel.friends.size,
                    baseballGame = baseballGame,
                    course = travel.toTravelCourse(),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(isLoading = false)
                }

                _events.send(TravelCourseEditEvent.SessionExpired)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toTravelErrorMessage(
                            fallbackMessage = "여행 일정을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 변경된 여행 코스를 화면 상태에 반영합니다. */
    private fun updateCourse(course: TravelCourse) {
        _uiState.update {
            it.copy(
                course = course,
                errorMessage = null,
            )
        }
    }

    /** 현재 실행 중인 조회, 재정렬과 저장 요청을 취소합니다. */
    private fun cancelRequests() {
        loadJob?.cancel()
        alignJob?.cancel()
        saveJob?.cancel()
    }
}

/**
 * 변경된 타임라인을 관광지 일정과 야구 경기 배치 정보에 반영합니다.
 */
private fun TravelCourse.updateTimeline(
    day: Int,
    timelineItems: List<TravelCourseTimelineItem>,
): TravelCourse {
    val updatedPlaces = timelineItems
        .filterIsInstance<TravelCourseTimelineItem.Place>()
        .map { item -> item.place }
        .normalizeOrders()

    val updatedDays = days.map { travelDay ->
        if (travelDay.day == day) {
            travelDay.copy(places = updatedPlaces)
        } else {
            travelDay
        }
    }

    val updatedBaseballGame = if (baseballGame.day == day) {
        val gameIndex = timelineItems.indexOfFirst { item ->
            item is TravelCourseTimelineItem.BaseballGame
        }

        baseballGame.copy(
            baseballGameAfterIdx = gameIndex.coerceAtLeast(0),
        )
    } else {
        baseballGame
    }

    return copy(
        baseballGame = updatedBaseballGame,
        days = updatedDays,
    )
}

/** 저장된 여행 상세 모델을 편집 가능한 여행 코스로 변환합니다. */
private fun Travel.toTravelCourse(): TravelCourse =
    TravelCourse(
        baseballGame = baseballGame,
        days = days,
    )

/** 관광지 순서를 1부터 다시 부여합니다. */
private fun List<TravelPlace>.normalizeOrders(): List<TravelPlace> =
    mapIndexed { index, place ->
        place.copy(order = index + 1)
    }

/** 요청을 실행하지 않아도 편집 내용을 변경할 수 있는 상태인지 나타냅니다. */
private val TravelCourseEditUiState.canEditContent: Boolean
    get() = course != null &&
        !isLoading &&
        !isRequestInProgress
