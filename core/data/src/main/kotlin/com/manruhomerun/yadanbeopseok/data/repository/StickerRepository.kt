package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.StickerPack

/**
 * 여행에서 획득한 스티커 정보를 조회합니다.
 */
interface StickerRepository {
    /**
     * 특정 여행에서 획득한 스티커팩과 소속 스티커 목록을 조회합니다.
     *
     * 획득한 스티커팩이 없으면 null을 반환합니다.
     * 통신 실패는 미획득 상태와 구분하여 예외로 전달합니다.
     *
     * @param travelId 스티커를 조회할 여행 ID
     * @return 획득한 스티커팩. 미획득 상태라면 null
     */
    suspend fun getTravelStickerPack(travelId: String): StickerPack?
}
