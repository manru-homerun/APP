package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelPreference
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelPreferenceUpdateRequestDto
import com.manruhomerun.yadanbeopseok.data.mapper.toUserProfile
import com.manruhomerun.yadanbeopseok.data.mapper.toUserProfileUpdateRequestDto
import com.manruhomerun.yadanbeopseok.data.repository.UserRepository
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.user.api.UserApi
import javax.inject.Inject

/**
 * 사용자 프로필과 여행 취향 API를 사용하는 Repository 구현체입니다.
 */
internal class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val apiCallExecutor: ApiCallExecutor,
) : UserRepository {
    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     */
    override suspend fun getMyProfile(): UserProfile {
        val response = apiCallExecutor.execute {
            userApi.getMyProfile()
        }

        return response.toUserProfile()
    }

    /**
     * 현재 프로필에 전달받은 변경값을 반영하여 서버에 저장합니다.
     *
     * 서버의 프로필 수정 API가 전체 프로필을 요구하므로 현재 프로필을
     * 먼저 조회한 뒤 변경값을 합쳐 요청합니다.
     */
    override suspend fun updateMyProfile(
        nickname: String?,
        profileImageUrl: String?,
        favoriteTeam: KboTeam?,
    ): UserProfile {
        val currentProfile = getMyProfile()
        val request = currentProfile.toUserProfileUpdateRequestDto(
            updatedNickname = nickname,
            updatedProfileImageUrl = profileImageUrl,
            updatedFavoriteTeam = favoriteTeam,
        )

        apiCallExecutor.execute {
            userApi.updateMyProfile(request = request)
        }

        return currentProfile.copy(
            nickname = request.nickname,
            profileImageUrl = request.profileImageUrl,
            favoriteTeam = favoriteTeam ?: currentProfile.favoriteTeam,
        )
    }

    /**
     * 현재 로그인한 사용자의 여행 취향을 조회합니다.
     */
    override suspend fun getMyTravelPreference(): TravelPreference {
        val response = apiCallExecutor.execute {
            userApi.getMyTravelPreference()
        }

        return response.toTravelPreference()
    }

    /**
     * 변경된 여행 취향을 서버에 저장합니다.
     */
    override suspend fun updateMyTravelPreference(
        preference: TravelPreference,
    ): TravelPreference {
        apiCallExecutor.execute {
            userApi.updateMyTravelPreference(
                request = preference.toTravelPreferenceUpdateRequestDto(),
            )
        }

        return preference
    }
}
