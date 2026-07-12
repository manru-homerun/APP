package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 친구 모델입니다.
 *
 * ERD의 friends 테이블을 화면에서 쓰기 좋게 UserProfile과 묶은 형태입니다.
 */
data class Friend(
    val id: String,
    val user: UserProfile,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

/**
 * 앱 내부에서 사용하는 친구 요청 모델입니다.
 *
 * 받은 요청, 보낸 요청 화면에서 공통으로 사용할 수 있습니다.
 */
data class FriendRequest(
    val id: String,
    val requester: UserProfile,
    val receiver: UserProfile,
    val status: FriendRequestStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}
