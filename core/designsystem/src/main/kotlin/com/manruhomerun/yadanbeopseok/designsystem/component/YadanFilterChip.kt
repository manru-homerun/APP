package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 관광지와 검색 결과의 카테고리를 선택할 때 사용하는 필터 칩입니다.
 *
 * HTML의 `.fchip` 디자인에 대응합니다.
 * 선택하지 않은 칩은 흰색 배경과 회색 테두리를 사용하며,
 * 선택한 칩은 진한 배경과 오프화이트 글자를 사용합니다.
 *
 * Material [Surface]를 사용하여 선택 상태, 리플 효과 및
 * 접근성 정보를 제공하고 외형은 HTML 디자인에 맞게 조정합니다.
 *
 * @param text 칩에 표시할 필터 이름입니다.
 * @param selected 현재 필터가 선택되었는지 나타냅니다.
 * @param onClick 칩을 눌렀을 때 실행할 작업입니다.
 * @param modifier 외부에서 배치와 크기를 지정할 Modifier입니다.
 * @param enabled 칩의 활성화 여부입니다.
 */
@Composable
fun YadanFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerColor =
        if (selected) {
            YadanTextPrimary
        } else {
            YadanSurface
        }

    val contentColor =
        if (selected) {
            YadanBackground
        } else {
            YadanTextSecondary
        }

    val borderColor =
        if (selected) {
            YadanTextPrimary
        } else {
            YadanOutline
        }

    /*
     * HTML에는 비활성 필터 칩이 없지만 다른 공통 컴포넌트와
     * 동일하게 비활성 상태에서는 전체 투명도를 낮춥니다.
     */
    val chipAlpha =
        if (enabled) {
            ENABLED_ALPHA
        } else {
            DISABLED_ALPHA
        }

    /*
     * Material Surface는 기본적으로 최소 48dp의 레이아웃 영역을
     * 확보합니다. 필터 칩은 HTML의 약 32px 높이를 그대로 사용해야
     * 하므로 이 컴포넌트 안에서만 최소 레이아웃 크기를 해제합니다.
     *
     * 시스템의 터치 입력 영역 확장은 별도로 적용되므로
     * 보이는 크기만 HTML 디자인에 맞게 유지됩니다.
     */
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 0.dp,
    ) {
        Surface(
            selected = selected,
            onClick = onClick,
            modifier =
                modifier
                    .alpha(chipAlpha)
                    .semantics {
                        role = Role.Checkbox
                    },
            enabled = enabled,
            shape = YadanPillShape,
            color = containerColor,
            contentColor = contentColor,
            border =
                BorderStroke(
                    width = FILTER_CHIP_BORDER_WIDTH,
                    color = borderColor,
                ),
        ) {
            Text(
                text = text,
                modifier =
                    Modifier.padding(
                        horizontal = FILTER_CHIP_HORIZONTAL_PADDING,
                        vertical = FILTER_CHIP_VERTICAL_PADDING,
                    ),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        // HTML의 font-weight: 700에 대응합니다.
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private val FILTER_CHIP_BORDER_WIDTH = 1.5.dp
private val FILTER_CHIP_HORIZONTAL_PADDING = 13.dp
private val FILTER_CHIP_VERTICAL_PADDING = 7.dp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan filter chips",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanFilterChipPreview() {
    YadanbeopseokTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }
        val filters = listOf("전체", "숙박", "행사", "체험", "음식")

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "관광지 카테고리",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            /*
             * 실제 화면에서는 가로 스크롤이 가능한 LazyRow 안에
             * HTML과 같은 7dp 간격으로 배치합니다.
             */
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                filters.forEachIndexed { index, filter ->
                    YadanFilterChip(
                        text = filter,
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                    )
                }
            }

            Text(
                text = "비활성 상태",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                YadanFilterChip(
                    text = "선택 안 됨",
                    selected = false,
                    onClick = {},
                    enabled = false,
                )

                YadanFilterChip(
                    text = "선택됨",
                    selected = true,
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
