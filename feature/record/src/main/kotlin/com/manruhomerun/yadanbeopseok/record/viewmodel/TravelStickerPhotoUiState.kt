package com.manruhomerun.yadanbeopseok.record.viewmodel

import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.model.StickerPack

/**
 * D04 스티커 사진 편집 화면에서 사용하는 상태입니다.
 */
data class TravelStickerPhotoUiState(
    /** 서버에서 조회한 여행 스티커팩입니다. */
    val stickerPack: StickerPack? = null,

    /** 사용자가 선택한 사진의 URI 문자열입니다. */
    val photoUri: String? = null,

    /** 사진 위에 추가된 스티커 인스턴스 목록입니다. */
    val placedStickers: List<PlacedSticker> = emptyList(),

    /** 현재 편집 대상으로 선택된 스티커 인스턴스 ID입니다. */
    val selectedStickerId: Long? = null,

    /** 스티커팩을 조회 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 완성된 이미지를 저장하거나 공유하는 중인지 나타냅니다. */
    val isExporting: Boolean = false,

    /** 사용자에게 표시할 오류 메시지입니다. */
    val errorMessage: String? = null,
) {
    /** 하단 선택 목록에 표시할 획득 스티커입니다. */
    val availableStickers: List<Sticker>
        get() = stickerPack?.stickers.orEmpty()

    /** 사진을 저장하거나 공유할 수 있는 상태인지 나타냅니다. */
    val canExport: Boolean
        get() = photoUri != null && !isLoading && !isExporting
}

/**
 * 사진 위에 배치된 하나의 스티커 편집 상태입니다.
 *
 * 같은 스티커를 여러 번 추가할 수 있으므로 서버의 스티커 ID와 별도로
 * [id]를 사용해 각 배치 인스턴스를 구분합니다.
 *
 * 위치는 캔버스 크기가 바뀌어도 유지되도록 0부터 1 사이의 비율로 저장합니다.
 */
data class PlacedSticker(
    val id: Long,
    val sticker: Sticker,
    val centerXFraction: Float = 0.5f,
    val centerYFraction: Float = 0.5f,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
)
