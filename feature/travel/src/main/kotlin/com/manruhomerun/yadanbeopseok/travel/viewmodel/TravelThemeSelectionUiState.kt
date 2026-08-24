package com.manruhomerun.yadanbeopseok.travel.viewmodel

import com.manruhomerun.yadanbeopseok.model.TravelTheme

/**
 * B·03 여행 테마 선택 화면에서 사용하는 상태입니다.
 *
 * 서버에서 조회한 선택 가능한 테마 목록만 관리합니다.
 * 사용자가 선택한 테마는 공유 [TravelCreationUiState]에 저장합니다.
 */
data class TravelThemeSelectionUiState(
    /** 서버에서 제공하는 여행 테마 목록입니다. */
    val themes: List<TravelTheme> = emptyList(),

    /** 여행 테마 목록을 불러오고 있는지 나타냅니다. */
    val isLoading: Boolean = false,

    /** 여행 테마 조회 중 표시할 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
)

/**
 * 여행 테마를 화면에 표시할 때 사용하는 정보입니다.
 *
 * 서버에서 받은 순수 앱 모델과 화면 전용 아이콘 유형을 함께 보관합니다.
 */
data class TravelThemeItemUiState(
    val theme: TravelTheme,
    val icon: TravelThemeIcon,
)

/**
 * B·03 여행 테마 카드에서 사용하는 아이콘 유형입니다.
 *
 * Compose의 ImageVector를 직접 저장하지 않아 ViewModel 상태가
 * Compose UI 구현에 의존하지 않도록 합니다.
 */
enum class TravelThemeIcon {
    ESCAPE,
    RELAXATION,
    MEMORY,
    REFLECTION,
    PHOTOGRAPHY,
    ACTIVITY,
    EXCITEMENT,
    CULTURE,
    CELEBRATION,
    SPONTANEOUS,
    GENERAL,
}

/**
 * 서버 여행 테마를 화면 표시 정보로 변환합니다.
 *
 * 아직 서버 명세에 아이콘 코드가 없으므로 확정된 테마 이름을 기준으로
 * 아이콘 유형을 연결하며, 알 수 없는 테마에는 공통 아이콘을 사용합니다.
 */
internal fun TravelTheme.toTravelThemeItemUiState(): TravelThemeItemUiState =
    TravelThemeItemUiState(
        theme = this,
        icon = themeIconByName[name.trim()] ?: TravelThemeIcon.GENERAL,
    )

private val themeIconByName = mapOf(
    "지루한 일상 탈출" to TravelThemeIcon.ESCAPE,
    "피로를 푸는 휴식" to TravelThemeIcon.RELAXATION,
    "힐링" to TravelThemeIcon.RELAXATION,
    "소중한 사람과 추억" to TravelThemeIcon.MEMORY,
    "추억" to TravelThemeIcon.MEMORY,
    "나를 돌아보는 시간" to TravelThemeIcon.REFLECTION,
    "남는 건 사진 뿐" to TravelThemeIcon.PHOTOGRAPHY,
    "액티비티로 활력 충전" to TravelThemeIcon.ACTIVITY,
    "낯선 곳의 설렘" to TravelThemeIcon.EXCITEMENT,
    "역사·문화 탐방" to TravelThemeIcon.CULTURE,
    "특별한 기념일" to TravelThemeIcon.CELEBRATION,
    "발길이 이끄는 대로" to TravelThemeIcon.SPONTANEOUS,
)
