package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravel
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelCourse
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelCourseAlignRequestDto
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelCourseGenerateRequestDto
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelCreateRequestDto
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelListPage
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelTheme
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelUpdateRequestDto
import com.manruhomerun.yadanbeopseok.data.repository.CreateTravelParams
import com.manruhomerun.yadanbeopseok.data.repository.GenerateTravelCourseParams
import com.manruhomerun.yadanbeopseok.data.repository.TravelRepository
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelQueryStatus
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * 여행 조회, 생성 및 수정에 필요한 데이터를 제공하는 Repository 구현체입니다.
 */
internal class TravelRepositoryImpl @Inject constructor(
    private val travelApi: TravelApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelRepository {
    /** 지정한 상태의 여행 목록 한 페이지를 조회합니다. */
    override suspend fun getTravels(
        status: TravelStatus,
        pageNumber: Int,
        pageSize: Int,
    ): TravelListPage {
        val response = apiCallExecutor.execute {
            travelApi.getTravels(
                status = status.toTravelQueryStatus(),
                pageNumber = pageNumber,
                pageSize = pageSize,
            )
        }

        return response.toTravelListPage()
    }

    /**
     * 여행 ID에 해당하는 상세 일정과 방문 인증 상태를 조회합니다.
     */
    override suspend fun getTravel(travelId: String): Travel {
        val response = apiCallExecutor.execute {
            travelApi.getTravel(travelId = travelId)
        }

        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return response.toTravel(
            travelId = travelId,
            currentDate = currentDate,
        )
    }

    /**
     * 여행 만들기에서 선택할 수 있는 여행 테마 목록을 조회합니다.
     */
    override suspend fun getTravelThemes(): List<TravelTheme> {
        val response = apiCallExecutor.execute {
            travelApi.getTravelThemes()
        }

        return response.map { dto ->
            dto.toTravelTheme()
        }
    }

    /**
     * 여행 만들기에서 선택한 조건으로 최초 여행 코스를 생성합니다.
     */
    override suspend fun generateTravelCourse(params: GenerateTravelCourseParams): TravelCourse {
        val request = params.toTravelCourseGenerateRequestDto()

        val response = apiCallExecutor.execute {
            travelApi.generateTravelCourse(request = request)
        }

        return response.toTravelCourse()
    }

    /**
     * 저장 전 여행 코스의 관광지 순서를 거리 기준으로 재정렬합니다.
     */
    override suspend fun alignTravelCourse(
        startDate: LocalDate,
        endDate: LocalDate,
        course: TravelCourse,
    ): TravelCourse {
        val request = course.toTravelCourseAlignRequestDto(
            startDate = startDate,
            endDate = endDate,
        )

        val response = apiCallExecutor.execute {
            travelApi.alignTravelCourse(request = request)
        }

        return response.toTravelCourse()
    }

    /**
     * 사용자가 최종 확정한 여행 코스를 서버에 저장합니다.
     */
    override suspend fun createTravel(params: CreateTravelParams) {
        val request = params.toTravelCreateRequestDto()

        apiCallExecutor.execute {
            travelApi.createTravel(request = request)
        }
    }

    /**
     * 저장된 여행의 이름, 야구 경기 배치와 일차별 관광지 일정을 수정합니다.
     */
    override suspend fun updateTravel(
        travelId: String,
        name: String,
        course: TravelCourse,
    ) {
        val request = course.toTravelUpdateRequestDto(name = name)

        apiCallExecutor.execute {
            travelApi.updateTravel(
                travelId = travelId,
                request = request,
            )
        }
    }
}

private fun TravelStatus.toTravelQueryStatus(): TravelQueryStatus =
    when (this) {
        TravelStatus.UPCOMING -> TravelQueryStatus.PLANNING
        TravelStatus.ACTIVE -> TravelQueryStatus.IN_PROGRESS
        TravelStatus.COMPLETED -> TravelQueryStatus.COMPLETED
    }
