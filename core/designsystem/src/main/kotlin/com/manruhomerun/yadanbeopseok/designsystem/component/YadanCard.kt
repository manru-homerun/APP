package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 공통 카드의 시각적 유형입니다.
 */
enum class YadanCardStyle {
    /**
     * 일반적인 정보 표시에 사용하는 기본 카드입니다.
     *
     * 흰색 배경과 회색 테두리를 사용합니다.
     */
    DEFAULT,

    /**
     * 사용자가 선택한 항목에 사용하는 카드입니다.
     *
     * 연한 하늘색 배경과 2dp Primary 테두리를 사용합니다.
     * B·01, B·02, B·04 선택 카드 스타일에 대응합니다.
     */
    SELECTED,
}

/**
 * 클릭 동작이 없는 정보 표시용 공통 카드입니다.
 *
 * 카드마다 내부 여백이 다르므로 공통 카드에서는 패딩을 지정하지 않습니다.
 *
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param style 카드의 기본 또는 선택 상태입니다.
 * @param content 카드 내부에 표시할 내용입니다.
 */
@Composable
fun YadanCard(
    modifier: Modifier = Modifier,
    style: YadanCardStyle = YadanCardStyle.DEFAULT,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = yadanCardColors(style),
        elevation = yadanCardElevation(),
        border = yadanCardBorder(style),
        content = content,
    )
}

/**
 * 클릭할 수 있는 야단법석 공통 카드입니다.
 *
 * Material3 Card의 클릭 처리, Ripple 및 접근성 처리를 사용합니다.
 *
 * @param onClick 카드를 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param style 카드의 기본 또는 선택 상태입니다.
 * @param enabled 카드 활성화 여부입니다.
 * @param content 카드 내부에 표시할 내용입니다.
 */
@Composable
fun YadanCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YadanCardStyle = YadanCardStyle.DEFAULT,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = yadanCardColors(style),
        elevation = yadanCardElevation(),
        border = yadanCardBorder(style),
        content = content,
    )
}

/**
 * 카드 상태에 맞는 배경색과 콘텐츠 색상을 반환합니다.
 */
@Composable
private fun yadanCardColors(
    style: YadanCardStyle,
): CardColors {
    val containerColor =
        when (style) {
            YadanCardStyle.DEFAULT -> YadanSurface
            YadanCardStyle.SELECTED -> YadanPrimaryTint
        }

    return CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = YadanTextPrimary,
    )
}

/**
 * 카드 상태에 맞는 테두리를 반환합니다.
 */
private fun yadanCardBorder(
    style: YadanCardStyle,
): BorderStroke =
    when (style) {
        YadanCardStyle.DEFAULT ->
            BorderStroke(
                width = 1.5.dp,
                color = YadanOutline,
            )

        YadanCardStyle.SELECTED ->
            BorderStroke(
                width = 2.dp,
                color = YadanPrimary,
            )
    }

/**
 * HTML의 shadow-sm을 Material elevation으로 근사합니다.
 *
 * HTML에서는 카드 상태에 따라 그림자가 변하지 않으므로
 * 모든 상호작용 상태에 같은 elevation을 사용합니다.
 */
@Composable
private fun yadanCardElevation(): CardElevation =
    CardDefaults.cardElevation(
        defaultElevation = CardShadowElevation,
        pressedElevation = CardShadowElevation,
        focusedElevation = CardShadowElevation,
        hoveredElevation = CardShadowElevation,
        draggedElevation = CardShadowElevation,
        disabledElevation = CardShadowElevation,
    )

private val CardShadowElevation = 2.dp

@Preview(
    name = "Yadan cards - HTML examples",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanCardPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            /*
             * B·02에서 선택된 동행 조건 카드입니다.
             * Selected 배경, 테두리와 실제 내부 구성을 확인합니다.
             */
            YadanCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                style = YadanCardStyle.SELECTED,
            ) {
                SelectedAccessibilityPreviewContent()
            }

            /*
             * G·02의 개별 필수 약관 카드입니다.
             * 기본 클릭 카드의 외형과 실제 내부 구성을 확인합니다.
             */
            YadanCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                RequiredAgreementPreviewContent(
                    title = "서비스 이용약관",
                )
            }

            /*
             * B·05에서 선택한 기간을 표시하는 카드입니다.
             * 클릭하지 않는 기본 카드의 실제 내부 구성을 확인합니다.
             */
            YadanCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                PeriodSummaryPreviewContent()
            }
        }
    }
}

/**
 * B·02 선택된 어르신 동행 조건 카드의 HTML 구성을 재현합니다.
 */
@Composable
private fun SelectedAccessibilityPreviewContent() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(46.dp)
                    .background(
                        color = YadanPrimary,
                        shape = MaterialTheme.shapes.medium,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Elderly,
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = YadanOnPrimary,
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "어르신 동반",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
            )

            Text(
                text = "계단이 적은 완만한 동선으로 짜요",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.5.sp,
                    ),
                color = YadanTextMuted,
            )
        }

        PreviewCheckMark(size = 27.dp)
    }
}

/**
 * G·02 개별 필수 약관 카드의 HTML 구성을 재현합니다.
 */
@Composable
private fun RequiredAgreementPreviewContent(
    title: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 15.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewCheckMark(size = 23.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
            )

            Box(
                modifier =
                    Modifier
                        .background(
                            color = YadanPrimaryTintStrong,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 3.dp,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "필수",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanPrimaryInk,
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = YadanTextMuted,
        )
    }
}

/**
 * B·05에서 선택한 여행 기간 카드의 HTML 구성을 재현합니다.
 */
@Composable
private fun PeriodSummaryPreviewContent() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "선택한 기간",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.5.sp,
                    ),
                color = YadanTextMuted,
            )

            Text(
                text = "5.22 (금) ~ 5.23 (토)",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
            )
        }

        Box(
            modifier =
                Modifier
                    .background(
                        color = YadanPrimary,
                        shape = YadanPillShape,
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "1박 2일",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanOnPrimary,
            )
        }
    }
}

/**
 * Preview에서 선택 상태를 보여주는 원형 체크 표시입니다.
 */
@Composable
private fun PreviewCheckMark(
    size: Dp,
) {
    Box(
        modifier =
            Modifier
                .size(size)
                .background(
                    color = YadanPrimary,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = YadanOnPrimary,
        )
    }
}
