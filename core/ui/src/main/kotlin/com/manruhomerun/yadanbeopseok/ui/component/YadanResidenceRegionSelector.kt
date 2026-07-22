package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.ProfileRegion

/**
 * 사용자 프로필에서 거주 지역을 하나 선택하는 드롭다운입니다.
 *
 * HTML의 `.card.drop` 구조에 대응하며 회원가입 여행 프로필과
 * 프로필 수정 화면에서 재사용합니다.
 *
 * 선택 가능한 지역은 [ProfileRegion.residenceOptions]를 사용하므로
 * 거주 지역 데이터에 포함되지 않는 울산과 제주는 표시하지 않습니다.
 *
 * 기존 [YadanCard]를 드롭다운 앵커로 재사용하고,
 * 목록의 표시와 접근성 동작은 Material3 드롭다운이 담당합니다.
 *
 * @param selectedRegion 현재 선택된 거주 지역입니다.
 * 아직 선택하지 않았다면 null을 전달합니다.
 * @param onRegionSelected 거주 지역을 선택했을 때 호출됩니다.
 * @param modifier 컴포넌트의 크기와 배치를 지정합니다.
 * @param placeholder 선택된 지역이 없을 때 표시할 문구입니다.
 * @param enabled 거주 지역 선택 가능 여부입니다.
 */
@Composable
fun YadanResidenceRegionSelector(
    selectedRegion: ProfileRegion?,
    onRegionSelected: (ProfileRegion) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "거주 지역을 선택해주세요",
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    /*
     * 드롭다운이 열린 상태에서 화면이 비활성화되면
     * 목록이 화면에 남지 않도록 닫습니다.
     */
    LaunchedEffect(enabled) {
        if (!enabled) {
            expanded = false
        }
    }

    YadanResidenceRegionSelectorContent(
        selectedRegion = selectedRegion,
        onRegionSelected = onRegionSelected,
        expanded = expanded,
        onExpandedChange = { shouldExpand ->
            expanded = shouldExpand
        },
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
    )
}

/**
 * 거주 지역 선택기의 외형과 펼쳐진 목록을 표시합니다.
 *
 * 펼침 상태를 외부에서 전달받으므로 정적 Preview에서도
 * 드롭다운이 열린 상태를 확인할 수 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YadanResidenceRegionSelectorContent(
    selectedRegion: ProfileRegion?,
    onRegionSelected: (ProfileRegion) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    enabled: Boolean,
) {
    val arrowRotation by animateFloatAsState(
        targetValue =
            if (expanded) {
                180f
            } else {
                0f
            },
        label = "residenceRegionArrowRotation",
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { shouldExpand ->
            if (enabled) {
                onExpandedChange(shouldExpand)
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        /*
         * HTML의 기본 카드 외형을 그대로 사용하기 위해
         * 별도 배경과 테두리 대신 YadanCard를 재사용합니다.
         */
        YadanCard(
            modifier =
                Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = enabled,
                    )
                    .fillMaxWidth()
                    .alpha(
                        if (enabled) {
                            1f
                        } else {
                            0.42f
                        },
                    ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp,
                        ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        selectedRegion?.displayName
                            ?: placeholder,
                    modifier = Modifier.weight(1f),
                    style =
                        YadanTypography.bodyMedium.copy(
                            fontWeight =
                                if (selectedRegion == null) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.ExtraBold
                                },
                        ),
                    color =
                        if (selectedRegion == null) {
                            YadanTextMuted
                        } else {
                            YadanTextPrimary
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                    tint = YadanTextMuted,
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
            shape = YadanShapes.large,
            containerColor = YadanSurface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border =
                BorderStroke(
                    width = 1.5.dp,
                    color = YadanOutline,
                ),
        ) {
            ProfileRegion.residenceOptions.forEach { region ->
                val isSelected =
                    region == selectedRegion

                DropdownMenuItem(
                    text = {
                        Text(
                            text = region.displayName,
                            style =
                                YadanTypography.bodyMedium.copy(
                                    fontWeight =
                                        if (isSelected) {
                                            FontWeight.ExtraBold
                                        } else {
                                            FontWeight.SemiBold
                                        },
                                ),
                            color =
                                if (isSelected) {
                                    YadanPrimaryInk
                                } else {
                                    YadanTextPrimary
                                },
                        )
                    },
                    onClick = {
                        onExpandedChange(false)
                        onRegionSelected(region)
                    },
                    trailingIcon =
                        if (isSelected) {
                            {
                                /*
                                 * 선택된 지역은 텍스트로 이미 안내되므로
                                 * 체크 아이콘은 장식 요소로 처리합니다.
                                 */
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = YadanPrimaryInk,
                                )
                            }
                        } else {
                            null
                        },
                )
            }
        }
    }
}

@Preview(
    name = "Yadan residence region selector",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanResidenceRegionSelectorPreview() {
    var selectedRegion by remember {
        mutableStateOf<ProfileRegion?>(
            ProfileRegion.BUSAN,
        )
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            YadanResidenceRegionSelector(
                selectedRegion = selectedRegion,
                onRegionSelected = { region ->
                    selectedRegion = region
                },
            )

            YadanResidenceRegionSelector(
                selectedRegion = null,
                onRegionSelected = {},
            )

            YadanResidenceRegionSelector(
                selectedRegion = ProfileRegion.SEOUL,
                onRegionSelected = {},
                enabled = false,
            )
        }
    }
}

@Preview(
    name = "Yadan residence region selector - expanded",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 800,
)
@Composable
private fun YadanResidenceRegionSelectorExpandedPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
        ) {
            YadanResidenceRegionSelectorContent(
                selectedRegion = ProfileRegion.BUSAN,
                onRegionSelected = {},
                expanded = true,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = "거주 지역을 선택해주세요",
                enabled = true,
            )
        }
    }
}
