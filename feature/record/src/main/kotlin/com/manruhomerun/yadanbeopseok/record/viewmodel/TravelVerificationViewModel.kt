package com.manruhomerun.yadanbeopseok.record.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.ApiException
import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.StickerRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRecordRepository
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.model.STICKER_REQUIRED_VERIFIED_SPOT_COUNT
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationProvider
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 방문 인증에 필요한 정보와 위치를 조회하고,
 * 인증 요청, 진행률 갱신, 스티커 조회를 순서대로 처리합니다.
 *
 * 시스템 권한 요청과 실제 화면 이동은 Route에서 처리합니다.
 */
@HiltViewModel
class TravelVerificationViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val travelRecordRepository: TravelRecordRepository,
    private val stickerRepository: StickerRepository,
    private val currentLocationProvider: CurrentLocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelVerificationUiState())
    val uiState = _uiState.asStateFlow()

    private var currentTravelId: String? = null
    private var currentSpotId: String? = null
    private var requestJob: Job? = null
    private var isSessionExpired = false

    /** 동일한 인증 화면이 재구성되어도 초기 조회를 반복하지 않습니다. */
    fun loadVerification(travelId: String, spotId: String) {
        if (isSessionExpired || requestJob?.isActive == true) return
        if (_uiState.value.verificationResult != null) return

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
        if (state.verificationResult != null) return

        val canRefresh = state.phase == TravelVerificationPhase.READY ||
            state.retryAction == TravelVerificationRetryAction.LOAD_LOCATION

        if (!canRefresh) return

        request(TravelVerificationRetryAction.LOAD_LOCATION)
    }

    /** 인증 버튼에서 호출하며, 요청 중 중복 호출은 차단합니다. */
    fun verifySpot() {
        val state = _uiState.value

        if (state.phase != TravelVerificationPhase.READY) return
        if (state.verificationResult != null) return
        val travel = state.travel ?: return
        if (blockAdditionalVerification(travel)) return

        request(TravelVerificationRetryAction.VERIFY_SPOT)
    }

    /** 실패한 작업만 재시도하며 자동으로 POST를 반복하지 않습니다. */
    fun retry() {
        val action = _uiState.value.retryAction ?: return
        request(action)
    }

    /**
     * D02b 표시가 끝난 뒤 방문 인증 수가 지급 기준에 도달하면 스티커를 조회합니다.
     *
     * 지급 기준에 도달하지 않은 경우에는 Route가 일정 화면으로 돌아가므로
     * 이 함수에서 별도의 작업을 실행하지 않습니다.
     */
    fun loadStickerReward() {
        val state = _uiState.value
        val travel = state.travel ?: return

        val hasReachedStickerRequirement = travel.verifiedSpotsCount >= STICKER_REQUIRED_VERIFIED_SPOT_COUNT

        if (state.phase != TravelVerificationPhase.VERIFIED) return
        if (!hasReachedStickerRequirement) return

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
                val failurePhase = _uiState.value.phase
                if (
                    failurePhase == TravelVerificationPhase.SUBMITTING &&
                    exception.isDistanceVerificationFailure(action)
                ) {
                    showError(
                        message = "인증 장소에서 너무 멀리 떨어져 있습니다.\n관광지 근처에서 다시 시도해주세요.",
                        retryAction = TravelVerificationRetryAction.VERIFY_SPOT,
                    )
                    return@launch
                }

                val retryAction = when (failurePhase) {
                    TravelVerificationPhase.LOADING ->
                        TravelVerificationRetryAction.LOAD_CONTENT

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

        setPhase(TravelVerificationPhase.LOADING)

        val travel = travelRepository.getTravel(travelId)
        val place = updateVerificationTarget(travel) ?: return
        if (!canVerifyTarget(travel, place)) return

        loadLocation()
    }

    /** 최신 여행에서 대상 장소와 일차를 찾아 화면 상태에도 함께 반영합니다. */
    private fun updateVerificationTarget(travel: Travel): TravelPlace? {
        val spotId = currentSpotId ?: return null
        val day = travel.days.firstOrNull { travelDay ->
            travelDay.places.any { place -> place.spot.id == spotId }
        }
        val place = day?.places?.firstOrNull { it.spot.id == spotId }

        _uiState.update {
            it.copy(
                travel = travel,
                targetSpot = place?.spot,
                travelDay = day?.day,
            )
        }

        if (day == null || place == null) {
            showError("이 여행에 포함된 관광지가 아닙니다.")
            return null
        }

        return place
    }

    /** 새 인증 요청에 필요한 여행 상태와 대상 장소, 인증 상한을 확인합니다. */
    private fun canVerifyTarget(travel: Travel, place: TravelPlace): Boolean {
        if (travel.status != TravelStatus.ACTIVE) {
            showError("여행 중에만 방문 인증을 할 수 있습니다.")
            return false
        }

        if (place.spot.category == TravelSpotCategory.STADIUM) {
            showError("야구 경기는 방문 인증 대상이 아닙니다.")
            return false
        }

        if (place.isVerified) {
            showError("이미 방문 인증을 완료한 관광지입니다.")
            return false
        }

        return !blockAdditionalVerification(travel)
    }

    /** 추가 POST만 차단하며 인증 결과와 스티커 조회 재시도에는 적용하지 않습니다. */
    private fun blockAdditionalVerification(travel: Travel): Boolean {
        if (travel.verifiedSpotsCount < STICKER_REQUIRED_VERIFIED_SPOT_COUNT) return false

        showError("이 여행은 ${STICKER_REQUIRED_VERIFIED_SPOT_COUNT}곳의 방문 인증을 완료해 추가로 인증할 수 없습니다.")
        return true
    }

    /** 위치를 확보해도 인증 성공으로 처리하지 않습니다. */
    private suspend fun loadLocation(): Location? {
        setPhase(TravelVerificationPhase.LOCATING)
        _uiState.update { it.copy(locationResult = null) }

        val result = currentLocationProvider.getCurrentLocation()
        _uiState.update { it.copy(locationResult = result) }

        return when (result) {
            is CurrentLocationResult.Success -> {
                if (!result.location.hasValidVerificationAccuracy()) {
                    showError(
                        message = "현재 위치의 정확도를 확인하지 못했습니다. 다시 시도해주세요.",
                        retryAction = TravelVerificationRetryAction.LOAD_LOCATION,
                    )
                    return null
                }

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
        if (state.verificationResult != null) return

        setPhase(TravelVerificationPhase.LOADING)
        val travel = travelRepository.getTravel(travelId)
        val travelPlace = updateVerificationTarget(travel) ?: return

        // 화면을 열어 둔 사이 인증이 반영됐다면 추가 POST 없이 완료 처리합니다.
        if (travelPlace.isVerified && travelPlace.spot.category != TravelSpotCategory.STADIUM) {
            showVerificationCompleted(travel)
            return
        }

        if (!canVerifyTarget(travel, travelPlace)) return
        val travelDay = _uiState.value.travelDay ?: throw InvalidResponseException("Unexpected travel day.")

        // 인증 직전에도 권한, 위치 서비스와 현재 좌표를 다시 확인합니다.
        val location = loadLocation() ?: return

        setPhase(TravelVerificationPhase.SUBMITTING)

        val verificationResult = travelRecordRepository.verifyTravelSpot(
            travelId = travelId,
            spotId = spotId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy.toDouble(),
            day = travelDay,
            placementOrder = travelPlace.order,
        )

        // 이후 조회가 실패해도 성공한 POST 결과는 유지합니다.
        _uiState.update { it.copy(verificationResult = verificationResult) }

        refreshTravel()
    }

    private suspend fun refreshTravel() {
        val travelId = currentTravelId ?: return
        val spotId = currentSpotId ?: return

        setPhase(TravelVerificationPhase.REFRESHING_TRAVEL)

        val travel = travelRepository.getTravel(travelId)
        val place = travel.days.flatMap { it.places }
            .firstOrNull { it.spot.id == spotId }

        _uiState.update { it.copy(travel = travel) }

        if (place?.isVerified != true) {
            if (_uiState.value.verificationResult != null) {
                // POST 성공은 확인됐으므로 진행률 조회만 다시 시도합니다.
                throw InvalidResponseException("Verification is not reflected in travel.")
            }

            if (blockAdditionalVerification(travel)) return

            showError(
                message = "인증 완료를 확인하지 못했습니다. 위치를 확인한 뒤 다시 인증해주세요.",
                retryAction = TravelVerificationRetryAction.VERIFY_SPOT,
            )
            return
        }

        showVerificationCompleted(travel)
    }

    /** 성공 확인은 인증 상한보다 우선하며, 기존 완료·스티커 화면 전환을 유지합니다. */
    private fun showVerificationCompleted(travel: Travel) {
        _uiState.update {
            it.copy(
                travel = travel,
                phase = TravelVerificationPhase.VERIFIED,
                retryAction = null,
                errorMessage = null,
            )
        }
    }

    /** 방문 인증 수가 지급 기준에 도달한 경우에만 스티커를 조회합니다. */
    private suspend fun loadStickerPack() {
        val travel = _uiState.value.travel ?: return
        val hasReachedStickerRequirement = travel.verifiedSpotsCount >= STICKER_REQUIRED_VERIFIED_SPOT_COUNT

        if (!hasReachedStickerRequirement) return

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

/** 방문 인증 API에 전달할 수 있는 위치 정확도인지 확인합니다. */
private fun Location.hasValidVerificationAccuracy(): Boolean {
    return hasAccuracy() && accuracy.isFinite() && accuracy >= 0f
}

/** 방문 인증 요청에서 인증 범위 초과를 포함하는 400 응답인지 확인합니다. */
private fun Exception.isDistanceVerificationFailure(action: TravelVerificationRetryAction): Boolean {
    return action == TravelVerificationRetryAction.VERIFY_SPOT &&
        this is ApiException &&
        statusCode == HTTP_BAD_REQUEST
}

/** 서버 내부 오류 문구를 그대로 화면에 노출하지 않습니다. */
private fun Exception.toVerificationErrorMessage(): String = when (this) {
    is NetworkConnectionException -> "인터넷 연결을 확인한 후 다시 시도해주세요."
    is NetworkTimeoutException -> "서버 응답이 지연되고 있습니다. 다시 시도해주세요."
    else -> "요청을 처리하지 못했습니다. 다시 시도해주세요."
}
