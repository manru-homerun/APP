package com.manruhomerun.yadanbeopseok.travel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.data.repository.CreateTravelParams
import com.manruhomerun.yadanbeopseok.data.repository.FriendRepository
import com.manruhomerun.yadanbeopseok.data.repository.GenerateTravelCourseParams
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelCompanionCondition
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import com.manruhomerun.yadanbeopseok.model.UserProfile
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 여행 생성 과정에서 발생하는 일회성 이벤트입니다.
 *
 * 실제 화면 이동은 Route에서 이벤트를 받아 처리합니다.
 */
sealed interface TravelCreationEvent {
    /** B·01 경기 선택이 완료됐습니다. */
    data object GameSelected : TravelCreationEvent

    /** 추천 여행 코스 생성이 완료됐습니다. */
    data object CourseGenerated : TravelCreationEvent

    /** 최종 여행 저장이 완료됐습니다. */
    data object TravelSaved : TravelCreationEvent

    /** 인증 정보가 만료됐습니다. */
    data object SessionExpired : TravelCreationEvent
}

/**
 * 경기 선택부터 추천 여행 저장까지 공유하는 입력 상태를 관리합니다.
 */
@HiltViewModel
class TravelCreationViewModel @Inject constructor(
    private val baseballRepository: BaseballRepository,
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
    private val friendRepository: FriendRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelCreationUiState())
    val uiState: StateFlow<TravelCreationUiState> = _uiState.asStateFlow()

    private val _gameSelectionUiState = MutableStateFlow(TravelGameSelectionUiState())
    val gameSelectionUiState: StateFlow<TravelGameSelectionUiState> = _gameSelectionUiState.asStateFlow()

    private val _themeSelectionUiState = MutableStateFlow(TravelThemeSelectionUiState())
    val themeSelectionUiState: StateFlow<TravelThemeSelectionUiState> = _themeSelectionUiState.asStateFlow()

    private val _companionSelectionUiState =
        MutableStateFlow(TravelCompanionSelectionUiState())
    val companionSelectionUiState: StateFlow<TravelCompanionSelectionUiState> = _companionSelectionUiState.asStateFlow()

    private val _spotSelectionUiState = MutableStateFlow(TravelSpotSelectionUiState())
    val spotSelectionUiState: StateFlow<TravelSpotSelectionUiState> = _spotSelectionUiState.asStateFlow()

    private val _events = Channel<TravelCreationEvent>(Channel.BUFFERED)
    val events: Flow<TravelCreationEvent> = _events.receiveAsFlow()

    private var scheduleLoadJob: Job? = null
    private var gameDetailLoadJob: Job? = null
    private var themeLoadJob: Job? = null
    private var companionLoadJob: Job? = null
    private var suggestedSpotsLoadJob: Job? = null
    private var dibsSpotsLoadJob: Job? = null
    private var spotSearchJob: Job? = null

    private var hasLoadedThemes = false
    private var hasLoadedCompanions = false
    private var loadedSuggestedSpotsRegion: Region? = null
    private var loadedDibsSpotsRegion: Region? = null

    /**
     * B·01에 처음 진입했을 때 기준 구단의 경기 일정을 조회합니다.
     *
     * 같은 구단으로 이미 초기화됐다면 다시 요청하지 않습니다.
     */
    fun initializeGameSelection(team: KboTeam) {
        if (_gameSelectionUiState.value.selectedTeam == team) return

        loadGameSchedule(team)
    }

    /** B·01의 구단 필터를 변경하고 해당 구단의 경기 일정을 조회합니다. */
    fun selectScheduleTeam(team: KboTeam) {
        if (_gameSelectionUiState.value.selectedTeam == team) return

        loadGameSchedule(team)
    }

    /** B·01 경기 카드의 선택 상태를 변경합니다. */
    fun selectGameSummary(gameId: String) {
        val currentState = _gameSelectionUiState.value

        if (currentState.isScheduleLoading || currentState.isGameDetailLoading) return
        if (currentState.games.none { game -> game.id == gameId }) return
        if (currentState.selectedGameId == gameId) return

        _gameSelectionUiState.update {
            it.copy(
                selectedGameId = gameId,
                errorMessage = null,
            )
        }
    }

    /**
     * A·05에서 전달받은 경기 ID로 경기 상세 정보를 조회하고
     * B·02부터 여행 만들기를 시작할 수 있도록 준비합니다.
     */
    fun initializeSelectedGame(gameId: String) {
        val normalizedGameId = gameId.trim()
        if (normalizedGameId.isEmpty()) return
        if (_uiState.value.selectedGame?.id == normalizedGameId) return

        loadSelectedGame(normalizedGameId, requireCurrentSelection = false)
    }

    /**
     * B·01에서 선택한 경기의 상세 정보를 조회하고 공유 입력 상태에 저장합니다.
     */
    fun confirmSelectedGame() {
        val currentState = _gameSelectionUiState.value
        val selectedGame = currentState.selectedGame ?: return

        if (!currentState.isContinueEnabled) return

        loadSelectedGame(selectedGame.id, requireCurrentSelection = true)
    }

    /** 현재 선택된 구단의 경기 일정 조회를 다시 시도합니다. */
    fun retryGameSchedule() {
        val currentState = _gameSelectionUiState.value
        val selectedTeam = currentState.selectedTeam ?: return

        if (currentState.isScheduleLoading) return

        loadGameSchedule(selectedTeam)
    }

    /** B·02 동행 조건의 선택 여부를 전환합니다. */
    fun toggleCompanionCondition(condition: TravelCompanionCondition) {
        _uiState.update { currentState ->
            val selectedConditions = currentState.selectedCompanionConditions

            val updatedConditions = if (condition in selectedConditions) {
                selectedConditions - condition
            } else {
                selectedConditions + condition
            }

            currentState.copy(
                selectedCompanionConditions = updatedConditions,
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /** B·03 여행 테마 목록을 최초 한 번 조회합니다. */
    fun initializeThemeSelection() {
        if (hasLoadedThemes || _themeSelectionUiState.value.isLoading) return

        loadTravelThemes()
    }

    /** B·03 여행 테마 목록 조회를 다시 시도합니다. */
    fun retryThemeSelection() {
        if (_themeSelectionUiState.value.isLoading) return

        loadTravelThemes()
    }

    /** B·03 여행 테마의 선택 여부를 전환합니다. */
    fun toggleTheme(theme: TravelTheme) {
        _uiState.update { currentState ->
            val selectedThemes = currentState.selectedThemes
            val isSelected = selectedThemes.any { it.id == theme.id }

            if (!isSelected && selectedThemes.size >= MAX_THEME_COUNT) {
                return@update currentState
            }

            val updatedThemes = if (isSelected) {
                selectedThemes.filterNot { it.id == theme.id }
            } else {
                selectedThemes + theme
            }

            currentState.copy(
                selectedThemes = updatedThemes,
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /** B·04 동행자로 선택할 수 있는 친구 목록을 최초 한 번 조회합니다. */
    fun initializeCompanionSelection() {
        if (hasLoadedCompanions || _companionSelectionUiState.value.isLoading) return

        loadCompanions()
    }

    /** B·04 친구 목록 조회를 다시 시도합니다. */
    fun retryCompanionSelection() {
        if (_companionSelectionUiState.value.isLoading) return

        loadCompanions()
    }

    /** B·04 동행자의 선택 여부를 전환합니다. */
    fun toggleCompanion(companion: UserProfile) {
        if (companion.nickname.isNullOrBlank()) return

        _uiState.update { currentState ->
            val selectedCompanions = currentState.selectedCompanions
            val isSelected = selectedCompanions.any { it.id == companion.id }

            if (!isSelected && selectedCompanions.size >= MAX_COMPANION_COUNT) {
                return@update currentState
            }

            val updatedCompanions = if (isSelected) {
                selectedCompanions.filterNot { it.id == companion.id }
            } else {
                selectedCompanions + companion
            }

            currentState.copy(
                selectedCompanions = updatedCompanions,
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /**
     * B·05에서 선택한 여행 기간을 저장합니다.
     *
     * 최대 2박 3일이며 선택한 경기일을 반드시 포함해야 합니다.
     */
    fun selectDateRange(startDate: LocalDate, endDate: LocalDate) {
        val selectedGame = _uiState.value.selectedGame ?: return

        if (!isValidDateRange(selectedGame, startDate, endDate)) return

        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /** B·06에 진입했을 때 현재 탭의 관광지 목록을 조회합니다. */
    fun initializeTravelSpotSelection() {
        val region = selectedTravelRegion() ?: return
        val currentState = _spotSelectionUiState.value

        when (currentState.selectedTab) {
            TravelSpotSelectionTab.SUGGESTED -> {
                if (
                    loadedSuggestedSpotsRegion != region &&
                    !currentState.isSuggestedSpotsLoading
                ) {
                    loadSuggestedTravelSpots(region)
                }
            }

            TravelSpotSelectionTab.DIBS -> {
                if (
                    loadedDibsSpotsRegion != region &&
                    !currentState.isDibsSpotsLoading
                ) {
                    loadDibsTravelSpots(region)
                }
            }
        }
    }

    /** B·06의 추천 또는 찜 탭을 변경하고 필요한 목록을 조회합니다. */
    fun selectTravelSpotTab(tab: TravelSpotSelectionTab) {
        _spotSelectionUiState.update {
            it.copy(
                selectedTab = tab,
                errorMessage = null,
            )
        }

        val region = selectedTravelRegion() ?: return
        val currentState = _spotSelectionUiState.value

        when (tab) {
            TravelSpotSelectionTab.SUGGESTED -> {
                if (
                    loadedSuggestedSpotsRegion != region &&
                    !currentState.isSuggestedSpotsLoading
                ) {
                    loadSuggestedTravelSpots(region)
                }
            }

            TravelSpotSelectionTab.DIBS -> {
                if (
                    loadedDibsSpotsRegion != region &&
                    !currentState.isDibsSpotsLoading
                ) {
                    loadDibsTravelSpots(region)
                }
            }
        }
    }

    /** B·06 관광지 검색어를 변경합니다. */
    fun updateTravelSpotSearchQuery(query: String) {
        if (_spotSelectionUiState.value.searchQuery == query) return

        spotSearchJob?.cancel()

        _spotSelectionUiState.update {
            it.copy(
                searchQuery = query,
                searchResults = emptyList(),
                selectedCategory = if (query.isBlank()) null else it.selectedCategory,
                isSearchLoading = false,
                errorMessage = null,
            )
        }
    }

    /** 입력된 검색어를 기준으로 관광지를 검색합니다. */
    fun searchTravelSpots() {
        val searchKeyword = _spotSelectionUiState.value.searchQuery.trim()

        if (searchKeyword.isEmpty()) {
            clearTravelSpotSearch()
            return
        }

        spotSearchJob?.cancel()

        _spotSelectionUiState.update {
            it.copy(
                searchQuery = searchKeyword,
                searchResults = emptyList(),
                isSearchLoading = true,
                errorMessage = null,
            )
        }

        spotSearchJob = viewModelScope.launch {
            try {
                val spots = travelSpotRepository.searchTravelSpots(searchKeyword)

                if (_spotSelectionUiState.value.searchQuery != searchKeyword) {
                    return@launch
                }

                _spotSelectionUiState.update {
                    it.copy(
                        searchResults = spots,
                        isSearchLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _spotSelectionUiState.update {
                    it.copy(isSearchLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _spotSelectionUiState.update {
                    it.copy(
                        searchResults = emptyList(),
                        isSearchLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "관광지를 검색하지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 검색 화면에서 기본 관광지 목록으로 돌아갑니다. */
    fun clearTravelSpotSearch() {
        spotSearchJob?.cancel()

        _spotSelectionUiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                selectedCategory = null,
                isSearchLoading = false,
                errorMessage = null,
            )
        }
    }

    /** 관광지 검색 결과에 적용할 카테고리를 변경합니다. */
    fun selectTravelSpotCategory(category: TravelSpotCategory?) {
        if (_spotSelectionUiState.value.selectedCategory == category) return

        _spotSelectionUiState.update {
            it.copy(selectedCategory = category)
        }
    }

    /** 현재 B·06 화면 상태에 해당하는 관광지 조회를 다시 시도합니다. */
    fun retryTravelSpotSelection() {
        val currentState = _spotSelectionUiState.value

        if (currentState.isLoading) return

        if (currentState.isSearchMode) {
            searchTravelSpots()
            return
        }

        val region = selectedTravelRegion() ?: return

        when (currentState.selectedTab) {
            TravelSpotSelectionTab.SUGGESTED -> {
                loadSuggestedTravelSpots(region)
            }

            TravelSpotSelectionTab.DIBS -> {
                loadDibsTravelSpots(region)
            }
        }
    }

    /** B·06 관광지의 필수 포함 여부를 전환합니다. */
    fun toggleTravelSpot(travelSpot: TravelSpot) {
        _uiState.update { currentState ->
            val selectedTravelSpots = currentState.selectedTravelSpots
            val isSelected = selectedTravelSpots.any { it.id == travelSpot.id }

            val updatedTravelSpots = if (isSelected) {
                selectedTravelSpots.filterNot { it.id == travelSpot.id }
            } else {
                selectedTravelSpots + travelSpot
            }

            currentState.copy(
                selectedTravelSpots = updatedTravelSpots,
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /** B·07에서 최종 저장할 여행 이름을 변경합니다. */
    fun updateTravelName(name: String) {
        _uiState.update {
            it.copy(
                travelName = name,
                errorMessage = null,
            )
        }
    }

    /** 추천 결과에서 B·06으로 돌아갈 때 생성된 추천 코스만 제거합니다. */
    fun clearGeneratedCourse() {
        val currentState = _uiState.value
        if (currentState.isGenerating || currentState.isSaving) return

        _uiState.update {
            it.copy(
                generatedCourse = null,
                errorMessage = null,
            )
        }
    }

    /** 현재 입력값으로 AI 추천 여행 코스를 생성합니다. */
    fun generateTravelCourse() {
        val currentState = _uiState.value
        if (currentState.isGenerating || currentState.isSaving) return

        val params = currentState.toGenerateTravelCourseParams() ?: return

        _uiState.update {
            it.copy(
                generatedCourse = null,
                isGenerating = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val generatedCourse = travelRepository.generateTravelCourse(params)
                val defaultName = currentState.selectedGame?.toDefaultTravelName().orEmpty()

                _uiState.update {
                    it.copy(
                        generatedCourse = generatedCourse,
                        travelName = it.travelName.ifBlank { defaultName },
                        isGenerating = false,
                    )
                }

                _events.send(TravelCreationEvent.CourseGenerated)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(isGenerating = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "여행 코스를 만들지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 생성된 여행 코스를 최종 저장합니다. */
    fun saveTravel() {
        val currentState = _uiState.value
        if (currentState.isGenerating || currentState.isSaving) return

        val params = currentState.toCreateTravelParams() ?: return

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                travelRepository.createTravel(params)

                _uiState.update {
                    it.copy(isSaving = false)
                }

                _events.send(TravelCreationEvent.TravelSaved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _uiState.update {
                    it.copy(isSaving = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "여행을 저장하지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** Snackbar에 오류를 표시한 뒤 현재 오류 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /** 새로운 여행 만들기를 시작할 수 있도록 모든 상태를 초기화합니다. */
    fun resetCreation() {
        cancelLoadingJobs()

        hasLoadedThemes = false
        hasLoadedCompanions = false
        loadedSuggestedSpotsRegion = null
        loadedDibsSpotsRegion = null

        _uiState.value = TravelCreationUiState()
        _gameSelectionUiState.value = TravelGameSelectionUiState()
        _themeSelectionUiState.value = TravelThemeSelectionUiState()
        _companionSelectionUiState.value = TravelCompanionSelectionUiState()
        _spotSelectionUiState.value = TravelSpotSelectionUiState()
    }

    /**
     * 경기 상세 정보를 조회해 여행 만들기 공유 상태에 저장합니다.
     *
     * [requireCurrentSelection]이 true이면 B·01에서 현재 선택된 경기와
     * 조회 결과의 경기 ID가 일치할 때만 저장합니다.
     */
    private fun loadSelectedGame(gameId: String, requireCurrentSelection: Boolean) {
        gameDetailLoadJob?.cancel()

        _gameSelectionUiState.update {
            it.copy(
                isGameDetailLoading = true,
                errorMessage = null,
            )
        }

        gameDetailLoadJob = viewModelScope.launch {
            try {
                val game = baseballRepository.getGame(gameId)

                val selectedGameId = _gameSelectionUiState.value.selectedGameId
                val isSelectionChanged = requireCurrentSelection && selectedGameId != gameId

                if (isSelectionChanged) {
                    _gameSelectionUiState.update {
                        it.copy(isGameDetailLoading = false)
                    }
                    return@launch
                }

                saveSelectedGame(game)

                _gameSelectionUiState.update {
                    it.copy(isGameDetailLoading = false)
                }

                _events.send(TravelCreationEvent.GameSelected)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _gameSelectionUiState.update {
                    it.copy(isGameDetailLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _gameSelectionUiState.update {
                    it.copy(
                        isGameDetailLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "경기 정보를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 선택한 구단의 요청일 기준 최대 2주 경기 일정을 조회합니다. */
    private fun loadGameSchedule(team: KboTeam) {
        scheduleLoadJob?.cancel()
        gameDetailLoadJob?.cancel()

        val confirmedGameId = _uiState.value.selectedGame?.id

        _gameSelectionUiState.value = TravelGameSelectionUiState(
            selectedTeam = team,
            selectedGameId = confirmedGameId,
            isScheduleLoading = true,
        )

        scheduleLoadJob = viewModelScope.launch {
            try {
                val games = baseballRepository.getTeamGameSchedule(team)
                    .sortedBy { game -> game.gameDateTime }

                if (_gameSelectionUiState.value.selectedTeam != team) {
                    return@launch
                }

                val selectedGameId = confirmedGameId?.takeIf { gameId ->
                    games.any { game -> game.id == gameId }
                }

                _gameSelectionUiState.update {
                    it.copy(
                        games = games,
                        selectedGameId = selectedGameId,
                        isScheduleLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _gameSelectionUiState.update {
                    it.copy(isScheduleLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                if (_gameSelectionUiState.value.selectedTeam == team) {
                    _gameSelectionUiState.update {
                        it.copy(
                            games = emptyList(),
                            selectedGameId = null,
                            isScheduleLoading = false,
                            errorMessage = exception.toTravelCreationErrorMessage(
                                fallbackMessage = "경기 일정을 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            }
        }
    }

    /** 서버에서 여행 테마 목록을 조회합니다. */
    private fun loadTravelThemes() {
        themeLoadJob?.cancel()

        _themeSelectionUiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        themeLoadJob = viewModelScope.launch {
            try {
                val themes = travelRepository.getTravelThemes()
                hasLoadedThemes = true

                _themeSelectionUiState.update {
                    it.copy(
                        themes = themes,
                        isLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _themeSelectionUiState.update {
                    it.copy(isLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _themeSelectionUiState.update {
                    it.copy(
                        themes = emptyList(),
                        isLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "여행 테마를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 서버에서 현재 사용자의 친구 목록을 조회합니다. */
    private fun loadCompanions() {
        companionLoadJob?.cancel()

        _companionSelectionUiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        companionLoadJob = viewModelScope.launch {
            try {
                val companions = friendRepository.getFriends().map { friend ->
                    friend.user
                }

                hasLoadedCompanions = true

                _companionSelectionUiState.update {
                    it.copy(
                        friends = companions,
                        isLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _companionSelectionUiState.update {
                    it.copy(isLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _companionSelectionUiState.update {
                    it.copy(
                        friends = emptyList(),
                        isLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "친구 목록을 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 선택한 경기 지역의 맞춤 추천 관광지를 조회합니다. */
    private fun loadSuggestedTravelSpots(region: Region) {
        suggestedSpotsLoadJob?.cancel()

        _spotSelectionUiState.update {
            it.copy(
                isSuggestedSpotsLoading = true,
                errorMessage = null,
            )
        }

        suggestedSpotsLoadJob = viewModelScope.launch {
            try {
                val spots = travelSpotRepository.getSuggestedTravelSpots(region)

                if (selectedTravelRegion() != region) {
                    return@launch
                }

                loadedSuggestedSpotsRegion = region

                _spotSelectionUiState.update {
                    it.copy(
                        suggestedSpots = spots,
                        isSuggestedSpotsLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _spotSelectionUiState.update {
                    it.copy(isSuggestedSpotsLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _spotSelectionUiState.update {
                    it.copy(
                        suggestedSpots = emptyList(),
                        isSuggestedSpotsLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "추천 관광지를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 선택한 경기 지역에 해당하는 찜한 관광지를 조회합니다. */
    private fun loadDibsTravelSpots(region: Region) {
        dibsSpotsLoadJob?.cancel()

        _spotSelectionUiState.update {
            it.copy(
                isDibsSpotsLoading = true,
                errorMessage = null,
            )
        }

        dibsSpotsLoadJob = viewModelScope.launch {
            try {
                val spots = travelSpotRepository.getTravelSpotDibs(region)

                if (selectedTravelRegion() != region) {
                    return@launch
                }

                loadedDibsSpotsRegion = region

                _spotSelectionUiState.update {
                    it.copy(
                        dibsSpots = spots,
                        isDibsSpotsLoading = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SessionExpiredException) {
                _spotSelectionUiState.update {
                    it.copy(isDibsSpotsLoading = false)
                }

                _events.send(TravelCreationEvent.SessionExpired)
            } catch (exception: Exception) {
                _spotSelectionUiState.update {
                    it.copy(
                        dibsSpots = emptyList(),
                        isDibsSpotsLoading = false,
                        errorMessage = exception.toTravelCreationErrorMessage(
                            fallbackMessage = "찜한 관광지를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
        }
    }

    /** 선택한 경기 상세 정보를 여행 생성 공유 상태에 저장합니다. */
    private fun saveSelectedGame(game: BaseballGame) {
        if (_uiState.value.selectedGame?.id == game.id) return

        resetTravelSpotSelection()

        _uiState.update {
            it.copy(
                selectedGame = game,
                startDate = null,
                endDate = null,
                selectedTravelSpots = emptyList(),
                generatedCourse = null,
                travelName = "",
                errorMessage = null,
            )
        }
    }

    /** 선택한 경기가 바뀌었을 때 기존 지역의 관광지 목록을 초기화합니다. */
    private fun resetTravelSpotSelection() {
        suggestedSpotsLoadJob?.cancel()
        dibsSpotsLoadJob?.cancel()
        spotSearchJob?.cancel()

        loadedSuggestedSpotsRegion = null
        loadedDibsSpotsRegion = null
        _spotSelectionUiState.value = TravelSpotSelectionUiState()
    }

    /** 현재 선택한 경기장의 지역을 반환합니다. */
    private fun selectedTravelRegion(): Region? {
        return _uiState.value.selectedGame?.stadium?.region
    }

    /** ViewModel에서 실행 중인 모든 조회 작업을 취소합니다. */
    private fun cancelLoadingJobs() {
        scheduleLoadJob?.cancel()
        gameDetailLoadJob?.cancel()
        themeLoadJob?.cancel()
        companionLoadJob?.cancel()
        suggestedSpotsLoadJob?.cancel()
        dibsSpotsLoadJob?.cancel()
        spotSearchJob?.cancel()
    }
}

/** 여행 코스 생성 요청에 필요한 입력값으로 변환합니다. */
private fun TravelCreationUiState.toGenerateTravelCourseParams(): GenerateTravelCourseParams? {
    val game = selectedGame ?: return null
    val resolvedStartDate = startDate ?: return null
    val resolvedEndDate = endDate ?: return null

    if (selectedThemes.isEmpty() || selectedThemes.size > MAX_THEME_COUNT) return null
    if (!isValidDateRange(game, resolvedStartDate, resolvedEndDate)) return null

    val friendNicknames = selectedCompanions.toFriendNicknamesOrNull() ?: return null

    return GenerateTravelCourseParams(
        startDate = resolvedStartDate,
        endDate = resolvedEndDate,
        baseballGameId = game.id,
        region = game.stadium.region,
        friendNicknames = friendNicknames,
        companionConditions = selectedCompanionConditions.toList(),
        themeIds = selectedThemes.map { theme -> theme.id },
        travelSpotIds = selectedTravelSpots.map { spot -> spot.id },
    )
}

/** 최종 여행 저장 요청에 필요한 입력값으로 변환합니다. */
private fun TravelCreationUiState.toCreateTravelParams(): CreateTravelParams? {
    val game = selectedGame ?: return null
    val resolvedStartDate = startDate ?: return null
    val resolvedEndDate = endDate ?: return null
    val resolvedCourse = generatedCourse ?: return null
    val resolvedName = travelName.trim().takeIf { it.isNotEmpty() } ?: return null
    val friendNicknames = selectedCompanions.toFriendNicknamesOrNull() ?: return null

    return CreateTravelParams(
        startDate = resolvedStartDate,
        endDate = resolvedEndDate,
        name = resolvedName,
        region = game.stadium.region,
        friendNicknames = friendNicknames,
        themeIds = selectedThemes.map { it.id },
        course = resolvedCourse,
    )
}

/** 선택한 사용자들의 고유 닉네임을 서버 요청값으로 변환합니다. */
private fun List<UserProfile>.toFriendNicknamesOrNull(): List<String>? {
    val nicknames = mapNotNull { user ->
        val nickname = user.nickname?.trim()
        nickname?.takeIf { it.isNotEmpty() }
    }

    return nicknames.takeIf { it.size == size }
}

/** 여행 기간이 최대 2박 3일이고 선택 경기일을 포함하는지 검사합니다. */
internal fun isValidDateRange(
    game: BaseballGame,
    startDate: LocalDate,
    endDate: LocalDate,
): Boolean {
    if (endDate < startDate) return false
    if (endDate > startDate.plus(MAX_TRAVEL_NIGHTS, DateTimeUnit.DAY)) return false

    return game.gameDateTime.date in startDate..endDate
}

/** 선택 경기를 기준으로 기본 여행 이름을 생성합니다. */
private fun BaseballGame.toDefaultTravelName(): String {
    return "${stadium.region.displayName} ${stadium.name} 직관 여행"
}

/** 여행 생성·저장 예외를 사용자 안내 문구로 변환합니다. */
private fun Throwable.toTravelCreationErrorMessage(fallbackMessage: String): String {
    return when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        is ApiException, is InvalidResponseException ->
            "$fallbackMessage 잠시 후 다시 시도해주세요."

        else ->
            "$fallbackMessage 잠시 후 다시 시도해주세요."
    }
}

private const val MAX_THEME_COUNT = 3
internal const val MAX_COMPANION_COUNT = 2
internal const val MAX_TRAVEL_NIGHTS = 2
