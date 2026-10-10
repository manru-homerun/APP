package com.manruhomerun.yadanbeopseok.ui

import android.content.ActivityNotFoundException

const val LEGAL_DOCUMENT_OPEN_ERROR_MESSAGE = "문서를 열 수 없습니다. 브라우저 앱을 확인해주세요."

/** 외부 앱 실행 성공 여부를 반환하며, 처리할 앱이 없거나 실행이 제한된 경우에만 실패로 처리합니다. */
fun tryOpenExternalActivity(action: () -> Unit): Boolean = try {
    action()
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: IllegalArgumentException) {
    false
} catch (_: SecurityException) {
    false
}
