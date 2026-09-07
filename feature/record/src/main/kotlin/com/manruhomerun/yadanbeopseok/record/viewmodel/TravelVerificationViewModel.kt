package com.manruhomerun.yadanbeopseok.record.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.StickerRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRecordRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationProvider
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 방문 인증에 필요한 정보와 위치를 조회하고,
 * 인증 요청, 진행률 갱신, 스티커 조회를 순서대로 처리합니다.
 *
 * 시스템 권한 요청과 실제 화면 이동은 Route에서 처리합니다.
 */
@HiltViewModel
class TravelVerificationViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val travelSpotRepository: TravelSpotRepository,
    private val travelRecordRepository: TravelRecordRepository,
    private val stickerRepository: StickerRepository,
    private val currentLocationProvider: CurrentLocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelVerificationUiState())
    val uiState = _uiState.asStateFlow()

    private val verificationTimeZone = TimeZone.of("Asia/Seoul")

    private var currentTravelId: String? = null
    private var currentSpotId: String? = null
    private var requestJob: Job? = null
    private var isSessionExpired = false

    /** 동일한 인증 화면이 재구성되어도 초기 조회를 반복하지 않습니다. */
    fun loadVerification(travelId: String, spotId: String) {
        if (isSessionExpired || requestJob?.isActive == true) return
        if (_uiState.value.certification != null) return

        val normalizedTravelId = travelId.trim()
        val normalizedSpotId = spotId.trim()

        val isSameTarget = currentTravelId == normalizedTravelId &&
            currentSpotId == normalizedSpotId

        if (isSameTarget) return

        currentTravelId = normalizedTravelId
        currentSpotId = normalizedSpotId
        _uiState.value = TravelVerificationUiState()

        if (normalizedTravelId.isEmpty() || normalizedSpotId.isEmpty()) {
            showError("방문 인증 대상 정보를 확인할 수 없습니다.")
            return
        }

        request(TravelVerificationRetryAction.LOAD_CONTENT)
    }

    /** 권한 허용 또는 위치 서비스 설정 변경 후 현재 위치를 다시 확인합니다. */
    fun refreshLocation() {
        val state = _uiState.value

        if (state.travel == null || state.targetSpot == null) return
        if (state.certification != null) return

        val canRefresh = state.phase == TravelVerificationPhase.READY ||
            state.retryAction == TravelVerificationRetryAction.LOAD_LOCATION

        if (!canRefresh) return

        request(TravelVerificationRetryAction.LOAD_LOCATION)
    }

    /** 인증 버튼에서 호출하며, 요청 중 중복 호출은 차단합니다. */
    fun verifySpot() {
        val state = _uiState.value

        if (state.phase != TravelVerificationPhase.READY) return
        if (state.certification != null) return

        request(TravelVerificationRetryAction.VERIFY_SPOT)
    }

    /** 실패한 작업만 재시도하며 자동으로 POST를 반복하지 않습니다. */
    fun retry() {
        val action = _uiState.value.retryAction ?: return
        request(action)
    }

    /**
     * D02b 표시가 끝난 뒤 전체 인증이 완료된 경우 스티커를 조회합니다.
     *
     * 일부 인증만 완료된 경우에는 Route가 일정 화면으로 돌아가므로
     * 이 함수에서 별도의 작업을 실행하지 않습니다.
     */
    fun loadStickerReward() {
        val state = _uiState.value
        val travel = state.travel ?: return

        val isAllCertified = travel.certificationTargetCount > 0 &&
            travel.certifiedSpotsCount == travel.certificationTargetCount

        if (state.phase != TravelVerificationPhase.VERIFIED) return
        if (!isAllCertified) return

        request(TravelVerificationRetryAction.LOAD_STICKERS)
    }

    private fun request(action: TravelVerificationRetryAction) {
        if (isSessionExpired || requestJob?.isActive == true) return

        requestJob = viewModelScope.launch {
            try {
                when (action) {
                    TravelVerificationRetryAction.LOAD_CONTENT -> loadContent()
                    TravelVerificationRetryAction.LOAD_LOCATION -> loadLocation()
                    TravelVerificationRetryAction.VERIFY_SPOT -> submitVerification()
                    TravelVerificationRetryAction.REFRESH_TRAVEL -> refreshTravel()
                    TravelVerificationRetryAction.LOAD_STICKERS -> loadStickerPack()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                isSessionExpired = true
                _uiState.update {
                    it.copy(
                        phase = TravelVerificationPhase.ERROR,
                        retryAction = null,
                        errorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                val retryAction = when (_uiState.value.phase) {
                    TravelVerificationPhase.LOCATING ->
                        TravelVerificationRetryAction.LOAD_LOCATION

                    // POST 응답을 받지 못했어도 서버에 저장됐을 수 있습니다.
                    // 다시 POST하기 전에 여행 조회로 인증 여부를 확인합니다.
                    TravelVerificationPhase.SUBMITTING,
                    TravelVerificationPhase.REFRESHING_TRAVEL ->
                        TravelVerificationRetryAction.REFRESH_TRAVEL

                    TravelVerificationPhase.LOADING_STICKERS ->
                        TravelVerificationRetryAction.LOAD_STICKERS

                    else -> action
                }

                showError(exception.toVerificationErrorMessage(), retryAction)
            }
        }
    }

    private suspend fun loadContent() {
        val travelId = currentTravelId ?: return
        val spotId = currentSpotId ?: return

        setPhase(TravelVerificationPhase.LOADING)

        val travel = travelRepository.getTravel(travelId)

        if (travel.status != TravelStatus.ACTIVE) {
            showError("여행 중에만 방문 인증을 할 수 있습니다.")
            return
        }

        val day = travel.days.firstOrNull { travelDay ->
            travelDay.places.any { place -> place.spot.id == spotId }
        }
        val place = day?.places?.firstOrNull { it.spot.id == spotId }

        if (day == null || place == null || !place.isCertificationTarget) {
            showError("이 여행의 방문 인증 대상 관광지가 아닙니다.")
            return
        }

        if (place.isCertified) {
            showError("이미 방문 인증을 완료한 관광지입니다.")
            return
        }

        val detail = travelSpotRepository.getTravelSpotDetail(spotId)

        if (detail.spot.id != spotId) {
            throw InvalidResponseException("Unexpected travel spot ID.")
        }

        _uiState.update {
            it.copy(
                travel = travel,
                targetSpot = detail,
                travelDay = day.day,
            )
        }

        loadLocation()
    }

    /** 위치를 확보해도 인증 성공으로 처리하지 않습니다. */
    private suspend fun loadLocation(): Location? {
        setPhase(TravelVerificationPhase.LOCATING)
        _uiState.update { it.copy(locationResult = null) }

        val result = currentLocationProvider.getCurrentLocation()
        _uiState.update { it.copy(locationResult = result) }

        return when (result) {
            is CurrentLocationResult.Success -> {
                setPhase(TravelVerificationPhase.READY)
                result.location
            }

            else -> {
                val message = when (result) {
                    CurrentLocationResult.PermissionRequired ->
                        "방문 인증을 위해 위치 권한이 필요합니다."

                    CurrentLocationResult.LocationDisabled ->
                        "기기의 위치 서비스를 켜주세요."

                    CurrentLocationResult.Unavailable ->
                        "현재 위치를 확인하지 못했습니다. 다시 시도해주세요."

                    is CurrentLocationResult.Success -> return result.location
                }

                showError(message, TravelVerificationRetryAction.LOAD_LOCATION)
                null
            }
        }
    }

    private suspend fun submitVerification() {
        val state = _uiState.value
        val travelId = currentTravelId ?: return
        val spotId = currentSpotId ?: return

        if (state.travel == null || state.targetSpot == null) return
        if (state.certification != null) return

        // 인증 직전에도 권한, 위치 서비스와 현재 좌표를 다시 확인합니다.
        val location = loadLocation() ?: return

        setPhase(TravelVerificationPhase.SUBMITTING)

        val certification = travelRecordRepository.verifyTravelSpot(
            travelId = travelId,
            spotId = spotId,
            latitude = location.latitude,
            longitude = location.longitude,
            visitedAt = Clock.System.now().toLocalDateTime(verificationTimeZone),
            accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
        )

        // 이후 조회가 실패해도 성공한 POST 결과는 유지합니다.
        _uiState.update { it.copy(certification = certification) }

        refreshTravel()
    }

    private suspend fun refreshTravel() {
        val travelId = currentTravelId ?: return
        val spotId = currentSpotId ?: return

        setPhase(TravelVerificationPhase.REFRESHING_TRAVEL)

        val travel = travelRepository.getTravel(travelId)
        val place = travel.days.flatMap { it.places }
            .firstOrNull { it.spot.id == spotId }

        if (place?.isCertified != true) {
            if (_uiState.value.certification != null) {
                // POST 성공은 확인됐으므로 진행률 조회만 다시 시도합니다.
                throw InvalidResponseException("Verification is not reflected in travel.")
            }

            showError(
                message = "인증 완료를 확인하지 못했습니다. 위치를 확인한 뒤 다시 인증해주세요.",
                retryAction = TravelVerificationRetryAction.VERIFY_SPOT,
            )
            return
        }

        if (travel.certificationTargetCount <= 0) {
            throw InvalidResponseException("Invalid certification target count.")
        }

        // 인증 개수와 관계없이 D02b 인증 완료 화면을 먼저 표시합니다.
        _uiState.update {
            it.copy(
                travel = travel,
                phase = TravelVerificationPhase.VERIFIED,
                retryAction = null,
                errorMessage = null,
            )
        }
    }

    /** 전체 인증 완료가 확인된 경우에만 스티커를 조회합니다. */
    private suspend fun loadStickerPack() {
        val travel = _uiState.value.travel ?: return
        val isAllCertified = travel.certificationTargetCount > 0 &&
            travel.certifiedSpotsCount == travel.certificationTargetCount

        if (!isAllCertified) return

        setPhase(TravelVerificationPhase.LOADING_STICKERS)

        val stickerPack = stickerRepository.getTravelStickerPack(travel.id)

        _uiState.update {
            it.copy(
                stickerPack = stickerPack,
                phase = if (stickerPack != null) {
                    TravelVerificationPhase.REWARD
                } else {
                    TravelVerificationPhase.STICKER_NOT_GRANTED
                },
                retryAction = if (stickerPack == null) {
                    TravelVerificationRetryAction.LOAD_STICKERS
                } else {
                    null
                },
                errorMessage = null,
            )
        }
    }

    private fun setPhase(phase: TravelVerificationPhase) {
        _uiState.update {
            it.copy(
                phase = phase,
                retryAction = null,
                errorMessage = null,
            )
        }
    }

    private fun showError(
        message: String,
        retryAction: TravelVerificationRetryAction? = null,
    ) {
        _uiState.update {
            it.copy(
                phase = TravelVerificationPhase.ERROR,
                retryAction = retryAction,
                errorMessage = message,
            )
        }
    }
}

/** 서버 내부 오류 문구를 그대로 화면에 노출하지 않습니다. */
private fun Exception.toVerificationErrorMessage(): String = when (this) {
    is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
    is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 다시 시도해주세요."
    else -> "요청을 처리하지 못했습니다. 다시 시도해주세요."
}
