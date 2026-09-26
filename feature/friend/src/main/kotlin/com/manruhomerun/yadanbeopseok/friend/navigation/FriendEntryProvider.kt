package com.manruhomerun.yadanbeopseok.friend.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.friend.viewmodel.FriendManagementTab
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.FriendManagementInitialTab
import com.manruhomerun.yadanbeopseok.navigation.route.FriendManagementNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.FriendSearchNavKey

/** 친구 기능의 NavKey와 실제 Route를 연결합니다. */
fun EntryProviderScope<NavKey>.friendEntryProvider(navigator: Navigator) {
    entry<FriendManagementNavKey> { key ->
        FriendManagementRoute(
            initialTab = key.initialTab.toFriendManagementTab(),
            onBackClick = navigator::navigateBack,
            onSearchClick = {
                navigator.navigate(FriendSearchNavKey)
            },
        )
    }

    entry<FriendSearchNavKey> {
        FriendSearchRoute(
            onBackClick = navigator::navigateBack,
        )
    }
}

private fun FriendManagementInitialTab.toFriendManagementTab() = when (this) {
    FriendManagementInitialTab.FRIENDS ->
        FriendManagementTab.FRIENDS

    FriendManagementInitialTab.REQUESTS ->
        FriendManagementTab.REQUESTS
}
