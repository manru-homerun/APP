package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.model.FriendList
import com.manruhomerun.yadanbeopseok.model.FriendRelationshipStatus
import com.manruhomerun.yadanbeopseok.model.FriendRequest
import com.manruhomerun.yadanbeopseok.model.FriendRequestList
import com.manruhomerun.yadanbeopseok.model.FriendSearchResult
import com.manruhomerun.yadanbeopseok.model.FriendSearchUser
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendListResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendRequestItemResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.ReceivedFriendRequestListResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.SentFriendRequestListResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserSearchItemResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserSearchResponseDto

/**
 * 친구 목록 응답을 앱 내부 친구 목록과 카운트로 변환합니다.
 */
internal fun FriendListResponseDto.toFriendList(): FriendList = FriendList(
    friends = friends.map(FriendResponseDto::toFriend),
    friendCount = friendCount,
    receivedRequestCount = receivedRequestCount,
)

/**
 * 받은 친구 요청 응답을 앱 내부 요청 목록으로 변환합니다.
 */
internal fun ReceivedFriendRequestListResponseDto.toFriendRequestList(): FriendRequestList = FriendRequestList(
    requests = receivedRequests.map(FriendRequestItemResponseDto::toFriendRequest),
    friendCount = friendCount,
    receivedRequestCount = receivedRequestCount,
    sentRequestCount = sentRequestCount,
)

/**
 * 보낸 친구 요청 응답을 앱 내부 요청 목록으로 변환합니다.
 */
internal fun SentFriendRequestListResponseDto.toFriendRequestList(): FriendRequestList = FriendRequestList(
    requests = sentRequests.map(FriendRequestItemResponseDto::toFriendRequest),
    friendCount = friendCount,
    receivedRequestCount = receivedRequestCount,
    sentRequestCount = sentRequestCount,
)

/**
 * 사용자 검색 응답을 친구 검색 화면 모델로 변환합니다.
 */
internal fun UserSearchResponseDto.toFriendSearchResult(): FriendSearchResult = FriendSearchResult(
    resultCount = resultCount,
    users = users.map(UserSearchItemResponseDto::toFriendSearchUser),
)

/**
 * 개별 친구 응답을 앱 내부 친구 모델로 변환합니다.
 *
 * 응원 구단은 기존 구단 ID 변환 함수를 재사용합니다.
 */
private fun FriendResponseDto.toFriend(): Friend = Friend(
    id = friendId.toString(),
    user = UserProfile(
        id = userId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        favoriteTeam = favoriteTeamId.toKboTeam("favoriteTeamId"),
    ),
)

/**
 * 친구 요청에 포함된 상대방을 앱 내부 모델로 변환합니다.
 */
private fun FriendRequestItemResponseDto.toFriendRequest(): FriendRequest = FriendRequest(
    id = friendRequestId.toString(),
    user = UserProfile(
        id = userId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        favoriteTeam = favoriteTeamId.toKboTeam("favoriteTeamId"),
    ),
)

/**
 * 검색된 사용자와 현재 사용자의 친구 관계를 앱 내부 모델로 변환합니다.
 */
private fun UserSearchItemResponseDto.toFriendSearchUser(): FriendSearchUser = FriendSearchUser(
    user = UserProfile(
        id = userId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        favoriteTeam = favoriteTeamName.toKboTeamOrNull(),
    ),
    relationshipStatus = friendStatus.toFriendRelationshipStatus(),
)

/**
 * 서버 구단명을 앱이 알고 있는 짧은 이름 또는 전체 이름과 비교합니다.
 */
private fun String.toKboTeamOrNull(): KboTeam? {
    val normalizedName = trim()

    return KboTeam.entries.firstOrNull { team ->
        team.displayName.equals(normalizedName, ignoreCase = true) ||
            team.fullName.equals(normalizedName, ignoreCase = true)
    }
}

/**
 * 서버 친구 관계 문자열을 앱 내부 상태로 변환합니다.
 */
private fun String.toFriendRelationshipStatus(): FriendRelationshipStatus = when (this) {
    "NONE" -> FriendRelationshipStatus.NONE
    "REQUEST_SENT" -> FriendRelationshipStatus.REQUEST_SENT
    "REQUEST_RECEIVED" -> FriendRelationshipStatus.REQUEST_RECEIVED
    "FRIEND" -> FriendRelationshipStatus.FRIEND
    else -> FriendRelationshipStatus.UNKNOWN
}
