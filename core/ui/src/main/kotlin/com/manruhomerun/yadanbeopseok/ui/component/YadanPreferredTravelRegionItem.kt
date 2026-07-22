package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.ProfileRegion

/**
 * 사용자 프로필에서 선호 여행 지역 하나를 선택하는 컴포넌트입니다.
 *
 * HTML의 `.rg`, `.rg.sel` 지역 선택 타일에 대응합니다.
 * 회원가입 여행 프로필과 프로필 수정 화면에서 재사용합니다.
 *
 * 복수 선택 상태와 그리드 배치는 화면에서 관리하고,
 * 이 컴포넌트는 지역 하나의 선택 상태와 클릭 처리만 담당합니다.
 *
 * 화면에서는 [ProfileRegion.preferredTravelOptions]를 사용하여
 * 선호 여행 지역으로 선택할 수 있는 지역만 전달해야 합니다.
 *
 * @param region 표시할 선호 여행 지역입니다.
 * @param selected 현재 지역의 선택 여부입니다.
 * @param onClick 지역을 눌렀을 때 실행할 작업입니다.
 * @param modifier 그리드 안에서 항목의 크기와 배치를 지정합니다.
 * @param enabled 지역 선택 가능 여부입니다.
 */
@Composable
fun YadanPreferredTravelRegionItem(
    region: ProfileRegion,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanSurface
        }

    val contentColor =
        if (selected) {
            YadanOnPrimary
        } else {
            YadanTextPrimary
        }

    val borderColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanOutline
        }

    /*
     * 선호 여행 지역은 여러 개를 선택할 수 있으므로
     * 카드 전체를 Checkbox 역할을 가진 복수 선택 항목으로 제공합니다.
     *
     * 클릭 처리는 Modifier.toggleable이 담당하므로
     * 클릭 가능한 Surface 오버로드를 사용하지 않습니다.
     */
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .clip(YadanShapes.medium)
                .toggleable(
                    value = selected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = {
                        onClick()
                    },
                ),
        shape = YadanShapes.medium,
        color = containerColor,
        contentColor = contentColor,
        border =
            BorderStroke(
                width = 1.5.dp,
                color = borderColor,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 11.dp),
            horizontalArrangement =
                Arrangement.spacedBy(
                    space = 3.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                /*
                 * 카드 전체에서 선택 상태를 제공하므로
                 * 체크 아이콘은 장식 요소로 처리합니다.
                 */
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
            }

            Text(
                text = region.displayName,
                style =
                    YadanTypography.bodySmall.copy(
                        fontWeight =
                            if (selected) {
                                FontWeight.ExtraBold
                            } else {
                                FontWeight.Bold
                            },
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
        }
    }
}

@Preview(
    name = "Yadan preferred travel region items",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanPreferredTravelRegionItemPreview() {
    var selectedRegions by remember {
        mutableStateOf(
            setOf(
                ProfileRegion.BUSAN,
                ProfileRegion.JEJU,
            ),
        )
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(REGION_GRID_SPACING),
        ) {
            ProfileRegion.preferredTravelOptions
                .chunked(REGION_COLUMN_COUNT)
                .forEach { regions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(REGION_GRID_SPACING),
                    ) {
                        regions.forEach { region ->
                            YadanPreferredTravelRegionItem(
                                region = region,
                                selected = region in selectedRegions,
                                onClick = {
                                    selectedRegions =
                                        if (region in selectedRegions) {
                                            selectedRegions - region
                                        } else {
                                            selectedRegions + region
                                        }
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        /*
                         * 마지막 행도 위쪽 행과 같은 5열 너비를 유지합니다.
                         */
                        repeat(REGION_COLUMN_COUNT - regions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
        }
    }
}

private const val REGION_COLUMN_COUNT = 5
private val REGION_GRID_SPACING = 7.dp
