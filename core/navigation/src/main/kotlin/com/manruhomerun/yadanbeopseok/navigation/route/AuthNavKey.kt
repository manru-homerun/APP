package com.manruhomerun.yadanbeopseok.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 로그인과 신규 회원 온보딩 화면에서 사용하는 NavKey의 공통 계약입니다.
 */
sealed interface AuthNavKey : NavKey

/**
 * 카카오 로그인을 진행하는 시작 화면입니다.
 */
@Serializable
data object LoginNavKey : AuthNavKey

/**
 * 신규 회원이 서비스 이용약관에 동의하는 화면입니다.
 */
@Serializable
data object TermsAgreementNavKey : AuthNavKey

/**
 * 신규 회원이 닉네임, 성별 및 생년월일을 입력하는 화면입니다.
 */
@Serializable
data object BasicInfoNavKey : AuthNavKey

/**
 * 신규 회원이 응원 구단을 선택하는 화면입니다.
 */
@Serializable
data object TeamSelectionNavKey : AuthNavKey

/**
 * 신규 회원이 거주 지역과 여행 취향을 입력하는 화면입니다.
 */
@Serializable
data object TravelProfileNavKey : AuthNavKey
