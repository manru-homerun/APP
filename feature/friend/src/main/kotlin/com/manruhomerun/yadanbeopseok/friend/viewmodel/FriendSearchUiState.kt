package com.manruhomerun.yadanbeopseok.friend.viewmodel

import com.manruhomerun.yadanbeopseok.model.FriendSearchUser

/**
 * F·03 친구 찾기 화면의 검색어, 결과와 요청 처리 상태입니다.
 */
data class FriendSearchUiState(
    val query: String = "",
    val searchedQuery: String? = null,
    val users: List<FriendSearchUser> = emptyList(),
    val resultCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requestingUserIds: Set<String> = emptySet(),
    val userMessage: String? = null,
)
