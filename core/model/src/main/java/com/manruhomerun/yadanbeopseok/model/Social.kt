package com.manruhomerun.yadanbeopseok.model

/**
 * 앱 내부에서 사용하는 친구 모델입니다.
 *
 * ERD의 friends 테이블을 화면에서 쓰기 좋게 UserProfile과 묶은 형태입니다.
 */
data class Friend(val id: String, val user: UserProfile)

/**
 * 현재 사용자의 친구 목록과 카운트입니다.
 */
data class FriendList(val friends: List<Friend>, val friendCount: Long, val receivedRequestCount: Long)

/**
 * 받은 요청과 보낸 요청에 표시할 상대방 정보입니다.
 */
data class FriendRequest(val id: String, val user: UserProfile)

/**
 * 친구 요청 목록과 서버가 반환한 카운트입니다.
 */
data class FriendRequestList(
    val requests: List<FriendRequest>,
    val friendCount: Long,
    val receivedRequestCount: Long,
    val sentRequestCount: Long,
)

/**
 * 닉네임 검색으로 찾은 사용자와 현재 친구 관계입니다.
 */
data class FriendSearchUser(val user: UserProfile, val relationshipStatus: FriendRelationshipStatus)

/**
 * 사용자 검색 결과입니다.
 */
data class FriendSearchResult(val resultCount: Int, val users: List<FriendSearchUser>)

/**
 * 현재 사용자와 검색된 사용자 사이의 친구 관계입니다.
 */
enum class FriendRelationshipStatus {
    NONE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    FRIEND,
    UNKNOWN,
}
