package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.data.mapper.toOnboardingRequestDto
import com.manruhomerun.yadanbeopseok.data.repository.OnboardingRepository
import com.manruhomerun.yadanbeopseok.data.repository.SaveOnboardingParams
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.user.api.UserApi
import javax.inject.Inject

/**
 * [OnboardingRepository]의 실제 구현체입니다.
 *
 * 앱에서 입력한 온보딩 정보를 서버 요청 DTO로 변환하고,
 * 야단법석 백엔드에 전달하여 온보딩을 완료합니다.
 */
internal class OnboardingRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val apiCallExecutor: ApiCallExecutor,
) : OnboardingRepository {
    /**
     * 온보딩 정보를 서버에 저장합니다.
     *
     * 네트워크 및 HTTP 오류는 [ApiCallExecutor]가 앱 공통 예외로 변환하고,
     * 성공 응답의 필수 데이터는 requireData를 통해 검증합니다.
     */
    override suspend fun saveOnboarding(
        params: SaveOnboardingParams,
    ) {
        val response = apiCallExecutor.execute {
                userApi.saveOnboarding(
                    request = params.toOnboardingRequestDto(),
                )
            }

        val onboardingResponse = response.requireData()

        if (!onboardingResponse.onboardingCompleted) {
            throw InvalidResponseException(
                message = "Onboarding completion was not confirmed.",
            )
        }
    }
}
