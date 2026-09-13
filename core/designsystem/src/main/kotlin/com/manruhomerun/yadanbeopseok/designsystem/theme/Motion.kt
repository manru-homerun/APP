package com.manruhomerun.yadanbeopseok.designsystem.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

/** 앱의 짧은 상태 전환에 사용하는 시간입니다. */
val YadanMotionDurationShort = 180

/** 일반적인 화면 이동에 사용하는 시간입니다. */
val YadanMotionDurationMedium = 280

/** 완료 화면과 보상 화면에 사용하는 시간입니다. */
val YadanMotionDurationEmphasized = 320

/** 다음 화면이 오른쪽에서 들어오고 현재 화면이 왼쪽으로 나가는 전환입니다. */
fun <S> AnimatedContentTransitionScope<S>.yadanForwardTransition(): ContentTransform {
    val enter = slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(
            durationMillis = YadanMotionDurationMedium,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(durationMillis = YadanMotionDurationShort),
    )

    val exit = slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(
            durationMillis = YadanMotionDurationMedium,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(durationMillis = YadanMotionDurationShort),
    )

    return enter togetherWith exit
}

/** 뒤로가기에서 현재 화면이 오른쪽으로 나가고 이전 화면이 복원되는 전환입니다. */
fun <S> AnimatedContentTransitionScope<S>.yadanBackwardTransition(): ContentTransform {
    val enter = slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(
            durationMillis = YadanMotionDurationMedium,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeIn(
        animationSpec = tween(durationMillis = YadanMotionDurationShort),
    )

    val exit = slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(
            durationMillis = YadanMotionDurationMedium,
            easing = FastOutSlowInEasing,
        ),
    ) + fadeOut(
        animationSpec = tween(durationMillis = YadanMotionDurationShort),
    )

    return enter togetherWith exit
}

/** 탭 이동과 로딩 결과처럼 방향성이 없는 화면에 사용하는 전환입니다. */
fun yadanFadeTransition(): ContentTransform = fadeIn(
    animationSpec = tween(durationMillis = YadanMotionDurationShort),
) togetherWith fadeOut(
    animationSpec = tween(durationMillis = YadanMotionDurationShort),
)

/** 저장 완료와 보상 화면을 강조하는 페이드 및 확대 전환입니다. */
fun yadanEmphasizedTransition(): ContentTransform {
    val enter: EnterTransition = fadeIn(
        animationSpec = tween(durationMillis = YadanMotionDurationEmphasized),
    ) + scaleIn(
        initialScale = 0.96f,
        animationSpec = tween(
            durationMillis = YadanMotionDurationEmphasized,
            easing = FastOutSlowInEasing,
        ),
    )

    val exit: ExitTransition = fadeOut(
        animationSpec = tween(durationMillis = YadanMotionDurationShort),
    )

    return enter togetherWith exit
}
