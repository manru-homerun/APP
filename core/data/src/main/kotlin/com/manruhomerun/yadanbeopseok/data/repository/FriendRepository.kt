package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.model.FriendRequest
import com.manruhomerun.yadanbeopseok.model.UserProfile

interface FriendRepository {
    /**
     * 닉네임을 기준으로 친구 신청 가능한 사용자를 검색합니다.
     */
    suspend fun searchUsers(
        keyword: String,
    ): List<UserProfile>

    /**
     * 현재 로그인한 사용자의 친구 목록을 조회합니다.
     */
    suspend fun getFriends(): List<Friend>

    /**
     * 내가 받은 친구 요청 목록을 조회합니다.
     */
    suspend fun getReceivedFriendRequests(): List<FriendRequest>

    /**
     * 내가 보낸 친구 요청 목록을 조회합니다.
     */
    suspend fun getSentFriendRequests(): List<FriendRequest>

    /**
     * 특정 사용자에게 친구 요청을 보냅니다.
     */
    suspend fun requestFriend(
        userId: String,
    ): FriendRequest

    /**
     * 받은 친구 요청을 수락합니다.
     */
    suspend fun acceptFriendRequest(
        requestId: String,
    ): Friend

    /**
     * 받은 친구 요청을 거절합니다.
     */
    suspend fun rejectFriendRequest(
        requestId: String,
    )

    /**
     * 친구 관계를 삭제합니다.
     */
    suspend fun deleteFriend(
        friendId: String,
    )
}
