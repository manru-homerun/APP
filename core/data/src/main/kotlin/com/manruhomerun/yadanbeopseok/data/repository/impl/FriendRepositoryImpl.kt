package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toFriendList
import com.manruhomerun.yadanbeopseok.data.mapper.toFriendRequestList
import com.manruhomerun.yadanbeopseok.data.mapper.toFriendSearchResult
import com.manruhomerun.yadanbeopseok.data.mapper.toRequestId
import com.manruhomerun.yadanbeopseok.data.repository.FriendRepository
import com.manruhomerun.yadanbeopseok.model.FriendList
import com.manruhomerun.yadanbeopseok.model.FriendRequestList
import com.manruhomerun.yadanbeopseok.model.FriendSearchResult
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.friend.api.FriendApi
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendRequestCreateRequestDto
import com.manruhomerun.yadanbeopseok.network.user.api.UserApi
import javax.inject.Inject

/**
 * 현재 사용자의 친구 목록을 제공하는 Repository 구현체입니다.
 */
internal class FriendRepositoryImpl @Inject constructor(
    private val friendApi: FriendApi,
    private val userApi: UserApi,
    private val apiCallExecutor: ApiCallExecutor,
) : FriendRepository {
    /**
     * 친구 목록 API를 호출하고 앱 내부 친구 모델 목록으로 변환합니다.
     */
    override suspend fun getFriends(): FriendList {
        val response = apiCallExecutor.execute {
            friendApi.getFriends()
        }

        return response.toFriendList()
    }

    override suspend fun getReceivedFriendRequests(): FriendRequestList {
        val response = apiCallExecutor.execute {
            friendApi.getReceivedFriendRequests()
        }

        return response.toFriendRequestList()
    }

    override suspend fun getSentFriendRequests(): FriendRequestList {
        val response = apiCallExecutor.execute {
            friendApi.getSentFriendRequests()
        }

        return response.toFriendRequestList()
    }

    override suspend fun searchUsers(nickname: String, limit: Int): FriendSearchResult {
        val response = apiCallExecutor.execute {
            userApi.searchUsers(
                nickname = nickname,
                limit = limit,
            )
        }

        return response.toFriendSearchResult()
    }

    override suspend fun sendFriendRequest(receiverUserId: String) {
        apiCallExecutor.execute {
            friendApi.sendFriendRequest(
                request = FriendRequestCreateRequestDto(receiverUserId),
            )
        }
    }

    override suspend fun acceptFriendRequest(requestId: String) {
        apiCallExecutor.execute {
            friendApi.acceptFriendRequest(requestId.toRequestId("requestId"))
        }
    }

    override suspend fun rejectFriendRequest(requestId: String) {
        apiCallExecutor.execute {
            friendApi.rejectFriendRequest(requestId.toRequestId("requestId"))
        }
    }

    override suspend fun cancelFriendRequest(requestId: String) {
        apiCallExecutor.execute {
            friendApi.cancelFriendRequest(requestId.toRequestId("requestId"))
        }
    }

    override suspend fun deleteFriend(friendId: String) {
        apiCallExecutor.execute {
            friendApi.deleteFriend(friendId.toRequestId("friendId"))
        }
    }
}
