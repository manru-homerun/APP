package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCheckbox
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCheckboxSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 신규 회원이 필수 약관에 동의하는 화면입니다.
 *
 * 약관 동의 상태는 이후 ViewModel에서 관리할 수 있도록
 * 화면 외부에서 전달받습니다.
 */
@Composable
fun TermsAgreementScreen(
    isServiceTermsAgreed: Boolean,
    isPrivacyAgreementAgreed: Boolean,
    onServiceTermsAgreementChange: (Boolean) -> Unit,
    onPrivacyAgreementChange: (Boolean) -> Unit,
    onAllAgreementChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAllAgreed = isServiceTermsAgreed && isPrivacyAgreementAgreed

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(YadanBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "약관 동의",
            onNavigationClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 26.dp,
                    ),
        ) {
            Text(
                text = "서비스 이용에 동의해주세요",
                style =
                    YadanTypography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            AllAgreementCard(
                checked = isAllAgreed,
                onCheckedChange = onAllAgreementChange,
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 13.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                RequiredAgreementCard(
                    title = "서비스 이용약관",
                    checked = isServiceTermsAgreed,
                    onCheckedChange = onServiceTermsAgreementChange,
                )

                RequiredAgreementCard(
                    title = "개인정보 수집·이용",
                    checked = isPrivacyAgreementAgreed,
                    onCheckedChange = onPrivacyAgreementChange,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(18.dp))

            YadanButton(
                text = "동의하고 계속",
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAllAgreed,
                trailingIcon = {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = "만 14세 이상만 가입할 수 있어요 · 필수 항목 동의 시 가입 완료",
                modifier = Modifier.fillMaxWidth(),
                style =
                    YadanTypography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = YadanTextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 모든 필수 약관의 동의 상태를 한 번에 변경하는 카드입니다.
 */
@Composable
private fun AllAgreementCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        onClick = {
            onCheckedChange(!checked)
        },
        modifier =
            modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Checkbox
                    stateDescription =
                        if (checked) {
                            "전체 약관 동의됨"
                        } else {
                            "전체 약관 동의 안 됨"
                        }
                },
        style =
            if (checked) {
                YadanCardStyle.SELECTED
            } else {
                YadanCardStyle.DEFAULT
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 17.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YadanCheckbox(
                checked = checked,
                onCheckedChange = null,
            )

            Text(
                text = "전체 동의",
                modifier = Modifier.weight(1f),
                style =
                    YadanTypography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )
        }
    }
}

/**
 * 개별 필수 약관의 동의 상태를 표시하고 변경하는 카드입니다.
 */
@Composable
private fun RequiredAgreementCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        onClick = {
            onCheckedChange(!checked)
        },
        modifier =
            modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Checkbox
                    stateDescription =
                        if (checked) {
                            "동의됨"
                        } else {
                            "동의 안 됨"
                        }
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 15.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YadanCheckbox(
                checked = checked,
                onCheckedChange = null,
                size = YadanCheckboxSize.SMALL,
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style =
                        YadanTypography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanTextPrimary,
                )

                YadanStatusChip(
                    text = "필수",
                    style = YadanStatusChipStyle.TINTED,
                    size = YadanStatusChipSize.SMALL,
                )
            }

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = YadanTextMuted,
            )
        }
    }
}

@Preview(
    name = "Terms agreement",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TermsAgreementScreenPreview() {
    YadanbeopseokTheme {
        TermsAgreementScreen(
            isServiceTermsAgreed = true,
            isPrivacyAgreementAgreed = true,
            onServiceTermsAgreementChange = {},
            onPrivacyAgreementChange = {},
            onAllAgreementChange = {},
            onBackClick = {},
            onContinueClick = {},
        )
    }
}
