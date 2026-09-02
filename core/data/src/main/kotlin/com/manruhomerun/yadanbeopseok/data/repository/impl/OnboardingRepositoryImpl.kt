package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.mapper.toOnboardingRequestDto
import com.manruhomerun.yadanbeopseok.data.repository.OnboardingRepository
import com.manruhomerun.yadanbeopseok.data.repository.SaveOnboardingParams
import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
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
    private val authTokenDataSource: AuthTokenDataSource,
) : OnboardingRepository {

    /**
     * 서버를 통해 닉네임의 사용 가능 여부를 확인합니다.
     */
    override suspend fun isNicknameAvailable(nickname: String): Boolean {
        val normalizedNickname = nickname.trim()

        return apiCallExecutor.execute {
            userApi.checkNicknameAvailability(nickname = normalizedNickname)
        }.available
    }

    /**
     * 온보딩 정보를 서버에 저장합니다.
     *
     * 서버가 온보딩 완료를 확인한 뒤 DataStore에 저장된
     * 온보딩 완료 상태를 갱신합니다.
     */
    override suspend fun saveOnboarding(
        params: SaveOnboardingParams,
    ) {
        val response =
            apiCallExecutor.execute {
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

        if (!authTokenDataSource.markOnboardingCompleted()) {
            throw SessionExpiredException()
        }
    }
}
