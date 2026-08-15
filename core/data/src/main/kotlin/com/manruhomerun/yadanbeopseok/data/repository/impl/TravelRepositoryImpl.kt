package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelListPage
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import javax.inject.Inject

/**
 * 여행 데이터를 제공하는 Repository 구현체입니다.
 */
internal class TravelRepositoryImpl @Inject constructor(
    private val travelApi: TravelApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelRepository {
    /**
     * 진행 중 여행과 진행 예정 여행 목록을 함께 조회합니다.
     */
    override suspend fun getPlannedTravels(): TravelListPage {
        val response =
            apiCallExecutor.execute {
                travelApi.getTravels(
                    status = "PLANNED",
                )
            }

        return response
            .requireData()
            .toTravelListPage()
    }
}
