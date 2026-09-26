package com.manruhomerun.yadanbeopseok.notification.viewmodel

import com.manruhomerun.yadanbeopseok.model.NotificationSetting

/** E·02 알림 설정 화면의 상태입니다. */
data class NotificationSettingUiState(
    val setting: NotificationSetting? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
)
