package com.manruhomerun.yadanbeopseok.network.friend.dto

import kotlinx.serialization.Serializable

/**
 * 선택한 사용자에게 친구 요청을 전송하는 요청 DTO입니다.
 */
@Serializable
data class FriendRequestCreateRequestDto(val receiverUserId: String)
