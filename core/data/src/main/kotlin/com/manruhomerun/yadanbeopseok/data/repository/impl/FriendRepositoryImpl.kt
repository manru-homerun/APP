package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toFriends
import com.manruhomerun.yadanbeopseok.data.repository.FriendRepository
import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.friend.api.FriendApi
import javax.inject.Inject

/**
 * 현재 사용자의 친구 목록을 제공하는 Repository 구현체입니다.
 */
internal class FriendRepositoryImpl @Inject constructor(
    private val friendApi: FriendApi,
    private val apiCallExecutor: ApiCallExecutor,
) : FriendRepository {
    /**
     * 친구 목록 API를 호출하고 앱 내부 친구 모델 목록으로 변환합니다.
     */
    override suspend fun getFriends(): List<Friend> {
        val response = apiCallExecutor.execute {
            friendApi.getFriends()
        }

        return response.toFriends()
    }
}
