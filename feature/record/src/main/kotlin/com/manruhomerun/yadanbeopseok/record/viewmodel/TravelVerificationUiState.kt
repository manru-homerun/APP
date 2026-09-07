package com.manruhomerun.yadanbeopseok.record.viewmodel

import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationResult

/** D02 위치 확인, D02b 인증 완료, D03 스티커 획득에서 사용하는 상태입니다. */
data class TravelVerificationUiState(
    val travel: Travel? = null,
    val targetSpot: TravelSpotDetail? = null,
    val travelDay: Int? = null,
    val locationResult: CurrentLocationResult? = null,
    val phase: TravelVerificationPhase = TravelVerificationPhase.LOADING,

    /** POST 성공 결과를 유지하여 이후 조회 실패와 인증 실패를 구분합니다. */
    val certification: TravelCertification? = null,

    /** 스티커 조회로 실제 획득이 확인된 경우에만 값을 보관합니다. */
    val stickerPack: StickerPack? = null,

    /** 실패한 작업만 재시도하며, 인증 성공 후에는 POST를 재시도하지 않습니다. */
    val retryAction: TravelVerificationRetryAction? = null,
    val errorMessage: String? = null,
)

/** 권한 요청 UI는 별도 화면이 아니라 Route에서 시스템 권한창으로 처리합니다. */
enum class TravelVerificationPhase {
    LOADING,
    LOCATING,
    READY,
    SUBMITTING,
    REFRESHING_TRAVEL,
    VERIFIED,
    LOADING_STICKERS,
    STICKER_NOT_GRANTED,
    REWARD,
    ERROR,
}

/** 인증 이후의 조회 재시도와 방문 인증 요청을 구분합니다. */
enum class TravelVerificationRetryAction {
    LOAD_CONTENT,
    LOAD_LOCATION,
    VERIFY_SPOT,
    REFRESH_TRAVEL,
    LOAD_STICKERS,
}
