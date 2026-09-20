package com.manruhomerun.yadanbeopseok.network.friend.api

import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendListResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendRequestCreateRequestDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.ReceivedFriendRequestListResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.SentFriendRequestListResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 야단법석 백엔드의 친구 목록 API를 정의합니다.
 *
 * 인증이 필요한 요청이므로 AuthInterceptor가 야단법석 Access Token을
 * Authorization 헤더에 자동으로 추가합니다.
 */
interface FriendApi {
    /**
     * 현재 로그인한 사용자의 친구 목록을 조회합니다.
     */
    @GET("users/me/friends")
    suspend fun getFriends(): FriendListResponseDto

    /**
     * 선택한 친구 관계를 삭제합니다.
     */
    @DELETE("users/me/friends/{friendId}")
    suspend fun deleteFriend(@Path("friendId") friendId: Long)

    /**
     * 현재 사용자가 받은 대기 중인 친구 요청을 조회합니다.
     */
    @GET("users/me/friend-requests/received")
    suspend fun getReceivedFriendRequests(): ReceivedFriendRequestListResponseDto

    /**
     * 현재 사용자가 보낸 대기 중인 친구 요청을 조회합니다.
     */
    @GET("users/me/friend-requests/sent")
    suspend fun getSentFriendRequests(): SentFriendRequestListResponseDto

    /**
     * 선택한 사용자에게 친구 요청을 전송합니다.
     */
    @POST("users/me/friend-requests")
    suspend fun sendFriendRequest(@Body request: FriendRequestCreateRequestDto)

    /**
     * 받은 친구 요청을 수락합니다.
     */
    @PATCH("users/me/friend-requests/{requestId}/accept")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: Long)

    /**
     * 받은 친구 요청을 거절합니다.
     */
    @PATCH("users/me/friend-requests/{requestId}/reject")
    suspend fun rejectFriendRequest(@Path("requestId") requestId: Long)

    /**
     * 보낸 친구 요청을 취소합니다.
     */
    @PATCH("users/me/friend-requests/{requestId}/cancel")
    suspend fun cancelFriendRequest(@Path("requestId") requestId: Long)
}
