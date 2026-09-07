package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryDark
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Sticker
import com.manruhomerun.yadanbeopseok.model.StickerPack
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationPhase
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerSize
import com.manruhomerun.yadanbeopseok.ui.component.YadanStickerView
import kotlinx.datetime.LocalDate

/**
 * D03 전체 방문 인증 완료 후 획득한 스티커팩을 표시합니다.
 *
 * 사진 꾸미기와 나중에 하기의 실제 화면 이동은 Route에 위임합니다.
 */
@Composable
fun TravelStickerRewardScreen(
    uiState: TravelVerificationUiState,
    onDecoratePhotoClick: (() -> Unit)?,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val travel = uiState.travel
    val stickerPack = uiState.stickerPack
    val hasStickers = stickerPack?.stickers?.isNotEmpty() == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanPrimaryDark)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            StickerRewardContent(
                travel = travel,
                stickerPack = stickerPack,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 26.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YadanButton(
                text = "사진에 붙이기",
                onClick = {
                    onDecoratePhotoClick?.invoke()
                },
                enabled = hasStickers && onDecoratePhotoClick != null,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )

            YadanButton(
                text = "나중에 하기",
                onClick = onLaterClick,
                modifier = Modifier.fillMaxWidth(),
                style = YadanButtonStyle.ON_DARK,
            )
        }
    }
}

@Composable
private fun StickerRewardContent(
    travel: Travel?,
    stickerPack: StickerPack?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (stickerPack != null && stickerPack.stickers.isNotEmpty()) {
            StickerRewardImages(stickerPack = stickerPack)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "스티커 획득!",
            style = YadanTypography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanOnPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = rewardDescription(
                travel = travel,
                stickerPack = stickerPack,
            ),
            style = YadanTypography.bodyMedium,
            color = YadanOnPrimary.copy(alpha = 0.76f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StickerRewardImages(stickerPack: StickerPack) {
    val stickers = stickerPack.stickers

    if (stickers.size == 1) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            YadanStickerView(
                sticker = stickers.first(),
                contentDescription = "${stickerPack.name} 스티커",
                size = YadanStickerSize.LARGE,
            )
        }
        return
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(
            items = stickers,
            key = Sticker::id,
        ) { sticker ->
            YadanStickerView(
                sticker = sticker,
                contentDescription = "${stickerPack.name} 스티커",
                size = YadanStickerSize.LARGE,
            )
        }
    }
}

private fun rewardDescription(
    travel: Travel?,
    stickerPack: StickerPack?,
): String {
    val stickerPackName = stickerPack
        ?.name
        ?.takeIf(String::isNotBlank)
        ?: "여행 스티커"

    if (travel == null) {
        return "${stickerPackName}을 받았어요"
    }

    return "${travel.region.displayName} 코스 " +
        "${travel.certificationTargetCount}곳을 모두 인증해\n" +
        "${stickerPackName}을 받았어요"
}

@Preview(
    name = "D03 스티커 획득",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun TravelStickerRewardScreenPreview() {
    YadanbeopseokTheme {
        TravelStickerRewardScreen(
            uiState = stickerRewardPreviewState(),
            onDecoratePhotoClick = {},
            onLaterClick = {},
        )
    }
}

private fun stickerRewardPreviewState(): TravelVerificationUiState {
    val stickerPack = StickerPack(
        id = "pack-busan",
        name = "사직 한정 스티커팩",
        stickers = listOf(
            Sticker(
                id = "sticker-sajik",
                stickerPackId = "pack-busan",
                imageUrl = "",
            ),
        ),
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
        certifiedSpotsCount = 5,
        days = emptyList(),
        status = TravelStatus.ACTIVE,
    )

    return TravelVerificationUiState(
        travel = travel,
        stickerPack = stickerPack,
        phase = TravelVerificationPhase.REWARD,
    )
}
