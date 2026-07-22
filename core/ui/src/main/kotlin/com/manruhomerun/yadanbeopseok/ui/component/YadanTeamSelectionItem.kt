package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * 구단 선택 항목의 화면별 표시 형태입니다.
 */
enum class YadanTeamSelectionItemStyle {
    /**
     * 회원가입 응원 구단 선택 화면에서 사용하는 가로 카드입니다.
     *
     * 로고, 구단명, 보조 구단명과 선택 아이콘을 표시합니다.
     */
    DEFAULT,

    /**
     * 프로필 수정 화면에서 사용하는 작은 세로 카드입니다.
     *
     * 5열 배치를 위해 로고와 짧은 구단명만 표시합니다.
     */
    COMPACT,
}

/**
 * 응원 구단을 선택할 때 사용하는 구단 선택 항목입니다.
 *
 * HTML의 `.team`, `.team.sel`, `.pe-team`, `.pe-team.sel` 구조에 대응합니다.
 * G·04 응원 구단 선택과 H·02 프로필 수정 화면에서 재사용합니다.
 *
 * 카드 외형은 기존 [YadanCard]를 사용하고,
 * 구단 로고는 기존 [YadanTeamLogo]를 재사용합니다.
 *
 * 그리드의 열 개수와 항목 간격은 이 컴포넌트가 아니라
 * 각 화면의 LazyVerticalGrid에서 결정합니다.
 *
 * 여러 구단 항목을 표시하는 부모에는
 * `Modifier.selectableGroup()`을 적용해야 합니다.
 *
 * @param team 표시할 KBO 구단입니다.
 * @param selected 현재 구단의 선택 여부입니다.
 * @param onClick 구단을 선택했을 때 실행할 작업입니다.
 * @param modifier 항목의 크기와 배치를 지정합니다.
 * @param style 화면에 따른 구단 선택 항목의 표시 형태입니다.
 * @param enabled 구단 선택 가능 여부입니다.
 */
@Composable
fun YadanTeamSelectionItem(
    team: KboTeam,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YadanTeamSelectionItemStyle =
        YadanTeamSelectionItemStyle.DEFAULT,
    enabled: Boolean = true,
) {
    /*
     * 구단은 한 번에 하나만 선택하므로 Card의 버튼 의미 대신
     * RadioButton 역할을 갖는 단일 선택 항목으로 제공합니다.
     *
     * 클릭 처리는 Modifier.selectable이 담당하므로
     * 클릭 가능한 YadanCard 오버로드를 사용하지 않습니다.
     */
    YadanCard(
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
                .clip(MaterialTheme.shapes.large)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        style =
            if (selected) {
                YadanCardStyle.SELECTED
            } else {
                YadanCardStyle.DEFAULT
            },
    ) {
        when (style) {
            YadanTeamSelectionItemStyle.DEFAULT -> {
                YadanDefaultTeamSelectionContent(
                    team = team,
                    selected = selected,
                )
            }

            YadanTeamSelectionItemStyle.COMPACT -> {
                YadanCompactTeamSelectionContent(
                    team = team,
                )
            }
        }
    }
}

/**
 * G·04 회원가입 응원 구단 선택 화면의 가로 카드 내용입니다.
 */
@Composable
private fun YadanDefaultTeamSelectionContent(
    team: KboTeam,
    selected: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 구단명이 바로 옆에 표시되므로 로고는 장식 요소로 처리하여
         * TalkBack에서 구단명이 중복 안내되지 않게 합니다.
         */
        YadanTeamLogo(
            team = team,
            contentDescription = null,
            size = 38.dp,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = team.displayName,
                style =
                    YadanTypography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = team.shortDescription,
                style =
                    YadanTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = YadanTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (selected) {
            /*
             * 선택 상태는 카드의 접근성 상태로 이미 제공하므로
             * 체크 아이콘에는 별도의 설명을 지정하지 않습니다.
             */
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = YadanPrimaryInk,
            )
        }
    }
}

/**
 * H·02 프로필 수정 화면의 5열 세로 카드 내용입니다.
 *
 * HTML의 28px 로고, 6px 간격과 10.5px 구단명을 기준으로 구성합니다.
 * 선택 상태는 공통 카드의 배경과 테두리로 표시합니다.
 */
@Composable
private fun YadanCompactTeamSelectionContent(
    team: KboTeam,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 70.dp)
                .padding(
                    horizontal = 4.dp,
                    vertical = 11.dp,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        /*
         * 구단명이 아래에 표시되므로 로고는 장식 요소로 처리합니다.
         */
        YadanTeamLogo(
            team = team,
            contentDescription = null,
            size = 28.dp,
        )

        Text(
            text = team.displayName,
            style =
                YadanTypography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            color = YadanTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 전체 구단명에서 HTML의 보조 구단명에 해당하는 부분을 반환합니다.
 *
 * 예: "롯데 자이언츠"는 "자이언츠",
 * "KIA 타이거즈"는 "타이거즈"로 표시합니다.
 */
private val KboTeam.shortDescription: String
    get() =
        fullName
            .substringAfter(
                delimiter = " ",
                missingDelimiterValue = fullName,
            )
            .trim()

@Preview(
    name = "Yadan team selection items",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTeamSelectionItemPreview() {
    var selectedTeam by remember {
        mutableStateOf(KboTeam.LOTTE)
    }

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
             * G·04 회원가입 화면의 2열 구단 선택 카드입니다.
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KboTeam.entries
                    .chunked(2)
                    .forEach { teams ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                        ) {
                            teams.forEach { team ->
                                YadanTeamSelectionItem(
                                    team = team,
                                    selected = selectedTeam == team,
                                    onClick = {
                                        selectedTeam = team
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
            }

            /*
             * H·02 프로필 수정 화면의 5열 구단 선택 카드입니다.
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KboTeam.entries
                    .chunked(5)
                    .forEach { teams ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                        ) {
                            teams.forEach { team ->
                                YadanTeamSelectionItem(
                                    team = team,
                                    selected = selectedTeam == team,
                                    onClick = {
                                        selectedTeam = team
                                    },
                                    modifier = Modifier.weight(1f),
                                    style =
                                        YadanTeamSelectionItemStyle.COMPACT,
                                )
                            }
                        }
                    }
            }
        }
    }
}
