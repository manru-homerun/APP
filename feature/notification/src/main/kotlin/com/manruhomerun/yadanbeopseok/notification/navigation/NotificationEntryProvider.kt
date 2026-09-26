package com.manruhomerun.yadanbeopseok.notification.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.FriendManagementInitialTab
import com.manruhomerun.yadanbeopseok.navigation.route.FriendManagementNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.MyPageNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.NotificationNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.NotificationSettingNavKey
import com.manruhomerun.yadanbeopseok.notifications.NotificationDestination
import com.manruhomerun.yadanbeopseok.notifications.resolveNotificationDestination

/** 알림 기능의 NavKey와 실제 Route를 연결합니다. */
fun EntryProviderScope<NavKey>.notificationEntryProvider(navigator: Navigator) {
    entry<NotificationNavKey> {
        NotificationRoute(
            onBackClick = navigator::navigateBack,
            onSettingClick = {
                navigator.navigate(NotificationSettingNavKey)
            },
            onNotificationClick = { notification ->
                navigator.navigateToNotificationDestination(
                    resolveNotificationDestination(
                        type = notification.type,
                        referenceId = notification.referenceId,
                    ),
                )
            },
        )
    }

    entry<NotificationSettingNavKey> {
        NotificationSettingRoute(
            onBackClick = navigator::navigateBack,
        )
    }
}

/** 알림 목적지의 기존 최상위 탭 스택을 구성한 뒤 실제 화면으로 이동합니다. */
fun Navigator.navigateToNotificationDestination(destination: NotificationDestination) {
    when (destination) {
        NotificationDestination.FriendRequests -> {
            navigateToTopLevelRoot(MyPageNavKey(returnToPrevious = true))
            navigate(
                FriendManagementNavKey(
                    initialTab = FriendManagementInitialTab.REQUESTS,
                ),
            )
        }

        NotificationDestination.FriendList -> {
            navigateToTopLevelRoot(MyPageNavKey(returnToPrevious = true))
            navigate(FriendManagementNavKey())
        }

        is NotificationDestination.TeamSchedule -> {
            navigateToTopLevelRoot(
                GameScheduleNavKey(
                    initialTeamId = destination.teamId,
                    returnToPrevious = true,
                ),
            )
        }

        NotificationDestination.NotificationCenter -> {
            navigateBackToOrNavigate(NotificationNavKey)
        }
    }
}
