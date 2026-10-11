package com.manruhomerun.yadanbeopseok.travel.spot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 관광지 상세 본문, 별도 이미지 조회와 찜 상태를 관리합니다. */
@HiltViewModel
class TravelSpotDetailViewModel @Inject constructor(
    private val travelSpotRepository: TravelSpotRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelSpotDetailUiState())
    val uiState: StateFlow<TravelSpotDetailUiState> = _uiState.asStateFlow()

    private var currentSpotId: String? = null
    private var loadJob: Job? = null
    private var imagesJob: Job? = null
    private var dibsJob: Job? = null
    private var currentLoadRequest: Any? = null
    private var currentImagesRequest: Any? = null

    /** 전달받은 관광지 ID에 해당하는 상세 정보를 조회합니다. */
    fun loadTravelSpot(spotId: String) {
        val normalizedSpotId = spotId.trim()

        if (normalizedSpotId.isEmpty()) {
            currentSpotId = null
            cancelRequests()

            _uiState.value = TravelSpotDetailUiState(
                isLoading = false,
                errorMessage = "관광지 정보를 확인할 수 없습니다.",
            )
            return
        }

        val state = _uiState.value
        val isAlreadyLoaded = currentSpotId == normalizedSpotId && (state.isLoading || state.hasDetail)

        if (isAlreadyLoaded) return

        currentSpotId = normalizedSpotId
        requestTravelSpot(normalizedSpotId)
    }

    /** 현재 관광지 상세 조회를 다시 시도합니다. */
    fun retry() {
        val state = _uiState.value
        if (state.isLoading || state.hasDetail) return
        requestTravelSpot(currentSpotId ?: return)
    }

    /** 본문을 유지한 채 실패한 이미지 목록 조회만 다시 시도합니다. */
    fun retryImages() {
        val state = _uiState.value
        if (state.isLoadingImages || state.imageErrorMessage == null) return

        val spotId = currentSpotId ?: return
        val request = currentLoadRequest ?: return
        if (state.detail?.spot?.id != spotId) return

        requestTravelSpotImages(spotId, request)
    }

    /** 현재 찜 상태에 따라 찜 등록 또는 취소를 요청합니다. */
    fun toggleDibs() {
        val detail = _uiState.value.detail ?: return
        if (_uiState.value.isUpdatingDibs) return

        val spotId = detail.spot.id
        val request = currentLoadRequest ?: return
        if (!isCurrentLoadRequest(spotId, request)) return
        val wasDibs = detail.spot.dibs

        _uiState.update {
            it.copy(isUpdatingDibs = true, errorMessage = null)
        }

        dibsJob = viewModelScope.launch {
            try {
                if (wasDibs) {
                    travelSpotRepository.deleteTravelSpotDibs(spotId)
                } else {
                    travelSpotRepository.addTravelSpotDibs(spotId)
                }

                if (isCurrentLoadRequest(spotId, request)) {
                    _uiState.update { state ->
                        val currentDetail = state.detail ?: return@update state
                        state.copy(
                            detail = currentDetail.copy(
                                spot = currentDetail.spot.copy(dibs = !wasDibs),
                            ),
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                /*
                 * 로그인 이동은 앱의 공통 세션 관찰이 처리합니다.
                 * 찜 변경 진행 상태는 finally에서 정리합니다.
                 */
            } catch (exception: Exception) {
                if (isCurrentLoadRequest(spotId, request)) {
                    _uiState.update {
                        it.copy(
                            errorMessage = exception.toTravelSpotErrorMessage(
                                fallbackMessage = "찜 상태를 변경하지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (isCurrentLoadRequest(spotId, request)) {
                    _uiState.update { it.copy(isUpdatingDibs = false) }
                }
            }
        }
    }

    /** Snackbar에 오류를 표시한 뒤 저장된 문구를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** 상세 본문을 먼저 반영한 다음 이미지 목록 조회를 시작합니다. */
    private fun requestTravelSpot(spotId: String) {
        cancelRequests()
        val request = Any()
        currentLoadRequest = request

        _uiState.value = TravelSpotDetailUiState(isLoading = true)

        loadJob = viewModelScope.launch {
            try {
                val detail = travelSpotRepository.getTravelSpotDetail(spotId)

                if (!isCurrentLoadRequest(spotId, request)) return@launch

                _uiState.value = TravelSpotDetailUiState(
                    detail = detail,
                    isLoading = false,
                    isLoadingImages = true,
                )
                requestTravelSpotImages(spotId, request)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                /*
                 * 로그인 이동은 앱의 공통 세션 관찰이 처리합니다.
                 */
                if (isCurrentLoadRequest(spotId, request)) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                }
            } catch (exception: Exception) {
                if (isCurrentLoadRequest(spotId, request)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.toTravelSpotErrorMessage(
                                fallbackMessage = "관광지 정보를 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            }
        }
    }

    /** 이미지 결과만 최신 상세 상태에 합쳐 찜 상태와 본문을 보존합니다. */
    private fun requestTravelSpotImages(spotId: String, loadRequest: Any) {
        if (!isCurrentLoadRequest(spotId, loadRequest)) return

        val imagesRequest = Any()
        currentImagesRequest = imagesRequest
        imagesJob?.cancel()
        _uiState.update { it.copy(isLoadingImages = true, imageErrorMessage = null) }

        imagesJob = viewModelScope.launch {
            try {
                val imageUrls = travelSpotRepository.getTravelSpotImages(spotId)

                if (isCurrentImagesRequest(spotId, loadRequest, imagesRequest)) {
                    _uiState.update { state ->
                        val detail = state.detail ?: return@update state
                        state.copy(
                            detail = detail.copy(
                                spot = detail.spot.copy(imageUrl = imageUrls.firstOrNull()),
                                imageUrls = imageUrls,
                            ),
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                // 세션 만료는 이미지 오류로 표시하지 않고 공통 로그인 이동에 맡깁니다.
            } catch (exception: Exception) {
                if (isCurrentImagesRequest(spotId, loadRequest, imagesRequest)) {
                    _uiState.update {
                        it.copy(
                            imageErrorMessage = exception.toTravelSpotErrorMessage(
                                fallbackMessage = "이미지를 불러오지 못했습니다.",
                            ),
                        )
                    }
                }
            } finally {
                if (isCurrentImagesRequest(spotId, loadRequest, imagesRequest)) {
                    _uiState.update { it.copy(isLoadingImages = false) }
                }
            }
        }
    }

    /** 동일한 관광지의 이전 요청도 현재 화면 상태를 변경하지 못하게 구분합니다. */
    private fun isCurrentLoadRequest(spotId: String, request: Any): Boolean =
        currentSpotId == spotId && currentLoadRequest === request

    private fun isCurrentImagesRequest(spotId: String, loadRequest: Any, imagesRequest: Any): Boolean =
        isCurrentLoadRequest(spotId, loadRequest) && currentImagesRequest === imagesRequest

    /** 요청 식별자를 먼저 무효화한 뒤 이전 작업을 취소합니다. */
    private fun cancelRequests() {
        currentLoadRequest = null
        currentImagesRequest = null
        loadJob?.cancel()
        imagesJob?.cancel()
        dibsJob?.cancel()
    }
}

/** 내부 예외를 사용자에게 표시할 안전한 문구로 변환합니다. */
private fun Throwable.toTravelSpotErrorMessage(fallbackMessage: String): String =
    when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            "$fallbackMessage 잠시 후 다시 시도해주세요."
    }
