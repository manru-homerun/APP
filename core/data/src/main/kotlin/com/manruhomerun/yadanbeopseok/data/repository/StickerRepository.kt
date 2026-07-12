package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.StickerPack

interface StickerRepository {
    /**
     * 특정 여행에서 획득한 스티커 팩 목록을 조회합니다.
     */
    suspend fun getTravelStickerPacks(
        travelId: String,
    ): List<StickerPack>

    /**
     * 특정 시즌에 획득했거나 획득 가능한 스티커 팩 목록을 조회합니다.
     */
    suspend fun getSeasonStickerPacks(
        year: Int,
    ): List<StickerPack>

    /**
     * 특정 스티커 팩의 상세 정보와 스티커 목록을 조회합니다.
     */
    suspend fun getStickerPack(
        stickerPackId: String,
    ): StickerPack
}
