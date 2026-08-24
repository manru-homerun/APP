package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendListResponseDto
import com.manruhomerun.yadanbeopseok.network.friend.dto.FriendResponseDto

/**
 * 친구 목록 응답을 앱 내부 친구 목록으로 변환합니다.
 *
 * B·04에서는 친구 수와 요청 수를 사용하지 않으므로 친구 목록만 반환합니다.
 */
internal fun FriendListResponseDto.toFriends(): List<Friend> =
    friends.map { friend ->
        friend.toFriend()
    }

/**
 * 개별 친구 응답을 앱 내부 친구 모델로 변환합니다.
 *
 * 응원 구단은 기존 구단 ID 변환 함수를 재사용합니다.
 */
private fun FriendResponseDto.toFriend(): Friend =
    Friend(
        id = friendId.toString(),
        user = UserProfile(
            id = userId,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            favoriteTeam = favoriteTeamId.toKboTeam("favoriteTeamId"),
        ),
    )
