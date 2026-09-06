package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationPhase
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelProgress
import kotlinx.datetime.LocalDate

/**
 * D02b 방문 인증 완료 화면입니다.
 *
 * 인증된 관광지와 갱신된 여행 전체 인증 진행률을 표시합니다.
 * 자동 전환 시점과 다음 화면 이동은 Route에서 처리합니다.
 */
@Composable
fun TravelVerificationCompletedScreen(
    uiState: TravelVerificationUiState,
    modifier: Modifier = Modifier,
) {
    val travel = uiState.travel
    val certifiedCount = travel?.certifiedSpotsCount ?: 0
    val totalCount = travel?.certificationTargetCount ?: 0
    val remainingCount = (totalCount - certifiedCount).coerceAtLeast(0)
    val isAllCertified = totalCount > 0 && certifiedCount == totalCount

    val spotName = uiState.certification
        ?.spotName
        ?.takeIf(String::isNotBlank)
        ?: uiState.targetSpot
            ?.spot
            ?.name
            ?.takeIf(String::isNotBlank)
        ?: "관광지"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimaryTint)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VerificationCompletedBadge()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "$spotName\n인증 완료!",
                    style = YadanTypography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanTextPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "방문이 기록됐어요!",
                    style = YadanTypography.bodySmall,
                    color = YadanTextMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                YadanCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        YadanTravelProgress(
                            certifiedPlaceCount = certifiedCount,
                            totalPlaceCount = totalCount,
                            label = "여행 전체 인증",
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (remainingCount > 0) {
                                "${remainingCount}곳 더 인증하면 스티커를 받아요"
                            } else {
                                "모든 관광지 인증을 완료했어요"
                            },
                            style = YadanTypography.labelSmall,
                            color = YadanPrimaryInk,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        VerificationCompletedTransitionIndicator(
            isAllCertified = isAllCertified,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 28.dp,
                    vertical = 30.dp,
                ),
        )
    }
}

@Composable
private fun VerificationCompletedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(128.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .border(
                    width = 2.dp,
                    color = YadanPrimary.copy(alpha = 0.14f),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .size(102.dp)
                .border(
                    width = 2.dp,
                    color = YadanPrimary.copy(alpha = 0.28f),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .size(78.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                )
                .background(
                    color = YadanPrimary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = YadanSurface,
            )
        }
    }
}

@Composable
private fun VerificationCompletedTransitionIndicator(
    isAllCertified: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = YadanPrimary,
            strokeWidth = 2.dp,
        )

        Text(
            text = if (isAllCertified) {
                "잠시 후 획득한 스티커를 확인해요"
            } else {
                "잠시 후 일정 화면으로 돌아가요"
            },
            style = YadanTypography.labelSmall,
            color = YadanTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "D02b 방문 인증 완료",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun TravelVerificationCompletedScreenPreview() {
    YadanbeopseokTheme {
        TravelVerificationCompletedScreen(
            uiState = verificationCompletedPreviewState(),
        )
    }
}

private fun verificationCompletedPreviewState(): TravelVerificationUiState {
    val spot = TravelSpot(
        id = "132159",
        name = "감천문화마을",
        address = "부산광역시 사하구 감내2로 203",
        region = Region.BUSAN,
        category = TravelSpotCategory.CULTURE,
    )

    val travel = Travel(
        id = "1",
        startDate = LocalDate(2026, 9, 5),
        endDate = LocalDate(2026, 9, 6),
        baseballGame = TravelBaseballGame(
            id = "123",
            day = 2,
            baseballGameAfterIdx = 0,
        ),
        name = "부산 사직 직관 여행",
        region = Region.BUSAN,
        friends = emptyList(),
        isLeader = true,
        themeIds = emptyList(),
        certificationTargetCount = 5,
        certifiedSpotsCount = 2,
        days = emptyList(),
        status = TravelStatus.ACTIVE,
    )

    return TravelVerificationUiState(
        travel = travel,
        targetSpot = TravelSpotDetail(spot = spot),
        travelDay = 1,
        phase = TravelVerificationPhase.VERIFIED,
    )
}
