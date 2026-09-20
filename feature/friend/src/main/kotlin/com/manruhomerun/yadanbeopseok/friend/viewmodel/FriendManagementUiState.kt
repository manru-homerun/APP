package com.manruhomerun.yadanbeopseok.friend.viewmodel

import com.manruhomerun.yadanbeopseok.model.Friend
import com.manruhomerun.yadanbeopseok.model.FriendRequest

/** F·01·F·02에서 선택할 친구 관리 탭입니다. */
enum class FriendManagementTab {
    FRIENDS,
    REQUESTS,
}

/**
 * F·01 친구 목록과 F·02 친구 요청 화면의 상태입니다.
 */
data class FriendManagementUiState(
    val selectedTab: FriendManagementTab = FriendManagementTab.FRIENDS,
    val friends: List<Friend> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val friendCount: Long? = null,
    val receivedRequestCount: Long? = null,
    val sentRequestCount: Long? = null,
    val isFriendsLoading: Boolean = true,
    val isReceivedRequestsLoading: Boolean = false,
    val isSentRequestsLoading: Boolean = false,
    val friendsErrorMessage: String? = null,
    val receivedRequestsErrorMessage: String? = null,
    val sentRequestsErrorMessage: String? = null,
    val processingRequestIds: Set<String> = emptySet(),
    val deletingFriendId: String? = null,
    val userMessage: String? = null,
)
