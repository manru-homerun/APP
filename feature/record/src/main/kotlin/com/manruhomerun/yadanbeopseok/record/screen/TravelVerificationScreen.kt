package com.manruhomerun.yadanbeopseok.record.screen

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.record.component.TravelVerificationMap
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationResult
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationPhase
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationRetryAction
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelProgress
import com.manruhomerun.yadanbeopseok.ui.component.displayTitle
import kotlinx.datetime.LocalDate

/**
 * D02 방문 인증 화면입니다.
 *
 * 여행 정보, 현재 위치와 인증 진행률을 표시합니다.
 * 권한 요청, 위치 설정, 인증 요청과 화면 이동은 Route에 위임합니다.
 * 인증 완료와 스티커 획득 화면은 Route에서 별도로 전환합니다.
 */
@Composable
fun TravelVerificationScreen(
    uiState: TravelVerificationUiState,
    onBackClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onRetryClick: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val travel = uiState.travel
    val targetSpot = uiState.targetSpot
    val phase = uiState.phase
    val location = (uiState.locationResult as? CurrentLocationResult.Success)?.location

    val isBusy = when (phase) {
        TravelVerificationPhase.LOADING,
        TravelVerificationPhase.LOCATING,
        TravelVerificationPhase.SUBMITTING,
        TravelVerificationPhase.REFRESHING_TRAVEL,
        TravelVerificationPhase.LOADING_STICKERS -> true

        else -> false
    }

    val canVerify = phase == TravelVerificationPhase.READY &&
        travel != null && targetSpot != null &&
        location != null && uiState.certification == null

    val canRetry = phase == TravelVerificationPhase.ERROR && uiState.retryAction != null
    val isLocationRetry = canRetry &&
        uiState.retryAction == TravelVerificationRetryAction.LOAD_LOCATION

    val needsPermission = isLocationRetry &&
        uiState.locationResult == CurrentLocationResult.PermissionRequired

    val needsLocationSettings = isLocationRetry &&
        uiState.locationResult == CurrentLocationResult.LocationDisabled

    val buttonText = when {
        needsPermission -> "위치 권한 허용"
        needsLocationSettings -> "위치 서비스 설정"
        canRetry -> "다시 시도"
        phase == TravelVerificationPhase.LOADING -> "인증 정보 확인 중"
        phase == TravelVerificationPhase.LOCATING -> "현재 위치 확인 중"
        phase == TravelVerificationPhase.SUBMITTING -> "방문 인증 중"
        phase == TravelVerificationPhase.REFRESHING_TRAVEL -> "인증 결과 확인 중"
        phase == TravelVerificationPhase.LOADING_STICKERS -> "스티커 확인 중"
        phase == TravelVerificationPhase.ERROR -> "인증할 수 없습니다"
        else -> "이곳 방문 인증하기"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "방문 인증",
            onNavigationClick = onBackClick,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (travel != null && targetSpot != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        YadanStatusChip(
                            text = "진행 중",
                            style = YadanStatusChipStyle.LIVE,
                            size = YadanStatusChipSize.SMALL,
                        )

                        val dayText = uiState.travelDay?.let { " · DAY $it" }.orEmpty()

                        Text(
                            text = "${travel.displayTitle()}$dayText",
                            modifier = Modifier.weight(1f),
                            style = YadanTypography.labelMedium,
                            color = YadanTextPrimary,
                        )
                    }

                    YadanTravelProgress(
                        certifiedPlaceCount = travel.certifiedSpotsCount,
                        totalPlaceCount = travel.certificationTargetCount,
                        label = "여행 전체 인증",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    VerificationLocationMap(
                        location = location,
                        isLocating = phase == TravelVerificationPhase.LOCATING,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )

                    YadanCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = YadanPrimary,
                                modifier = Modifier.size(28.dp),
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = targetSpot.spot.name,
                                    style = YadanTypography.titleSmall,
                                    color = YadanTextPrimary,
                                )

                                Text(
                                    text = if (location != null) {
                                        "현재 위치를 기준으로 방문 인증을 요청합니다."
                                    } else {
                                        "방문 인증에 사용할 현재 위치를 확인해주세요."
                                    },
                                    style = YadanTypography.bodySmall,
                                    color = YadanTextMuted,
                                )
                            }
                        }
                    }

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = YadanTypography.bodyMedium,
                            color = YadanError,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else if (phase == TravelVerificationPhase.LOADING) {
                CircularProgressIndicator(
                    color = YadanPrimary,
                    modifier = Modifier.size(32.dp),
                )
            } else {
                Text(
                    text = uiState.errorMessage ?: "방문 인증 정보를 확인할 수 없습니다.",
                    modifier = Modifier.padding(24.dp),
                    style = YadanTypography.bodyMedium,
                    color = YadanTextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }

        YadanButton(
            text = buttonText,
            onClick = {
                when {
                    needsPermission -> onRequestLocationPermission()
                    needsLocationSettings -> onOpenLocationSettings()
                    canRetry -> onRetryClick()
                    canVerify -> onVerifyClick()
                }
            },
            enabled = canVerify || canRetry,
            isLoading = isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

/**
 * 지도 표시 실패는 인증 API 실패와 구분합니다.
 * 지도 재시도는 지도만 다시 생성하며, 방문 인증을 요청하지 않습니다.
 */
@Composable
private fun VerificationLocationMap(
    location: Location?,
    isLocating: Boolean,
    modifier: Modifier = Modifier,
) {
    var hasMapError by remember(location) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(YadanPrimaryTint),
        contentAlignment = Alignment.Center,
    ) {
        when {
            location == null -> {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            color = YadanPrimary,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Text(
                        text = if (isLocating) {
                            "현재 위치를 확인하고 있습니다."
                        } else {
                            "현재 위치를 확인하면 지도가 표시됩니다."
                        },
                        style = YadanTypography.bodyMedium,
                        color = YadanTextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            hasMapError -> {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "지도를 불러오지 못했습니다.",
                        style = YadanTypography.bodyMedium,
                        color = YadanTextMuted,
                    )

                    TextButton(onClick = { hasMapError = false }) {
                        Text("지도 다시 불러오기", color = YadanPrimary)
                    }
                }
            }

            else -> {
                TravelVerificationMap(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    onError = { hasMapError = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(name = "D02 방문 인증", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TravelVerificationScreenPreview(
    @PreviewParameter(TravelVerificationPreviewProvider::class)
    uiState: TravelVerificationUiState,
) {
    YadanbeopseokTheme {
        TravelVerificationScreen(
            uiState = uiState,
            onBackClick = {},
            onVerifyClick = {},
            onRetryClick = {},
            onRequestLocationPermission = {},
            onOpenLocationSettings = {},
        )
    }
}

/** 초기 조회, 위치 조회, 인증 준비, 요청 중, 권한 및 오류 상태의 Preview입니다. */
class TravelVerificationPreviewProvider : PreviewParameterProvider<TravelVerificationUiState> {
    override val values: Sequence<TravelVerificationUiState>
        get() {
            val ready = verificationPreviewState()

            return sequenceOf(
                TravelVerificationUiState(),
                ready.copy(
                    phase = TravelVerificationPhase.LOCATING,
                    locationResult = null,
                ),
                ready,
                ready.copy(phase = TravelVerificationPhase.SUBMITTING),
                ready.copy(
                    phase = TravelVerificationPhase.ERROR,
                    locationResult = CurrentLocationResult.PermissionRequired,
                    retryAction = TravelVerificationRetryAction.LOAD_LOCATION,
                    errorMessage = "방문 인증을 위해 위치 권한이 필요합니다.",
                ),
                ready.copy(
                    phase = TravelVerificationPhase.ERROR,
                    locationResult = CurrentLocationResult.LocationDisabled,
                    retryAction = TravelVerificationRetryAction.LOAD_LOCATION,
                    errorMessage = "기기의 위치 서비스를 켜주세요.",
                ),
                ready.copy(
                    phase = TravelVerificationPhase.ERROR,
                    retryAction = TravelVerificationRetryAction.REFRESH_TRAVEL,
                    errorMessage = "인증 결과를 확인하지 못했습니다. 다시 시도해주세요.",
                ),
                TravelVerificationUiState(
                    phase = TravelVerificationPhase.ERROR,
                    retryAction = TravelVerificationRetryAction.LOAD_CONTENT,
                    errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
                ),
            )
        }
}

private fun verificationPreviewState(): TravelVerificationUiState {
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
        certificationTargetCount = 1,
        certifiedSpotsCount = 0,
        days = listOf(
            TravelDay(
                day = 1,
                places = listOf(
                    TravelPlace(
                        spot = spot,
                        order = 0,
                        isCertificationTarget = true,
                    ),
                ),
            ),
            TravelDay(day = 2, places = emptyList()),
        ),
        status = TravelStatus.ACTIVE,
    )

    val location = Location("preview").apply {
        latitude = 35.0975
        longitude = 129.0106
        accuracy = 10f
    }

    return TravelVerificationUiState(
        travel = travel,
        targetSpot = TravelSpotDetail(
            spot = spot,
            latitude = 35.0975,
            longitude = 129.0106,
        ),
        travelDay = 1,
        locationResult = CurrentLocationResult.Success(location),
        phase = TravelVerificationPhase.READY,
    )
}
