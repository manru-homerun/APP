package com.manruhomerun.yadanbeopseok.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.SharedViewModelStoreNavEntryDecorator
import com.manruhomerun.yadanbeopseok.navigation.route.BasicInfoNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TeamSelectionNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TermsAgreementNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelProfileNavKey

/**
 * 인증 및 신규 회원 온보딩에서 사용하는 NavKey와
 * 실제 화면 Route를 연결합니다.
 *
 * 온보딩 화면들은 [TermsAgreementNavKey]가 소유한
 * ViewModelStore를 공유합니다.
 */
fun EntryProviderScope<NavKey>.authEntryProvider(
    navigator: Navigator,
) {
    val onboardingChildMetadata =
        SharedViewModelStoreNavEntryDecorator.parent(
            parentContentKey =
                ONBOARDING_PARENT_CONTENT_KEY,
        )

    entry<LoginNavKey> {
        LoginRoute(
            navigator = navigator,
        )
    }

    /*
     * 약관 화면을 온보딩 공유 ViewModelStore의 부모로 사용합니다.
     */
    entry<TermsAgreementNavKey>(
        clazzContentKey = {
            ONBOARDING_PARENT_CONTENT_KEY
        },
    ) {
        TermsAgreementRoute(
            navigator = navigator,
        )
    }

    entry<BasicInfoNavKey>(
        metadata = onboardingChildMetadata,
    ) {
        BasicInfoRoute(
            navigator = navigator,
        )
    }

    entry<TeamSelectionNavKey>(
        metadata = onboardingChildMetadata,
    ) {
        TeamSelectionRoute(
            navigator = navigator,
        )
    }

    entry<TravelProfileNavKey>(
        metadata = onboardingChildMetadata,
    ) {
        TravelProfileRoute(
            navigator = navigator,
        )
    }
}

/**
 * 온보딩 부모 NavEntry의 ViewModelStore를 식별하는 안정적인 content key입니다.
 */
private const val ONBOARDING_PARENT_CONTENT_KEY =
    "auth/onboarding"
