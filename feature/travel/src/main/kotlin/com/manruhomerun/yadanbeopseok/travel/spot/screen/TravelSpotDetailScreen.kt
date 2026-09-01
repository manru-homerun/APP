package com.manruhomerun.yadanbeopseok.travel.spot.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadge
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadgeStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconToggleButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBarStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDibs
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotDetailUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanAsyncImage

/**
 * 선택한 관광지의 이미지, 기본 정보, 소개와 찜 상태를 표시합니다.
 */
@Composable
fun TravelSpotDetailScreen(
    uiState: TravelSpotDetailUiState,
    onBackClick: () -> Unit,
    onDibsClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        when {
            uiState.isLoading -> {
                TravelSpotDetailLoadingContent(
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            uiState.detail != null -> {
                TravelSpotDetailContent(
                    detail = uiState.detail,
                    isUpdatingDibs = uiState.isUpdatingDibs,
                    onBackClick = onBackClick,
                    onDibsClick = onDibsClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                TravelSpotDetailErrorContent(
                    message = uiState.errorMessage
                        ?: "관광지 정보를 불러오지 못했습니다.",
                    onBackClick = onBackClick,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 조회한 관광지 상세 정보를 표시합니다.
 */
@Composable
private fun TravelSpotDetailContent(
    detail: TravelSpotDetail,
    isUpdatingDibs: Boolean,
    onBackClick: () -> Unit,
    onDibsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spot = detail.spot
    val locationText = spot.address
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: spot.region?.displayName

    val informationRows = buildList {
        detail.telephone?.let { telephone ->
            add(TravelSpotInformation(label = "전화", value = telephone))
        }

        detail.homepage?.let { homepage ->
            add(TravelSpotInformation(label = "홈페이지", value = homepage))
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            TravelSpotImageGallery(
                detail = detail,
                onBackClick = onBackClick,
            )
        }

        item {
            Column(
                modifier = Modifier.padding(
                    start = 18.dp,
                    top = 14.dp,
                    end = 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                YadanCategoryBadge(
                    text = spot.category.displayName,
                    style = YadanCategoryBadgeStyle.SELECTED,
                )

                TravelSpotTitle(
                    spot = spot,
                    isUpdatingDibs = isUpdatingDibs,
                    onDibsClick = onDibsClick,
                )

                locationText?.let { location ->
                    TravelSpotAddress(address = location)
                }

                if (informationRows.isNotEmpty()) {
                    TravelSpotInformationTable(rows = informationRows)
                }

                detail.overview?.let { overview ->
                    TravelSpotOverview(overview = overview)
                }

                locationText?.let { location ->
                    TravelSpotLocation(address = location)
                }
            }
        }
    }
}

/**
 * 관광지 이미지 목록을 가로로 넘겨볼 수 있는 갤러리입니다.
 */
@Composable
private fun TravelSpotImageGallery(
    detail: TravelSpotDetail,
    onBackClick: () -> Unit,
) {
    val imageUrls: List<String?> = detail.imageUrls
        .takeIf { it.isNotEmpty() }
        ?: listOf(detail.spot.imageUrl)

    val pagerState = rememberPagerState(pageCount = imageUrls::size)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            YadanAsyncImage(
                imageUrl = imageUrls[page],
                contentDescription = if (imageUrls.size == 1) {
                    "${detail.spot.name} 이미지"
                } else {
                    "${detail.spot.name} 이미지 ${page + 1}"
                },
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.34f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        YadanTopAppBar(
            title = "",
            onNavigationClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
            style = YadanTopAppBarStyle.ON_DARK,
        )

        if (imageUrls.size > 1) {
            TravelSpotGalleryIndicator(
                pageCount = imageUrls.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 13.dp),
            )

            TravelSpotGalleryCount(
                pageCount = imageUrls.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp,
                        bottom = 10.dp,
                    ),
            )
        }
    }
}

/**
 * 갤러리의 현재 이미지 위치를 흰색 점으로 표시합니다.
 *
 * 이미지 위에 표시되는 A·06 전용 형태이므로 파란색 공통 페이지 표시기와
 * 분리하여 화면 내부에서 관리합니다.
 */
@Composable
private fun TravelSpotGalleryIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val selectedPage = currentPage.coerceIn(0, pageCount - 1)

    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "관광지 이미지"
            stateDescription = "${selectedPage + 1} / $pageCount"
        },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .width(if (page == selectedPage) 14.dp else 5.dp)
                    .height(5.dp)
                    .background(
                        color = if (page == selectedPage) {
                            YadanOnPrimary
                        } else {
                            YadanOnPrimary.copy(alpha = 0.55f)
                        },
                        shape = YadanPillShape,
                    ),
            )
        }
    }
}

/**
 * 갤러리 오른쪽 아래에 현재 이미지 번호를 표시합니다.
 */
@Composable
private fun TravelSpotGalleryCount(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clearAndSetSemantics {},
        color = Color.Black.copy(alpha = 0.38f),
        contentColor = YadanOnPrimary,
        shape = YadanPillShape,
    ) {
        Text(
            text = "${currentPage + 1} / $pageCount",
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 3.dp,
            ),
            style = YadanTypography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
        )
    }
}

/**
 * 관광지 이름과 현재 찜 상태를 표시합니다.
 */
@Composable
private fun TravelSpotTitle(
    spot: TravelSpot,
    isUpdatingDibs: Boolean,
    onDibsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = spot.name,
            modifier = Modifier.weight(1f),
            style = YadanTypography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanTextPrimary,
        )

        Spacer(modifier = Modifier.width(10.dp))

        YadanIconToggleButton(
            checked = spot.dibs,
            onCheckedChange = {
                onDibsClick()
            },
            size = YadanIconButtonSize.DEFAULT,
            enabled = !isUpdatingDibs,
            uncheckedContentColor = YadanTextMuted,
            checkedContentColor = YadanDibs,
        ) { checked ->
            Icon(
                imageVector = if (checked) {
                    Icons.Default.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = if (checked) {
                    "찜 취소"
                } else {
                    "찜하기"
                },
            )
        }
    }
}

/**
 * 관광지 주소를 이름 아래에 표시합니다.
 */
@Composable
private fun TravelSpotAddress(address: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = YadanPrimary,
        )

        Text(
            text = address,
            style = YadanTypography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = YadanTextMuted,
        )
    }
}

/**
 * 전화번호와 홈페이지 등 서버에서 제공한 추가 정보를 표시합니다.
 */
@Composable
private fun TravelSpotInformationTable(rows: List<TravelSpotInformation>) {
    YadanCard(modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, information ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 12.dp,
                    ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = information.label,
                    modifier = Modifier.width(64.dp),
                    style = YadanTypography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = YadanTextMuted,
                )

                Text(
                    text = information.value,
                    modifier = Modifier.weight(1f),
                    style = YadanTypography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = YadanTextPrimary,
                    textAlign = TextAlign.End,
                )
            }

            if (index < rows.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    thickness = 1.dp,
                    color = YadanDivider,
                )
            }
        }
    }
}

/**
 * 관광지 소개를 표시합니다.
 */
@Composable
private fun TravelSpotOverview(overview: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TravelSpotSectionTitle(title = "소개")

        Text(
            text = overview,
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
        )
    }
}

/**
 * 지도 SDK가 연결되기 전까지 서버에서 받은 실제 주소를 위치 정보로 표시합니다.
 */
@Composable
private fun TravelSpotLocation(address: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TravelSpotSectionTitle(title = "위치")

        YadanCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = YadanPrimary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        tint = YadanPrimary,
                    )
                }

                Text(
                    text = address,
                    modifier = Modifier.weight(1f),
                    style = YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = YadanTextPrimary,
                )
            }
        }
    }
}

/**
 * 관광지 상세 화면의 구역 제목입니다.
 */
@Composable
private fun TravelSpotSectionTitle(title: String) {
    Text(
        text = title,
        style = YadanTypography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold,
        ),
        color = YadanTextPrimary,
    )
}

/**
 * 관광지 정보를 불러오는 동안 표시합니다.
 */
@Composable
private fun TravelSpotDetailLoadingContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        YadanTopAppBar(
            title = "관광지 상세",
            onNavigationClick = onBackClick,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = YadanPrimary,
                strokeWidth = 3.dp,
            )
        }
    }
}

/**
 * 관광지 상세 조회에 실패했을 때 오류와 재시도 버튼을 표시합니다.
 */
@Composable
private fun TravelSpotDetailErrorContent(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        YadanTopAppBar(
            title = "관광지 상세",
            onNavigationClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "관광지 정보를 확인할 수 없습니다",
                style = YadanTypography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = YadanTextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = message,
                modifier = Modifier.padding(top = 8.dp),
                style = YadanTypography.bodyMedium,
                color = YadanTextSecondary,
                textAlign = TextAlign.Center,
            )

            YadanButton(
                text = "다시 시도",
                onClick = onRetryClick,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .widthIn(min = 148.dp),
            )
        }
    }
}

/**
 * 정보 표에 표시하는 한 행의 데이터입니다.
 */
private data class TravelSpotInformation(
    val label: String,
    val value: String,
)

@Preview(
    name = "Travel spot detail",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotDetailScreenPreview() {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(
                detail = previewTravelSpotDetail(),
                isLoading = false,
            ),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "Travel spot detail - Loading",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotDetailLoadingPreview() {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "Travel spot detail - Error",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelSpotDetailErrorPreview() {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
        )
    }
}

private fun previewTravelSpotDetail(): TravelSpotDetail =
    TravelSpotDetail(
        spot = TravelSpot(
            id = "132159",
            name = "감천문화마을",
            address = "부산광역시 사하구 감내2로 203",
            region = Region.BUSAN,
            category = TravelSpotCategory.CULTURE,
            dibs = true,
        ),
        telephone = "051-204-1444",
        homepage = "https://www.gamcheon.or.kr",
        longitude = 129.0106,
        latitude = 35.0975,
        overview = "산자락을 따라 이어진 알록달록한 집과 골목길을 둘러볼 수 있는 부산의 대표 문화 관광지입니다.",
        imageUrls = listOf(
            "preview-image-1",
            "preview-image-2",
            "preview-image-3",
            "preview-image-4",
            "preview-image-5",
        ),
    )
