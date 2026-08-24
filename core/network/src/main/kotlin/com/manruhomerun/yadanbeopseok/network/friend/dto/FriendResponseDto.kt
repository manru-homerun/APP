package com.manruhomerun.yadanbeopseok.network.friend.dto

import kotlinx.serialization.Serializable

/**
 * 현재 로그인한 사용자의 친구 목록 응답 DTO입니다.
 *
 * @property friendCount 전체 친구 수
 * @property receivedRequestCount 받은 대기 친구 요청 수
 * @property friends 현재 사용자의 친구 목록
 */
@Serializable
data class FriendListResponseDto(
    val friendCount: Long,
    val receivedRequestCount: Long,
    val friends: List<FriendResponseDto>,
)

/**
 * 친구 목록에 포함되는 개별 친구 응답 DTO입니다.
 *
 * @property friendId 친구 관계 ID
 * @property userId 친구 사용자의 ID
 * @property nickname 친구 닉네임
 * @property profileImageUrl 친구 프로필 이미지 URL
 * @property favoriteTeamId 친구가 응원하는 구단 ID
 * @property favoriteTeamName 친구가 응원하는 구단명
 */
@Serializable
data class FriendResponseDto(
    val friendId: Long,
    val userId: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val favoriteTeamId: Long,
    val favoriteTeamName: String,
)
