package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 화면의 콘텐츠 영역을 구분하는 공통 섹션 제목입니다.
 *
 * HTML의 `.sec`, `.sec-l`, `.sec-t` 형태를 기반으로 하지만
 * 글자 크기와 줄 높이는 공통 [YadanTypography]를 사용합니다.
 *
 * 오른쪽에는 개수 배지, 보조 문구 또는 화면별 작업을 전달할 수 있습니다.
 * 화면마다 다른 위아래 여백은 포함하지 않으며 사용하는 화면에서 지정합니다.
 *
 * @param title 섹션 제목입니다.
 * @param modifier 섹션 제목의 외부 배치에 사용할 Modifier입니다.
 * @param trailingContent 제목 오른쪽에 표시할 선택적 콘텐츠입니다.
 */
@Composable
fun YadanSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 오른쪽 콘텐츠가 길어져도 제목과 겹치지 않도록
         * 남은 공간 안에서 한 줄 말줄임 처리합니다.
         */
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style =
                YadanTypography.titleSmall.copy(
                    /*
                     * 글자 크기와 줄 높이는 공통 토큰을 사용하고,
                     * HTML 섹션 제목의 강조 굵기만 적용합니다.
                     */
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (trailingContent != null) {
            Box(
                modifier =
                    Modifier.padding(
                        start = HEADER_CONTENT_SPACING,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                trailingContent()
            }
        }
    }
}

/**
 * 섹션 제목 오른쪽에 표시하는 짧은 보조 문구입니다.
 *
 * HTML의 `.sec-s` 형태를 기반으로 하며
 * 크기와 줄 높이는 [YadanTypography.labelSmall]을 사용합니다.
 *
 * @param text 표시할 보조 문구입니다.
 * @param modifier 보조 문구의 배치에 사용할 Modifier입니다.
 */
@Composable
fun YadanSectionMetaText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style =
            YadanTypography.labelSmall.copy(
                /*
                 * 크기와 줄 높이는 공통 토큰을 사용하고
                 * HTML의 font-weight: 600만 적용합니다.
                 */
                fontWeight = FontWeight.SemiBold,
            ),
        color = YadanTextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 섹션 제목 오른쪽에 표시하는 개수 배지입니다.
 *
 * HTML의 `.count`, `.count.zero` 형태를 기반으로 합니다.
 * 개수가 0이면 회색 배경과 글자로 표시합니다.
 *
 * 글자 크기와 줄 높이는 [YadanTypography.bodySmall]을 사용합니다.
 *
 * @param count 배지에 표시할 개수입니다.
 * @param modifier 개수 배지의 배치에 사용할 Modifier입니다.
 */
@Composable
fun YadanSectionCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val isZero = count == 0

    val containerColor =
        if (isZero) {
            YadanDivider
        } else {
            YadanPrimaryTintStrong
        }

    val contentColor =
        if (isZero) {
            YadanTextMuted
        } else {
            YadanPrimary
        }

    Surface(
        modifier = modifier,
        shape = YadanPillShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = count.toString(),
            modifier =
                Modifier.padding(
                    horizontal = COUNT_HORIZONTAL_PADDING,
                    vertical = COUNT_VERTICAL_PADDING,
                ),
            style =
                YadanTypography.bodySmall.copy(
                    /*
                     * 크기와 줄 높이는 공통 토큰을 사용하고
                     * 개수를 강조하는 굵기만 적용합니다.
                     */
                    fontWeight = FontWeight.ExtraBold,
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/*
 * 제목과 오른쪽 콘텐츠 사이의 최소 간격입니다.
 */
private val HEADER_CONTENT_SPACING = 8.dp

/*
 * 개수 배지에서만 사용하는 내부 여백입니다.
 */
private val COUNT_HORIZONTAL_PADDING = 9.dp
private val COUNT_VERTICAL_PADDING = 2.dp

@Preview(
    name = "Yadan section headers",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanSectionHeaderPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            /*
             * 홈 화면의 내 원정 여행 영역입니다.
             */
            YadanSectionHeader(
                title = "내 원정 여행",
                trailingContent = {
                    YadanSectionCountBadge(
                        count = 2,
                    )
                },
            )

            /*
             * 경기 일정 화면의 월별 일정 영역입니다.
             */
            YadanSectionHeader(
                title = "2026년 5월",
                trailingContent = {
                    YadanSectionMetaText(
                        text = "롯데 경기",
                    )
                },
            )

            /*
             * 추천 여행 결과의 방문 순서 영역입니다.
             */
            YadanSectionHeader(
                title = "방문 순서",
            )

            /*
             * HTML에 정의된 0개 배지 상태입니다.
             * 실제 홈 화면에서는 화면에서 조건에 따라 숨길 수 있습니다.
             */
            YadanSectionHeader(
                title = "내 원정 여행",
                trailingContent = {
                    YadanSectionCountBadge(
                        count = 0,
                    )
                },
            )
        }
    }
}
