package com.manruhomerun.yadanbeopseok

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.manruhomerun.yadanbeopseok.auth.navigation.authEntryProvider
import com.manruhomerun.yadanbeopseok.baseball.navigation.baseballEntryProvider
import com.manruhomerun.yadanbeopseok.home.navigation.homeEntryProvider
import com.manruhomerun.yadanbeopseok.navigation.YadanNavigationState
import com.manruhomerun.yadanbeopseok.navigation.rememberSharedViewModelStoreNavEntryDecorator
import com.manruhomerun.yadanbeopseok.travel.navigation.travelEntryProvider

/**
 * 앱의 Nav3 백스택과 각 Feature의 화면 Entry를 연결합니다.
 *
 * 새로운 Feature가 추가되면 해당 Feature의 EntryProvider를
 * [entryProvider] 내부에 추가합니다.
 */
@Composable
fun YadanNavHost(
    navigationState: YadanNavigationState,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = navigationState.backStack,
        onBack = navigationState::navigateBack,
        modifier = modifier.fillMaxSize(),
        entryDecorators =
            listOf(
                /*
                 * 화면별 rememberSaveable 상태를 백스택에 맞춰 보존하고 복원합니다.
                 */
                rememberSaveableStateHolderNavEntryDecorator(),

                /*
                 * 각 NavEntry에 ViewModelStore를 제공하고,
                 * 부모가 지정된 화면에서는 부모 ViewModel도 공유할 수 있게 합니다.
                 */
                rememberSharedViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                authEntryProvider(
                    navigator = navigationState,
                )

                baseballEntryProvider(
                    navigator = navigationState,
                )

                homeEntryProvider(
                    navigator = navigationState,
                )

                travelEntryProvider(
                    navigator = navigationState,
                )
            }
    )
}
