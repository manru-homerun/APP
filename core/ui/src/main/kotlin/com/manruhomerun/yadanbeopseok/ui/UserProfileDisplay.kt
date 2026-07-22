package com.manruhomerun.yadanbeopseok.ui

import com.manruhomerun.yadanbeopseok.model.UserProfile

/**
 * 닉네임의 앞뒤 공백을 제거하고, 비어 있으면 null을 반환합니다.
 */
internal fun UserProfile.normalizedNickname(): String? =
    nickname
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

/**
 * 화면에 표시할 닉네임을 반환합니다.
 *
 * 유효한 닉네임이 없으면 기본 이름인 "사용자"를 반환합니다.
 */
internal fun UserProfile.displayNickname(): String =
    normalizedNickname() ?: "사용자"
