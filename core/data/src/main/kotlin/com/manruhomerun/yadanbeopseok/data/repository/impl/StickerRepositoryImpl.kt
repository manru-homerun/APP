package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toStickerPack
import com.manruhomerun.yadanbeopseok.data.repository.StickerRepository
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import javax.inject.Inject

/**
 * 특정 여행에서 획득한 스티커팩과 스티커 목록을 조회합니다.
 *
 * 미획득 상태는 null로 반환하고,
 * 통신 실패나 잘못된 응답은 예외로 전달합니다.
 */
internal class StickerRepositoryImpl @Inject constructor(
    private val travelApi: TravelApi,
    private val apiCallExecutor: ApiCallExecutor,
) : StickerRepository {
    override suspend fun getTravelStickerPack(travelId: String): StickerPack? {
        val response = apiCallExecutor.execute {
            travelApi.getTravelStickerPack(travelId = travelId)
        }

        return response.toStickerPack()
    }
}
