package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Friend

/**
 * 현재 사용자의 친구 목록을 제공하는 Repository 계약입니다.
 *
 * 여행 만들기의 동행자 선택 화면에서 친구 목록을 조회할 때 사용합니다.
 * 친구 검색, 요청 및 삭제 기능은 F 그룹 개발 시 별도 API 계약을
 * 확인한 후 추가합니다.
 */
interface FriendRepository {
    /**
     * 현재 로그인한 사용자의 친구 목록을 조회합니다.
     */
    suspend fun getFriends(): List<Friend>
}
