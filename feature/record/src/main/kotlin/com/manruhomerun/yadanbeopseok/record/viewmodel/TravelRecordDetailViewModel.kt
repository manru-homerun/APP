package com.manruhomerun.yadanbeopseok.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.BaseballRepository
import com.manruhomerun.yadanbeopseok.data.repository.StickerRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * D·01b 지난 여행 상세 화면의 여행, 경기와 획득 스티커 정보를 관리합니다.
 */
@HiltViewModel
class TravelRecordDetailViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val baseballRepository: BaseballRepository,
    private val stickerRepository: StickerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelRecordDetailUiState())
    val uiState: StateFlow<TravelRecordDetailUiState> = _uiState.asStateFlow()

    private var currentTravelId: String? = null
    private var detailLoadJob: Job? = null
    private var stickerLoadJob: Job? = null

    /**
     * 여행 상세와 해당 여행의 야구 경기 정보를 조회합니다.
     *
     * 방문 인증이 모두 완료된 여행이면 획득 스티커도 이어서 조회합니다.
     */
    fun loadTravel(travelId: String) {
        val normalizedTravelId = travelId.trim()

        if (normalizedTravelId.isEmpty()) {
            detailLoadJob?.cancel()
            stickerLoadJob?.cancel()
            currentTravelId = null

            _uiState.value = TravelRecordDetailUiState(
                isLoading = false,
                errorMessage = "여행 정보를 확인할 수 없습니다.",
            )
            return
        }

        val isAlreadyLoaded =
            currentTravelId == normalizedTravelId &&
                _uiState.value.hasTravelDetail

        if (isAlreadyLoaded) return

        currentTravelId = normalizedTravelId
        requestTravelDetail(normalizedTravelId)
    }

    /**
     * 여행과 경기 상세 조회를 다시 시도합니다.
     */
    fun retry() {
        if (detailLoadJob?.isActive == true) return

        val travelId = currentTravelId ?: return
        requestTravelDetail(travelId)
    }

    /**
     * 전체 인증 완료 후 실패했던 스티커 조회만 다시 시도합니다.
     */
    fun retrySticker() {
        val currentState = _uiState.value
        val travelId = currentTravelId ?: return

        if (stickerLoadJob?.isActive == true) return
        if (!currentState.isCertificationCompleted) return

        requestStickerPack(travelId)
    }

    /**
     * 여행 상세를 조회한 뒤 여행에 포함된 야구 경기 상세를 조회합니다.
     */
    private fun requestTravelDetail(travelId: String) {
        detailLoadJob?.cancel()
        stickerLoadJob?.cancel()

        _uiState.value = TravelRecordDetailUiState()

        detailLoadJob = viewModelScope.launch {
            try {
                val travel = travelRepository.getTravel(travelId)
                val baseballGame = baseballRepository.getGame(gameId = travel.baseballGame.id,)

                val shouldLoadSticker =
                    travel.certificationTargetCount > 0 &&
                        travel.certifiedSpotsCount >= travel.certificationTargetCount

                _uiState.value = TravelRecordDetailUiState(
                    travel = travel,
                    baseballGame = baseballGame,
                    isLoading = false,
                    isStickerLoading = shouldLoadSticker,
                )

                if (shouldLoadSticker) {
                    requestStickerPack(travelId)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isStickerLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                val errorMessage = exception.toTravelRecordDetailErrorMessage(
                    fallbackMessage = "지난 여행 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
                )

                _uiState.value = TravelRecordDetailUiState(
                    isLoading = false,
                    errorMessage = errorMessage,
                )
            }
        }
    }

    /**
     * 인증 완료 여행에서 획득한 스티커팩을 조회합니다.
     */
    private fun requestStickerPack(travelId: String) {
        stickerLoadJob?.cancel()

        _uiState.update {
            it.copy(
                isStickerLoading = true,
                stickerErrorMessage = null,
            )
        }

        stickerLoadJob = viewModelScope.launch {
            try {
                val stickerPack = stickerRepository.getTravelStickerPack(travelId)

                _uiState.update {
                    it.copy(
                        stickerPack = stickerPack,
                        isStickerLoading = false,
                        stickerErrorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                _uiState.update {
                    it.copy(
                        isStickerLoading = false,
                        stickerErrorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                val errorMessage = exception.toTravelRecordDetailErrorMessage(
                    fallbackMessage = "스티커 정보를 불러오지 못했습니다. 다시 시도해주세요.",
                )

                _uiState.update {
                    it.copy(
                        stickerPack = null,
                        isStickerLoading = false,
                        stickerErrorMessage = errorMessage,
                    )
                }
            }
        }
    }
}

/**
 * 내부 예외 정보를 노출하지 않고 사용자 안내 문구로 변환합니다.
 */
private fun Exception.toTravelRecordDetailErrorMessage(fallbackMessage: String): String {
    return when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else ->
            fallbackMessage
    }
}
