package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCheckbox
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTintStrong
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.TravelCompanionCondition

/**
 * B·02 여행 만들기의 동행 조건 선택 화면입니다.
 */
@Composable
fun TravelCompanionConditionScreen(
    selectedConditions: Set<TravelCompanionCondition>,
    isNextEnabled: Boolean,
    onConditionClick: (TravelCompanionCondition) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TravelCreationScaffold(
        currentStep = TravelCreationStep.COMPANION_CONDITION,
        title = "배려가 필요한 동행이 있나요?",
        description = "선택하면 베리어프리 정보로 동선을 맞춰드려요. 복수 선택할 수 있어요.",
        onNavigationClick = onBackClick,
        modifier = modifier,
        bottomBar = {
            YadanButton(
                text = "다음",
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 11.dp,
                        end = 24.dp,
                        bottom = 20.dp,
                    ),
                enabled = isNextEnabled,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        },
    ) {
        item(key = "companion_conditions") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TravelCompanionCondition.entries.forEach { condition ->
                    CompanionConditionCard(
                        condition = condition,
                        selected = condition in selectedConditions,
                        onClick = {
                            onConditionClick(condition)
                        },
                    )
                }
            }
        }
    }
}

/**
 * 하나의 동행 조건과 선택 상태를 표시합니다.
 */
@Composable
private fun CompanionConditionCard(
    condition: TravelCompanionCondition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardStyle = if (selected) {
        YadanCardStyle.SELECTED
    } else {
        YadanCardStyle.DEFAULT
    }

    val iconContainerColor = if (selected) {
        YadanPrimary
    } else {
        YadanPrimaryTintStrong
    }

    val iconColor = if (selected) {
        YadanOnPrimary
    } else {
        YadanPrimaryInk
    }

    YadanCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
            },
        style = cardStyle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = iconContainerColor,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = condition.icon,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = iconColor,
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = condition.title,
                    style = YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )

                Text(
                    text = condition.description,
                    style = YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = YadanTextMuted,
                )
            }

            YadanCheckbox(
                checked = selected,
                onCheckedChange = null,
            )
        }
    }
}

private val TravelCompanionCondition.title: String
    get() = when (this) {
        TravelCompanionCondition.CHILD -> "아이 동반"
        TravelCompanionCondition.SENIOR -> "어르신 동반"
        TravelCompanionCondition.WHEELCHAIR -> "휠체어 이용"
    }

private val TravelCompanionCondition.description: String
    get() = when (this) {
        TravelCompanionCondition.CHILD -> "유아차·수유실을 우선 안내해요"
        TravelCompanionCondition.SENIOR -> "계단이 적은 완만한 동선으로 짜요"
        TravelCompanionCondition.WHEELCHAIR -> "휠체어 접근 가능한 곳만 담아요"
    }

private val TravelCompanionCondition.icon: ImageVector
    get() = when (this) {
        TravelCompanionCondition.CHILD -> Icons.Default.ChildCare
        TravelCompanionCondition.SENIOR -> Icons.Default.Elderly
        TravelCompanionCondition.WHEELCHAIR -> Icons.AutoMirrored.Filled.Accessible
    }

@Preview(
    name = "B02 동행 조건",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCompanionConditionScreenPreview() {
    YadanbeopseokTheme {
        TravelCompanionConditionScreen(
            selectedConditions = setOf(TravelCompanionCondition.SENIOR),
            isNextEnabled = true,
            onConditionClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
