package com.manruhomerun.yadanbeopseok.auth.viewmodel

import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** 닉네임 입력값의 로컬 검증과 중복 확인 상태입니다. */
enum class NicknameInputState {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,

    /** 로컬 입력 규칙은 통과했지만 서버 중복 확인 전인 상태입니다. */
    VALID,

    /** 서버에서 닉네임 중복 여부를 확인하고 있습니다. */
    CHECKING,

    /** 서버에서 사용할 수 있는 닉네임으로 확인됐습니다. */
    AVAILABLE,

    /** 서버에서 이미 사용 중인 닉네임으로 확인됐습니다. */
    DUPLICATED,

    /** 네트워크 또는 서버 오류로 중복 여부를 확인하지 못했습니다. */
    CHECK_FAILED,
}

/** 생년월일 입력값의 검증 상태입니다. */
enum class BirthDateInputState {
    EMPTY,
    FUTURE_DATE,
    UNDER_MINIMUM_AGE,
    VALID,
}

/** 전체 온보딩 화면에서 공유하는 입력 및 요청 상태입니다. */
data class OnboardingUiState(
    val isServiceTermsAgreed: Boolean = false,
    val isPrivacyAgreementAgreed: Boolean = false,
    val nickname: String = "",
    val nicknameInputState: NicknameInputState = NicknameInputState.EMPTY,
    val gender: Gender? = null,
    val birthDate: LocalDate? = null,
    val birthDateInputState: BirthDateInputState = BirthDateInputState.EMPTY,
    val selectedTeam: KboTeam? = null,
    val residenceRegion: ProfileRegion? = null,
    val travelStyleScore: TravelStyleScore = TravelStyleScore(value = 4),
    val preferredTravelRegions: List<ProfileRegion> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    /** 서버 요청과 길이 검사에 사용하는 앞뒤 공백이 제거된 닉네임입니다. */
    val normalizedNickname: String
        get() = nickname.trim()

    /** 앞뒤 공백을 제외한 닉네임 길이입니다. */
    val nicknameLength: Int
        get() = normalizedNickname.length

    /** 서버에서 닉네임 중복 여부를 확인하고 있는지 나타냅니다. */
    val isNicknameChecking: Boolean
        get() = nicknameInputState == NicknameInputState.CHECKING

    /** 서버에서 사용 가능한 닉네임으로 확인됐는지 나타냅니다. */
    val isNicknameAvailable: Boolean
        get() = nicknameInputState == NicknameInputState.AVAILABLE

    /** 닉네임 입력란에 오류 상태를 표시해야 하는지 나타냅니다. */
    val hasNicknameValidationError: Boolean
        get() =
            nicknameInputState == NicknameInputState.TOO_SHORT ||
                nicknameInputState == NicknameInputState.TOO_LONG ||
                nicknameInputState == NicknameInputState.DUPLICATED ||
                nicknameInputState == NicknameInputState.CHECK_FAILED

    /** 모든 필수 약관에 동의했는지 나타냅니다. */
    val isAllAgreed: Boolean
        get() = isServiceTermsAgreed && isPrivacyAgreementAgreed

    /** 기본 정보 화면에서 다음 단계로 이동할 수 있는지 나타냅니다. */
    val isBasicInfoNextEnabled: Boolean
        get() = isNicknameAvailable &&
                gender != null &&
                birthDateInputState == BirthDateInputState.VALID

    /** 응원 구단 선택 화면에서 다음 단계로 이동할 수 있는지 나타냅니다. */
    val isTeamSelectionNextEnabled: Boolean
        get() = selectedTeam != null

    /** 여행 프로필 화면의 필수 입력이 완료됐는지 나타냅니다. */
    val isTravelProfileStartEnabled: Boolean
        get() =
            residenceRegion != null &&
                preferredTravelRegions.isNotEmpty()

    /** 온보딩 전체 필수 입력이 완료됐는지 나타냅니다. */
    val isOnboardingReadyToSubmit: Boolean
        get() =
            isAllAgreed &&
                isBasicInfoNextEnabled &&
                isTeamSelectionNextEnabled &&
                isTravelProfileStartEnabled

    /** 현재 온보딩 저장 요청을 시작할 수 있는지 나타냅니다. */
    val isOnboardingSubmitEnabled: Boolean
        get() = isOnboardingReadyToSubmit && !isSubmitting

    /** 닉네임 검증 결과에 맞는 사용자 안내 문구입니다. */
    val nicknameValidationMessage: String?
        get() =
            when (nicknameInputState) {
                NicknameInputState.EMPTY -> null
                NicknameInputState.TOO_SHORT -> "닉네임은 2자 이상 입력해주세요"
                NicknameInputState.TOO_LONG -> "닉네임은 12자 이하로 입력해주세요"
                NicknameInputState.VALID -> null
                NicknameInputState.CHECKING -> "닉네임 중복을 확인하고 있어요"
                NicknameInputState.AVAILABLE -> "사용 가능한 닉네임이에요"
                NicknameInputState.DUPLICATED -> "이미 사용 중인 닉네임이에요"
                NicknameInputState.CHECK_FAILED -> "닉네임을 확인하지 못했어요"
            }

    /** 생년월일 검증 결과에 맞는 사용자 안내 문구입니다. */
    val birthDateValidationMessage: String?
        get() =
            when (birthDateInputState) {
                BirthDateInputState.EMPTY -> null
                BirthDateInputState.FUTURE_DATE ->
                    "오늘 이후 날짜는 선택할 수 없어요"
                BirthDateInputState.UNDER_MINIMUM_AGE ->
                    "만 14세 이상만 가입할 수 있어요"
                BirthDateInputState.VALID -> null
            }
}

/**
 * 닉네임의 앞뒤 공백을 제거한 뒤 API와 동일한 길이 규칙을 검사합니다.
 *
 * 글자 사이 공백은 허용하며, 문자 종류는 별도로 제한하지 않습니다.
 */
internal fun String.toNicknameInputState(): NicknameInputState {
    val normalizedNickname = trim()

    return when {
        normalizedNickname.isEmpty() -> NicknameInputState.EMPTY
        normalizedNickname.length < NICKNAME_MIN_LENGTH -> NicknameInputState.TOO_SHORT
        normalizedNickname.length > NICKNAME_MAX_LENGTH -> NicknameInputState.TOO_LONG
        else -> NicknameInputState.VALID
    }
}

/** 생년월일과 최소 가입 연령을 검증합니다. */
internal fun LocalDate?.toBirthDateInputState(
    currentDate: LocalDate,
): BirthDateInputState {
    if (this == null) {
        return BirthDateInputState.EMPTY
    }

    val latestAllowedBirthDate =
        currentDate.minus(
            value = MINIMUM_SIGN_UP_AGE,
            unit = DateTimeUnit.YEAR,
        )

    return when {
        this > currentDate -> BirthDateInputState.FUTURE_DATE
        this > latestAllowedBirthDate ->
            BirthDateInputState.UNDER_MINIMUM_AGE
        else -> BirthDateInputState.VALID
    }
}

internal const val NICKNAME_MIN_LENGTH = 2
internal const val NICKNAME_MAX_LENGTH = 12

private const val MINIMUM_SIGN_UP_AGE = 14
