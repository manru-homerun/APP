package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryDark
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * B·06에서 추천 여행 코스를 생성하는 동안 표시하는 화면입니다.
 *
 * 표시되는 단계는 사용자에게 작업 내용을 안내하기 위한 시각적 정보이며,
 * 실제 생성 완료 여부는 API 응답과 ViewModel 상태로 판단합니다.
 */
@Composable
fun TravelCourseGeneratingScreen(
    regionName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimaryDark)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Icon(
            imageVector = Icons.Default.SportsBaseball,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = 58.dp,
                    y = -18.dp,
                )
                .size(240.dp),
            tint = YadanOnPrimary.copy(alpha = 0.08f),
        )

        Icon(
            imageVector = Icons.Outlined.Map,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = -42.dp,
                    y = -60.dp,
                )
                .size(150.dp),
            tint = YadanOnPrimary.copy(alpha = 0.06f),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(58.dp)
                    .semantics {
                        contentDescription = "여행 코스 생성 중"
                    },
                color = YadanOnPrimary,
                trackColor = YadanOnPrimary.copy(alpha = 0.22f),
                strokeWidth = 5.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$regionName 여행 코스를\n짜고 있어요",
                style = YadanTypography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanOnPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "취향과 동행 조건을 종합하는 중이에요",
                style = YadanTypography.bodySmall,
                color = YadanOnPrimary.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                GenerationProgressItem(
                    title = "취향·동행 취향 종합",
                    completed = true,
                )

                GenerationProgressItem(
                    title = "베리어프리 조건 필터링",
                    completed = true,
                )

                GenerationProgressItem(
                    title = "주변 관광지·맛집 탐색",
                    current = true,
                )

                GenerationProgressItem(
                    title = "방문 순서 최적화",
                )
            }
        }
    }
}

/**
 * 코스 생성 과정의 개별 진행 단계를 표시합니다.
 */
@Composable
private fun GenerationProgressItem(
    title: String,
    modifier: Modifier = Modifier,
    completed: Boolean = false,
    current: Boolean = false,
) {
    val progressDescription = when {
        completed -> "완료"
        current -> "진행 중"
        else -> "대기 중"
    }

    Surface(
        modifier = modifier.semantics {
            stateDescription = progressDescription
        },
        shape = YadanShapes.medium,
        color = YadanOnPrimary.copy(alpha = 0.1f),
        contentColor = YadanOnPrimary,
        border = BorderStroke(
            width = 1.5.dp,
            color = YadanOnPrimary.copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = if (completed) {
                            YadanOnPrimary
                        } else {
                            YadanOnPrimary.copy(alpha = 0f)
                        },
                        shape = CircleShape,
                    )
                    .border(
                        width = 2.dp,
                        color = if (completed) {
                            YadanOnPrimary
                        } else {
                            YadanOnPrimary.copy(alpha = 0.4f)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = YadanPrimaryDark,
                    )
                }
            }

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = YadanOnPrimary.copy(alpha = 0.92f),
            )

            if (current) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = YadanOnPrimary,
                    trackColor = YadanOnPrimary.copy(alpha = 0.3f),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Preview(
    name = "B06d 코스 생성 중",
    showBackground = true,
    backgroundColor = 0xFF3E7AC2,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseGeneratingScreenPreview() {
    YadanbeopseokTheme {
        TravelCourseGeneratingScreen(regionName = "부산")
    }
}
