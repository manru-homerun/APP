package com.manruhomerun.yadanbeopseok.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manruhomerun.yadanbeopseok.common.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.repository.StickerRepository
import com.manruhomerun.yadanbeopseok.model.Sticker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * D04의 스티커 조회, 사진 선택과 스티커 편집 상태를 관리합니다.
 *
 * 시스템 Photo Picker 실행과 완성된 이미지 저장·공유는 Route에서 처리합니다.
 */
@HiltViewModel
class TravelStickerPhotoViewModel @Inject constructor(
    private val stickerRepository: StickerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelStickerPhotoUiState())
    val uiState = _uiState.asStateFlow()

    private var currentTravelId: String? = null
    private var loadJob: Job? = null
    private var nextPlacedStickerId = 0L
    private var isSessionExpired = false

    /** 여행 ID로 획득한 스티커팩을 조회합니다. */
    fun loadStickers(travelId: String) {
        if (isSessionExpired || loadJob?.isActive == true) return

        val normalizedTravelId = travelId.trim()

        if (normalizedTravelId.isEmpty()) {
            loadJob?.cancel()
            currentTravelId = null

            _uiState.value = TravelStickerPhotoUiState(
                isLoading = false,
                errorMessage = "스티커 정보를 확인할 수 없습니다.",
            )
            return
        }

        val isAlreadyLoaded = currentTravelId == normalizedTravelId &&
            _uiState.value.stickerPack != null

        if (isAlreadyLoaded) return

        currentTravelId = normalizedTravelId
        nextPlacedStickerId = 0L
        _uiState.value = TravelStickerPhotoUiState()

        requestStickerPack(normalizedTravelId)
    }

    /** 스티커팩 조회 실패 후 다시 조회합니다. */
    fun retry() {
        if (isSessionExpired || loadJob?.isActive == true) return

        val travelId = currentTravelId ?: return
        requestStickerPack(travelId)
    }

    /** 선택한 사진을 저장하고 기존 스티커 편집 상태를 초기화합니다. */
    fun selectPhoto(photoUri: String) {
        val normalizedPhotoUri = photoUri.trim()
        if (normalizedPhotoUri.isEmpty()) return

        nextPlacedStickerId = 0L

        _uiState.update {
            it.copy(
                photoUri = normalizedPhotoUri,
                placedStickers = emptyList(),
                selectedStickerId = null,
                errorMessage = null,
            )
        }
    }

    /** 하단 목록에서 선택한 스티커를 사진 중앙에 추가합니다. */
    fun addSticker(sticker: Sticker) {
        val state = _uiState.value
        if (state.photoUri == null) return

        val availableSticker = state.availableStickers.firstOrNull {
            it.id == sticker.id
        } ?: return

        val placedSticker = PlacedSticker(
            id = ++nextPlacedStickerId,
            sticker = availableSticker,
        )

        _uiState.update {
            it.copy(
                placedStickers = it.placedStickers + placedSticker,
                selectedStickerId = placedSticker.id,
            )
        }
    }

    /** 사진 위에서 편집할 스티커를 선택하거나 선택을 해제합니다. */
    fun selectSticker(stickerId: Long?) {
        if (stickerId == null) {
            _uiState.update {
                it.copy(selectedStickerId = null)
            }
            return
        }

        val hasSticker = _uiState.value.placedStickers.any {
            it.id == stickerId
        }

        if (!hasSticker) return

        _uiState.update {
            it.copy(selectedStickerId = stickerId)
        }
    }

    /** 현재 사진과 사진 위에 배치된 모든 스티커를 초기화합니다. */
    fun clearPhoto() {
        nextPlacedStickerId = 0L

        _uiState.update {
            it.copy(
                photoUri = null,
                placedStickers = emptyList(),
                selectedStickerId = null,
            )
        }
    }

    /** 드래그, 확대·축소와 회전 결과를 스티커에 반영합니다. */
    fun updateStickerTransform(
        stickerId: Long,
        panXFraction: Float,
        panYFraction: Float,
        zoomChange: Float,
        rotationChange: Float,
    ) {
        val hasInvalidValue = !panXFraction.isFinite() ||
            !panYFraction.isFinite() ||
            !zoomChange.isFinite() ||
            !rotationChange.isFinite() ||
            zoomChange <= 0f

        if (hasInvalidValue) return

        _uiState.update { state ->
            val hasSticker = state.placedStickers.any {
                it.id == stickerId
            }

            if (!hasSticker) {
                return@update state
            }

            val updatedStickers = state.placedStickers.map { placedSticker ->
                if (placedSticker.id != stickerId) {
                    placedSticker
                } else {
                    placedSticker.copy(
                        centerXFraction = (
                            placedSticker.centerXFraction + panXFraction
                            ).coerceIn(0f, 1f),
                        centerYFraction = (
                            placedSticker.centerYFraction + panYFraction
                            ).coerceIn(0f, 1f),
                        scale = (
                            placedSticker.scale * zoomChange
                            ).coerceIn(
                                MIN_STICKER_SCALE,
                                MAX_STICKER_SCALE,
                            ),
                        rotationDegrees = (
                            placedSticker.rotationDegrees + rotationChange
                            ).normalizeDegrees(),
                    )
                }
            }

            state.copy(
                placedStickers = updatedStickers,
                selectedStickerId = stickerId,
            )
        }
    }

    /** 현재 선택된 스티커를 사진에서 제거합니다. */
    fun deleteSelectedSticker() {
        val selectedStickerId = _uiState.value.selectedStickerId ?: return

        _uiState.update {
            it.copy(
                placedStickers = it.placedStickers.filterNot { placedSticker ->
                    placedSticker.id == selectedStickerId
                },
                selectedStickerId = null,
            )
        }
    }

    /** 사진 저장 또는 공유 작업을 시작합니다. */
    fun startExport() {
        _uiState.update {
            if (it.canExport) {
                it.copy(
                    isExporting = true,
                    errorMessage = null,
                )
            } else {
                it
            }
        }
    }

    /** 사진 저장 또는 공유 작업을 종료합니다. */
    fun finishExport(errorMessage: String? = null) {
        _uiState.update {
            it.copy(
                isExporting = false,
                errorMessage = errorMessage,
            )
        }
    }

    /** 화면에 표시한 오류 메시지를 제거합니다. */
    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun requestStickerPack(travelId: String) {
        loadJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val stickerPack =
                    stickerRepository.getTravelStickerPack(travelId)

                val hasStickers =
                    stickerPack?.stickers?.isNotEmpty() == true

                _uiState.update {
                    it.copy(
                        stickerPack = if (hasStickers) {
                            stickerPack
                        } else {
                            null
                        },
                        isLoading = false,
                        errorMessage = if (hasStickers) {
                            null
                        } else {
                            "이 여행에서 획득한 스티커가 없습니다."
                        },
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: SessionExpiredException) {
                isSessionExpired = true
                _uiState.update { it.copy(isLoading = false, errorMessage = null)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.toStickerPhotoErrorMessage(),
                    )
                }
            }
        }
    }
}

private fun Float.normalizeDegrees(): Float {
    val normalized = this % FULL_ROTATION_DEGREES

    return if (normalized < 0f) {
        normalized + FULL_ROTATION_DEGREES
    } else {
        normalized
    }
}

/** 서버 내부 오류 문구를 화면에 직접 노출하지 않습니다. */
private fun Exception.toStickerPhotoErrorMessage(): String = when (this) {
    is NetworkConnectionException ->
        "인터넷 연결을 확인한 후 다시 시도해주세요."

    is NetworkTimeoutException ->
        "서버 응답이 지연되고 있습니다. 다시 시도해주세요."

    else ->
        "스티커 정보를 불러오지 못했습니다. 다시 시도해주세요."
}

private const val MIN_STICKER_SCALE = 0.4f
private const val MAX_STICKER_SCALE = 3f
private const val FULL_ROTATION_DEGREES = 360f
