package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 여행 방문 인증 진행률의 시각적 유형입니다.
 */
enum class YadanTravelProgressStyle {
    /** 흰색 또는 연한 배경 위에서 사용하는 기본 유형입니다. */
    DEFAULT,

    /** 여행 히어로 카드처럼 어두운 배경 위에서 사용하는 유형입니다. */
    ON_DARK,
}

/**
 * 여행 장소의 방문 인증 진행률을 표시합니다.
 *
 * HTML의 `.tc-prog`, `.tp-row`, `.pbar` 구조에 대응합니다.
 *
 * @param certifiedPlaceCount 방문 인증을 완료한 장소 개수입니다.
 * @param totalPlaceCount 방문 인증 대상 장소의 전체 개수입니다.
 * @param modifier 진행률 컴포넌트의 크기와 배치를 지정합니다.
 * @param style 밝은 배경 또는 어두운 배경의 색상 유형입니다.
 * @param label 진행률 왼쪽에 표시할 문구입니다.
 * null이면 완료 개수만 왼쪽에 표시합니다.
 */
@Composable
fun YadanTravelProgress(
    certifiedPlaceCount: Int,
    totalPlaceCount: Int,
    modifier: Modifier = Modifier,
    style: YadanTravelProgressStyle = YadanTravelProgressStyle.DEFAULT,
    label: String? = "방문 인증",
) {
    /*
     * 서버 응답이 잘못되더라도 음수 또는 전체 개수를 초과하는
     * 진행률이 화면에 표시되지 않도록 보정합니다.
     */
    val safeTotalCount =
        totalPlaceCount.coerceAtLeast(0)
    val safeCertifiedCount =
        certifiedPlaceCount.coerceIn(
            minimumValue = 0,
            maximumValue = safeTotalCount,
        )

    val progress =
        if (safeTotalCount > 0) {
            safeCertifiedCount
                .toFloat()
                .div(safeTotalCount)
        } else {
            0f
        }

    val labelColor =
        when (style) {
            YadanTravelProgressStyle.DEFAULT ->
                YadanTextMuted

            YadanTravelProgressStyle.ON_DARK ->
                YadanOnPrimary.copy(alpha = 0.88f)
        }

    val countColor =
        when (style) {
            YadanTravelProgressStyle.DEFAULT ->
                YadanPrimary

            YadanTravelProgressStyle.ON_DARK ->
                YadanOnPrimary
        }

    val trackColor =
        when (style) {
            YadanTravelProgressStyle.DEFAULT ->
                YadanDivider

            YadanTravelProgressStyle.ON_DARK ->
                Color.Black.copy(alpha = 0.26f)
        }

    val progressColor =
        when (style) {
            YadanTravelProgressStyle.DEFAULT ->
                YadanPrimary

            YadanTravelProgressStyle.ON_DARK ->
                YadanOnPrimary
        }

    val progressStateDescription =
        if (safeTotalCount > 0) {
            "$safeCertifiedCount / ${safeTotalCount}곳 방문 인증 완료"
        } else {
            "방문 인증 대상 장소 없음"
        }

    Column(
        modifier =
            modifier.semantics(
                mergeDescendants = true,
            ) {
                stateDescription =
                    progressStateDescription
                progressBarRangeInfo =
                    ProgressBarRangeInfo(
                        current = progress,
                        range = 0f..1f,
                    )
            },
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                if (label == null) {
                    Arrangement.Start
                } else {
                    Arrangement.SpaceBetween
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style =
                        YadanTypography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = labelColor,
                )
            }

            Text(
                text =
                    "$safeCertifiedCount / " +
                        "${safeTotalCount}곳",
                style =
                    YadanTypography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = countColor,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(YadanPillShape)
                    .background(trackColor),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(progressColor),
            )
        }
    }
}

@Preview(
    name = "Travel progress - Default",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTravelProgressDefaultPreview() {
    YadanbeopseokTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(YadanSurface)
                        .padding(14.dp),
            ) {
                YadanTravelProgress(
                    certifiedPlaceCount = 1,
                    totalPlaceCount = 5,
                    label = null,
                )
            }
        }
    }
}

@Preview(
    name = "Travel progress - On dark",
    showBackground = true,
    backgroundColor = 0xFF52A1FF,
)
@Composable
private fun YadanTravelProgressOnDarkPreview() {
    YadanbeopseokTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanPrimaryGradient)
                    .padding(20.dp),
        ) {
            YadanTravelProgress(
                certifiedPlaceCount = 1,
                totalPlaceCount = 2,
                style = YadanTravelProgressStyle.ON_DARK,
            )
        }
    }
}
