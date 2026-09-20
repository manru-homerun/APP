package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.FriendList
import com.manruhomerun.yadanbeopseok.model.FriendRequestList
import com.manruhomerun.yadanbeopseok.model.FriendSearchResult

/**
 * 친구 목록, 요청과 사용자 검색 기능을 제공하는 Repository 계약입니다.
 */
interface FriendRepository {
    /**
     * 현재 로그인한 사용자의 친구 목록을 조회합니다.
     */
    suspend fun getFriends(): FriendList

    /** 현재 사용자가 받은 대기 중인 친구 요청을 조회합니다. */
    suspend fun getReceivedFriendRequests(): FriendRequestList

    /** 현재 사용자가 보낸 대기 중인 친구 요청을 조회합니다. */
    suspend fun getSentFriendRequests(): FriendRequestList

    /** 닉네임으로 사용자를 검색합니다. */
    suspend fun searchUsers(nickname: String, limit: Int): FriendSearchResult

    /** 선택한 사용자에게 친구 요청을 전송합니다. */
    suspend fun sendFriendRequest(receiverUserId: String)

    /** 받은 친구 요청을 수락합니다. */
    suspend fun acceptFriendRequest(requestId: String)

    /** 받은 친구 요청을 거절합니다. */
    suspend fun rejectFriendRequest(requestId: String)

    /** 보낸 친구 요청을 취소합니다. */
    suspend fun cancelFriendRequest(requestId: String)

    /** 선택한 친구 관계를 삭제합니다. */
    suspend fun deleteFriend(friendId: String)
}
