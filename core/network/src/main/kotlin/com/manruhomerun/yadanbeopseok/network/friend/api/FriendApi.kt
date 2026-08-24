package com.manruhomerun.yadanbeopseok.network.friend.api

import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendListResponseDto
import retrofit2.http.GET

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
}
