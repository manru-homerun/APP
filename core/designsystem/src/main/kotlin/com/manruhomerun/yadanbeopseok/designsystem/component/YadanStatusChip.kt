package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 상태 칩의 시각적 유형입니다.
 */
enum class YadanStatusChipStyle {
    /** D-day와 같이 하늘색으로 강조하는 상태입니다. */
    PRIMARY,

    /** 진행 중인 상태이며 흰색 pulse 점을 함께 표시합니다. */
    LIVE,

    /** 여행 방장을 나타내는 상태입니다. */
    HOST,

    /** 여행 동행자를 나타내는 상태입니다. */
    GUEST,

    /** 종료되거나 강조하지 않는 상태입니다. */
    MUTED,
}

/**
 * 상태 칩의 크기입니다.
 */
enum class YadanStatusChipSize {
    /** HTML `.schip`의 기본 크기입니다. */
    DEFAULT,

    /** HTML `.schip.sm`의 작은 크기입니다. */
    SMALL,
}

/**
 * 여행 진행 상태, D-day, 방장 및 동행자 상태를 표시하는 칩입니다.
 *
 * HTML의 `.schip`, `.schip.live`, `.schip.dday`, `.schip.host`,
 * `.schip.guest`, `.schip.muted`, `.schip.sm`에 대응합니다.
 *
 * 상태를 보여주는 용도이므로 자체 클릭 동작은 제공하지 않습니다.
 * 실제 상태 판단과 문구 생성은 화면 또는 ViewModel에서 처리합니다.
 *
 * @param text 칩에 표시할 상태 문구입니다.
 * @param modifier 칩의 배치에 사용할 Modifier입니다.
 * @param style 상태 칩의 색상과 표시 유형입니다.
 * @param size 상태 칩의 시각적 크기입니다.
 * @param leadingIcon 문구 앞에 표시할 아이콘입니다.
 * LIVE 스타일에서는 pulse 점을 사용하므로 이 아이콘을 표시하지 않습니다.
 */
@Composable
fun YadanStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    style: YadanStatusChipStyle = YadanStatusChipStyle.PRIMARY,
    size: YadanStatusChipSize = YadanStatusChipSize.DEFAULT,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val visuals = statusChipVisuals(style)

    val horizontalPadding =
        when (size) {
            YadanStatusChipSize.DEFAULT -> DEFAULT_HORIZONTAL_PADDING
            YadanStatusChipSize.SMALL -> SMALL_HORIZONTAL_PADDING
        }

    val verticalPadding =
        when (size) {
            YadanStatusChipSize.DEFAULT -> DEFAULT_VERTICAL_PADDING
            YadanStatusChipSize.SMALL -> SMALL_VERTICAL_PADDING
        }

    val textStyle =
        when (size) {
            YadanStatusChipSize.DEFAULT ->
                MaterialTheme.typography.labelSmall.copy(
                    // HTML의 font-weight: 800에 대응합니다.
                    fontWeight = FontWeight.ExtraBold,
                )

            YadanStatusChipSize.SMALL ->
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
        }

    Surface(
        modifier = modifier,
        shape = YadanPillShape,
        color = visuals.containerColor,
        contentColor = visuals.contentColor,
        border = visuals.border,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(CONTENT_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                style == YadanStatusChipStyle.LIVE -> {
                    YadanLiveIndicator()
                }

                leadingIcon != null -> {
                    /*
                     * 방장 아이콘은 문구보다 밝은 Primary 색상을
                     * 사용하므로 아이콘 색상을 별도로 제공합니다.
                     */
                    CompositionLocalProvider(
                        LocalContentColor provides visuals.iconColor,
                    ) {
                        Box(
                            modifier = Modifier.size(LEADING_ICON_SIZE),
                            contentAlignment = Alignment.Center,
                        ) {
                            leadingIcon()
                        }
                    }
                }
            }

            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * HTML `.ld`와 `pulse` 애니메이션에 대응하는 LIVE 표시입니다.
 */
@Composable
private fun YadanLiveIndicator() {
    val infiniteTransition =
        rememberInfiniteTransition(
            label = "Yadan live indicator",
        )

    /*
     * HTML의 70% 지점까지 ring이 6px만큼 확장된 뒤,
     * 남은 시간에는 투명한 상태로 원래 크기로 돌아갑니다.
     */
    val ringExpansion by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = LIVE_PULSE_DURATION_MILLIS
                        0f at 0
                        1f at LIVE_PULSE_EXPAND_MILLIS
                        0f at LIVE_PULSE_DURATION_MILLIS
                    },
                repeatMode = RepeatMode.Restart,
            ),
        label = "Yadan live ring expansion",
    )

    val ringAlpha by
    infiniteTransition.animateFloat(
        initialValue = LIVE_PULSE_START_ALPHA,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = LIVE_PULSE_DURATION_MILLIS
                        LIVE_PULSE_START_ALPHA at 0
                        0f at LIVE_PULSE_EXPAND_MILLIS
                        0f at LIVE_PULSE_DURATION_MILLIS
                    },
                repeatMode = RepeatMode.Restart,
            ),
        label = "Yadan live ring alpha",
    )

    Box(
        modifier =
            Modifier
                .size(LIVE_INDICATOR_SIZE)
                .drawBehind {
                    val dotRadius = LIVE_INDICATOR_SIZE.toPx() / 2f
                    val ringRadius =
                        dotRadius +
                            LIVE_INDICATOR_MAX_EXPANSION.toPx() *
                            ringExpansion

                    drawCircle(
                        color =
                            YadanOnPrimary.copy(
                                alpha = ringAlpha,
                            ),
                        radius = ringRadius,
                    )

                    drawCircle(
                        color = YadanOnPrimary,
                        radius = dotRadius,
                    )
                },
    )
}

/**
 * 상태 유형에 맞는 색상과 테두리를 반환합니다.
 */
private fun statusChipVisuals(
    style: YadanStatusChipStyle,
): YadanStatusChipVisuals =
    when (style) {
        YadanStatusChipStyle.PRIMARY,
        YadanStatusChipStyle.LIVE,
            ->
            YadanStatusChipVisuals(
                containerColor = YadanPrimary,
                contentColor = YadanOnPrimary,
                iconColor = YadanOnPrimary,
            )

        YadanStatusChipStyle.HOST ->
            YadanStatusChipVisuals(
                containerColor = YadanSurface,
                contentColor = YadanPrimaryInk,
                iconColor = YadanPrimary,
                border =
                    BorderStroke(
                        width = STATUS_BORDER_WIDTH,
                        color = YadanPrimary,
                    ),
            )

        YadanStatusChipStyle.GUEST ->
            YadanStatusChipVisuals(
                containerColor = YadanSurface,
                contentColor = YadanTextSecondary,
                iconColor = YadanTextSecondary,
                border =
                    BorderStroke(
                        width = STATUS_BORDER_WIDTH,
                        color = YadanOutline,
                    ),
            )

        YadanStatusChipStyle.MUTED ->
            YadanStatusChipVisuals(
                containerColor = YadanDivider,
                contentColor = YadanTextMuted,
                iconColor = YadanTextMuted,
            )
    }

/**
 * 상태 칩을 그리는 데 필요한 색상과 테두리입니다.
 */
private data class YadanStatusChipVisuals(
    val containerColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val border: BorderStroke? = null,
)

private val DEFAULT_HORIZONTAL_PADDING = 10.dp
private val DEFAULT_VERTICAL_PADDING = 4.dp

private val SMALL_HORIZONTAL_PADDING = 8.dp
private val SMALL_VERTICAL_PADDING = 3.dp

private val CONTENT_SPACING = 5.dp
private val LEADING_ICON_SIZE = 13.dp

private val LIVE_INDICATOR_SIZE = 6.dp
private val LIVE_INDICATOR_MAX_EXPANSION = 6.dp

private val STATUS_BORDER_WIDTH = 1.5.dp

private const val LIVE_PULSE_START_ALPHA = 0.55f
private const val LIVE_PULSE_DURATION_MILLIS = 1_600
private const val LIVE_PULSE_EXPAND_MILLIS = 1_120

@Preview(
    name = "Yadan status chips",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanStatusChipPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "기본 상태",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                YadanStatusChip(
                    text = "여행 중 · DAY 1/2",
                    style = YadanStatusChipStyle.LIVE,
                )

                YadanStatusChip(
                    text = "D-3",
                    style = YadanStatusChipStyle.PRIMARY,
                )

                YadanStatusChip(
                    text = "종료",
                    style = YadanStatusChipStyle.MUTED,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                YadanStatusChip(
                    text = "방장",
                    style = YadanStatusChipStyle.HOST,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                        )
                    },
                )

                YadanStatusChip(
                    text = "동행자",
                    style = YadanStatusChipStyle.GUEST,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                        )
                    },
                )
            }

            Text(
                text = "작은 크기",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                YadanStatusChip(
                    text = "여행 중",
                    style = YadanStatusChipStyle.LIVE,
                    size = YadanStatusChipSize.SMALL,
                )

                YadanStatusChip(
                    text = "방장",
                    style = YadanStatusChipStyle.HOST,
                    size = YadanStatusChipSize.SMALL,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}
