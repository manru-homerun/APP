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
    private var savedPhotoUri: String? = null
    private var savedPhotoAspectRatio: Float? = null
    private var savedStickersInRenderOrder: List<PlacedSticker> = emptyList()

    /** 여행 ID로 획득한 스티커팩을 조회합니다. */
    fun loadStickers(travelId: String) {
        if (isSessionExpired || loadJob?.isActive == true || _uiState.value.isExporting) return

        val normalizedTravelId = travelId.trim()

        if (normalizedTravelId.isEmpty()) {
            loadJob?.cancel()
            currentTravelId = null
            resetSavedPhoto()

            _uiState.value = TravelStickerPhotoUiState(
                isLoading = false,
                errorMessage = "스티커 정보를 확인할 수 없습니다.",
            )
            return
        }

        // 조회 실패 후 화면이 재생성되어도 사진 편집은 유지하고 재조회는 retry에서 처리합니다.
        if (currentTravelId == normalizedTravelId) return

        currentTravelId = normalizedTravelId
        nextPlacedStickerId = 0L
        resetSavedPhoto()
        _uiState.value = TravelStickerPhotoUiState()

        requestStickerPack(normalizedTravelId)
    }

    /** 스티커팩 조회 실패 후 다시 조회합니다. */
    fun retry() {
        if (isSessionExpired || loadJob?.isActive == true) return

        val travelId = currentTravelId ?: return
        requestStickerPack(travelId)
    }

    /** 선택한 사진과 비율을 저장하고 기존 스티커 편집 상태를 초기화합니다. */
    fun selectPhoto(photoUri: String, aspectRatio: Float) {
        val normalizedPhotoUri = photoUri.trim()
        val hasInvalidAspectRatio = !aspectRatio.isFinite() || aspectRatio <= 0f

        if (normalizedPhotoUri.isEmpty() || hasInvalidAspectRatio || _uiState.value.isExporting) return

        nextPlacedStickerId = 0L
        resetSavedPhoto()

        updateEditedPhoto {
            it.copy(
                photoUri = normalizedPhotoUri,
                photoAspectRatio = aspectRatio,
                photoGeometry = null,
                placedStickers = emptyList(),
                selectedStickerId = null,
            )
        }
    }

    /** 현재 사진의 최초 유효 측정만 반영하며 회전과 재측정으로 기준을 바꾸지 않습니다. */
    fun initializePhotoGeometry(photoUri: String, geometry: StickerPhotoGeometry) {
        if (!geometry.isValid) return

        _uiState.update { state ->
            if (state.hasSelectedPhoto && state.photoUri == photoUri && state.photoGeometry == null && !state.isExporting) {
                state.copy(photoGeometry = geometry)
            } else {
                state
            }
        }
    }

    /** 하단 목록에서 선택한 스티커를 사진 중앙에 추가합니다. */
    fun addSticker(sticker: Sticker) {
        val state = _uiState.value
        if (!state.hasSelectedPhoto || state.photoGeometry?.isValid != true || state.isExporting) return

        val availableSticker = state.availableStickers.firstOrNull {
            it.id == sticker.id
        } ?: return

        val placedSticker = PlacedSticker(
            id = ++nextPlacedStickerId,
            sticker = availableSticker,
        )

        updateEditedPhoto {
            it.copy(
                placedStickers = it.placedStickers + placedSticker,
                selectedStickerId = placedSticker.id,
            )
        }
    }

    /** 사진 위에서 편집할 스티커를 선택하거나 선택을 해제합니다. */
    fun selectSticker(stickerId: Long?) {
        if (stickerId == null) {
            updateEditedPhoto {
                it.copy(selectedStickerId = null)
            }
            return
        }

        val hasSticker = _uiState.value.placedStickers.any {
            it.id == stickerId
        }

        if (!hasSticker) return

        updateEditedPhoto {
            it.copy(selectedStickerId = stickerId)
        }
    }

    /** 현재 사진과 사진 위에 배치된 모든 스티커를 초기화합니다. */
    fun clearPhoto() {
        if (_uiState.value.isExporting) return

        nextPlacedStickerId = 0L
        resetSavedPhoto()

        updateEditedPhoto {
            it.copy(
                photoUri = null,
                photoAspectRatio = null,
                photoGeometry = null,
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

        updateEditedPhoto { state ->
            val hasSticker = state.placedStickers.any {
                it.id == stickerId
            }

            if (!hasSticker) {
                return@updateEditedPhoto state
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

        updateEditedPhoto {
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

    /** 갤러리 게시가 성공한 스냅샷만 마지막 저장 내용으로 기록합니다. */
    fun markPhotoSaved(
        photoUri: String,
        aspectRatio: Float,
        placedStickers: List<PlacedSticker>,
        selectedStickerId: Long?,
    ) {
        val state = _uiState.value
        if (!state.isExporting || state.photoUri != photoUri || state.photoAspectRatio != aspectRatio) return

        savedPhotoUri = photoUri
        savedPhotoAspectRatio = aspectRatio
        savedStickersInRenderOrder = placedStickers.inRenderOrder(selectedStickerId)
        _uiState.update { it.withUnsavedChanges() }
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

    /** 저장 중 편집을 막고, 모든 편집 결과에 같은 미저장 판단 기준을 적용합니다. */
    private fun updateEditedPhoto(transform: (TravelStickerPhotoUiState) -> TravelStickerPhotoUiState) {
        _uiState.update { state ->
            if (state.isExporting) state else transform(state).withUnsavedChanges()
        }
    }

    private fun TravelStickerPhotoUiState.withUnsavedChanges(): TravelStickerPhotoUiState = copy(
        hasUnsavedChanges = hasSelectedPhoto && (
            photoUri != savedPhotoUri || photoAspectRatio != savedPhotoAspectRatio ||
                !placedStickers.inRenderOrder(selectedStickerId).matchesSavedStickers()
            ),
    )

    /** 인스턴스 ID가 달라도 실제 이미지와 배치가 같으면 같은 저장 내용입니다. */
    private fun List<PlacedSticker>.matchesSavedStickers(): Boolean =
        size == savedStickersInRenderOrder.size && indices.all { index ->
            val current = this[index]
            val saved = savedStickersInRenderOrder[index]
            current.sticker.imageUrl == saved.sticker.imageUrl &&
                current.centerXFraction == saved.centerXFraction && current.centerYFraction == saved.centerYFraction &&
                current.scale == saved.scale && current.rotationDegrees == saved.rotationDegrees
        }

    /** 새로운 사진 편집을 시작할 때 이전 사진의 저장 기준을 제거합니다. */
    private fun resetSavedPhoto() {
        savedPhotoUri = null
        savedPhotoAspectRatio = null
        savedStickersInRenderOrder = emptyList()
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
