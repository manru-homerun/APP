package com.manruhomerun.yadanbeopseok.record.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.STICKER_REQUIRED_VERIFIED_SPOT_COUNT
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.model.Travel

/**
 * D·01b 지난 여행 상세 화면에 표시할 상태입니다.
 *
 * 여행과 경기 상세, 지급 기준 도달 후 조회한 스티커팩을 관리합니다.
 */
data class TravelRecordDetailUiState(
    val travel: Travel? = null,
    val baseballGame: BaseballGame? = null,
    val stickerPack: StickerPack? = null,
    val isLoading: Boolean = true,
    val isStickerLoading: Boolean = false,
    val errorMessage: String? = null,
    val stickerErrorMessage: String? = null,
) {
    /** 여행 일정과 야구 경기 정보를 모두 표시할 수 있는 상태입니다. */
    val hasTravelDetail: Boolean
        get() = travel != null && baseballGame != null

    /** 방문 인증 수가 스티커 지급 기준에 도달했는지 나타냅니다. */
    val isStickerRequirementMet: Boolean
        get() = (travel?.verifiedSpotsCount ?: 0) >= STICKER_REQUIRED_VERIFIED_SPOT_COUNT

    /** 서버에서 실제로 사용할 수 있는 스티커를 받은 상태입니다. */
    val hasSticker: Boolean
        get() = stickerPack?.stickers?.isNotEmpty() == true

    /** D·04 사진 꾸미기 화면으로 이동할 수 있는 상태입니다. */
    val canDecoratePhoto: Boolean
        get() = hasSticker
}
