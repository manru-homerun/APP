package com.manruhomerun.yadanbeopseok.mypage.viewmodel

import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.NICKNAME_MAX_LENGTH
import com.manruhomerun.yadanbeopseok.model.NICKNAME_MIN_LENGTH
import com.manruhomerun.yadanbeopseok.model.UserProfile

/** H·02 닉네임 입력값의 검증과 중복 확인 상태입니다. */
enum class ProfileNicknameState {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    UNCHANGED,
    VALID,
    CHECKING,
    AVAILABLE,
    DUPLICATED,
    CHECK_FAILED,
}

/** H·02 프로필 수정 화면의 상태입니다. */
data class ProfileEditUiState(
    val profile: UserProfile? = null,
    val originalNickname: String = "",
    val nickname: String = "",
    val nicknameState: ProfileNicknameState = ProfileNicknameState.EMPTY,
    val selectedTeam: KboTeam? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    /** API 요청과 길이 검사에 사용하는 앞뒤 공백이 제거된 닉네임입니다. */
    val normalizedNickname: String
        get() = nickname.trim()

    /** 앞뒤 공백을 제외한 닉네임 길이입니다. */
    val nicknameLength: Int
        get() = normalizedNickname.length

    /** 입력창에 오류 상태를 표시해야 하는지 나타냅니다. */
    val hasNicknameError: Boolean
        get() =
            nicknameState == ProfileNicknameState.TOO_SHORT ||
                nicknameState == ProfileNicknameState.TOO_LONG ||
                nicknameState == ProfileNicknameState.DUPLICATED ||
                nicknameState == ProfileNicknameState.CHECK_FAILED

    /** 닉네임 중복 확인을 다시 요청할 수 있는지 나타냅니다. */
    val canRetryNicknameCheck: Boolean
        get() = nicknameState == ProfileNicknameState.CHECK_FAILED

    /** 현재 입력과 요청 상태로 프로필을 저장할 수 있는지 나타냅니다. */
    val isSaveEnabled: Boolean
        get() =
            profile != null &&
                selectedTeam != null &&
                !isLoading &&
                !isSaving &&
                (
                    nicknameState == ProfileNicknameState.UNCHANGED ||
                        nicknameState == ProfileNicknameState.AVAILABLE
                )

    /** 닉네임 검증 상태에 맞는 사용자 안내 문구입니다. */
    val nicknameValidationMessage: String?
        get() =
            when (nicknameState) {
                ProfileNicknameState.EMPTY -> null
                ProfileNicknameState.TOO_SHORT -> "닉네임은 2자 이상 입력해주세요"
                ProfileNicknameState.TOO_LONG -> "닉네임은 12자 이하로 입력해주세요"
                ProfileNicknameState.UNCHANGED -> null
                ProfileNicknameState.VALID -> null
                ProfileNicknameState.CHECKING -> "닉네임 중복을 확인하고 있어요"
                ProfileNicknameState.AVAILABLE -> "사용 가능한 닉네임이에요"
                ProfileNicknameState.DUPLICATED -> "이미 사용 중인 닉네임이에요"
                ProfileNicknameState.CHECK_FAILED -> "닉네임을 확인하지 못했어요"
            }
}

/** 닉네임을 정규화한 뒤 길이와 원본 일치 여부를 검사합니다. */
internal fun String.toProfileNicknameState(originalNickname: String): ProfileNicknameState {
    val normalizedNickname = trim()

    return when {
        normalizedNickname.isEmpty() -> ProfileNicknameState.EMPTY
        normalizedNickname.length < NICKNAME_MIN_LENGTH -> ProfileNicknameState.TOO_SHORT
        normalizedNickname.length > NICKNAME_MAX_LENGTH -> ProfileNicknameState.TOO_LONG
        normalizedNickname == originalNickname.trim() -> ProfileNicknameState.UNCHANGED
        else -> ProfileNicknameState.VALID
    }
}
