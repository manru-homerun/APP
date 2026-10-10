package com.manruhomerun.yadanbeopseok.travel.spot.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadge
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCategoryBadgeStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconToggleButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDibs
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
import com.manruhomerun.yadanbeopseok.ui.component.YadanImageLoadState

/** 관광지 상세 정보와 고정된 뒤로가기·찜 버튼을 표시합니다. */
@Composable
fun TravelSpotDetailScreen(
    uiState: TravelSpotDetailUiState,
    onBackClick: () -> Unit,
    onDibsClick: () -> Unit,
    onRetryClick: () -> Unit,
    onHomepageClick: (String) -> Unit,
    onTelephoneClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "관광지 상세",
            onNavigationClick = onBackClick,
            trailingContent = {
                uiState.detail?.let { detail ->
                    TravelSpotDibsButton(
                        spot = detail.spot,
                        isUpdatingDibs = uiState.isUpdatingDibs,
                        onDibsClick = onDibsClick,
                    )
                }
            },
        )

        when {
            uiState.isLoading -> {
                TravelSpotDetailLoadingContent(modifier = Modifier.fillMaxWidth().weight(1f))
            }

            uiState.detail != null -> {
                TravelSpotDetailContent(
                    detail = uiState.detail,
                    onHomepageClick = onHomepageClick,
                    onTelephoneClick = onTelephoneClick,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }

            else -> {
                TravelSpotDetailErrorContent(
                    message = uiState.errorMessage ?: "관광지 정보를 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

/** 사진, 기본 정보, 연락처와 소개를 서로 구분해 표시합니다. */
@Composable
private fun TravelSpotDetailContent(
    detail: TravelSpotDetail,
    onHomepageClick: (String) -> Unit,
    onTelephoneClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spot = detail.spot
    val locationText = spot.address?.trim()?.takeIf { it.isNotEmpty() } ?: spot.region?.displayName

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item(key = "gallery") {
            TravelSpotImageGallery(detail = detail)
        }

        item(key = "information") {
            Column(
                modifier = Modifier.padding(start = 18.dp, top = 14.dp, end = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YadanCategoryBadge(
                    text = spot.category.displayName,
                    style = YadanCategoryBadgeStyle.SELECTED,
                )

                Text(
                    text = spot.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = YadanTypography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = YadanTextPrimary,
                )

                locationText?.let { location -> TravelSpotAddress(address = location) }
            }
        }

        if (detail.telephone != null || detail.homepage != null) {
            item(key = "contacts") {
                Column(modifier = Modifier.padding(start = 18.dp, top = 24.dp, end = 18.dp)) {
                    detail.telephone?.let { telephone ->
                        TravelSpotContactRow(
                            text = telephone,
                            icon = Icons.Outlined.Phone,
                            onClick = onTelephoneClick,
                        )
                    }

                    detail.homepage?.let { homepage ->
                        TravelSpotContactRow(
                            text = "홈페이지 열기",
                            icon = Icons.AutoMirrored.Outlined.OpenInNew,
                            onClick = { onHomepageClick(homepage) },
                        )
                    }
                }
            }
        }

        detail.overview?.let { overview ->
            item(key = "overview") {
                TravelSpotOverview(
                    spotId = spot.id,
                    overview = overview,
                    modifier = Modifier.padding(start = 18.dp, top = 24.dp, end = 18.dp),
                )
            }
        }
    }
}

/** 관광지 이미지 목록을 가로로 넘겨볼 수 있는 갤러리입니다. */
@Composable
private fun TravelSpotImageGallery(detail: TravelSpotDetail) {
    key(detail.spot.id) {
        val imageUrls: List<String?> = detail.imageUrls.takeIf { it.isNotEmpty() } ?: listOf(detail.spot.imageUrl)
        val pagerState = rememberPagerState(pageCount = imageUrls::size)

        Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                key(page, imageUrls[page]) {
                    TravelSpotImagePage(
                        imageUrl = imageUrls[page],
                        contentDescription = if (imageUrls.size == 1) {
                            "${detail.spot.name} 이미지"
                        } else {
                            "${detail.spot.name} 이미지 ${page + 1}"
                        },
                    )
                }
            }

            if (imageUrls.size > 1) {
                TravelSpotGalleryIndicator(
                    pageCount = imageUrls.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp),
                )

                TravelSpotGalleryCount(
                    pageCount = imageUrls.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 10.dp),
                )
            }
        }
    }
}

/** 현재 사진의 표시 상태를 관리하고 실패한 이미지 요청만 다시 시작합니다. */
@Composable
private fun TravelSpotImagePage(imageUrl: String?, contentDescription: String) {
    var retryGeneration by rememberSaveable { mutableIntStateOf(0) }
    val requestGeneration = retryGeneration
    var loadState by remember(requestGeneration) {
        mutableStateOf(if (imageUrl.isNullOrBlank()) YadanImageLoadState.FALLBACK else YadanImageLoadState.LOADING)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        key(requestGeneration) {
            YadanAsyncImage(
                imageUrl = imageUrl,
                contentDescription = contentDescription.takeIf { loadState == YadanImageLoadState.SUCCESS },
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape,
                placeholderIcon = null,
                onLoadStateChange = { state ->
                    // 재시도 직전 요청의 콜백은 새 사진 상태를 변경하지 않습니다.
                    if (retryGeneration == requestGeneration) loadState = state
                },
            )
        }

        TravelSpotImageStatus(
            loadState = loadState,
            onRetryClick = {
                if (loadState == YadanImageLoadState.ERROR && !imageUrl.isNullOrBlank()) {
                    loadState = YadanImageLoadState.LOADING
                    retryGeneration++
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** 사진 영역 안에서만 로딩·실패·표시할 이미지 없음 상태를 안내합니다. */
@Composable
private fun TravelSpotImageStatus(loadState: YadanImageLoadState, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    val message = when (loadState) {
        YadanImageLoadState.LOADING -> "이미지를 불러오는 중입니다"
        YadanImageLoadState.ERROR -> "이미지를 불러오지 못했습니다"
        YadanImageLoadState.FALLBACK -> "표시할 이미지가 없습니다"
        YadanImageLoadState.SUCCESS -> return
    }

    Column(
        modifier = modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = YadanTypography.bodyMedium,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        if (loadState == YadanImageLoadState.ERROR) {
            TextButton(
                onClick = onRetryClick,
                modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = YadanPrimary),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(text = "다시 시도", style = YadanTypography.labelLarge)
                }
            }
        }
    }
}

/** 이미지 위에서 갤러리의 현재 위치를 표시하는 화면 전용 페이지 표시기입니다. */
@Composable
private fun TravelSpotGalleryIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
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
                        color = if (page == selectedPage) YadanOnPrimary else YadanOnPrimary.copy(alpha = 0.55f),
                        shape = YadanPillShape,
                    ),
            )
        }
    }
}

/** 갤러리 오른쪽 아래에 현재 이미지 번호를 표시합니다. */
@Composable
private fun TravelSpotGalleryCount(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clearAndSetSemantics {},
        color = Color.Black.copy(alpha = 0.38f),
        contentColor = YadanOnPrimary,
        shape = YadanPillShape,
    ) {
        Text(
            text = "${currentPage + 1} / $pageCount",
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = YadanTypography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        )
    }
}

/** 고정 헤더에 찜 상태를 표시하고 요청 중에는 중복 클릭을 막습니다. */
@Composable
private fun TravelSpotDibsButton(spot: TravelSpot, isUpdatingDibs: Boolean, onDibsClick: () -> Unit) {
    YadanIconToggleButton(
        checked = spot.dibs,
        onCheckedChange = { onDibsClick() },
        size = YadanIconButtonSize.DEFAULT,
        enabled = !isUpdatingDibs,
        uncheckedContentColor = YadanTextMuted,
        checkedContentColor = YadanDibs,
    ) { checked ->
        Icon(
            imageVector = if (checked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = when {
                isUpdatingDibs -> "찜 상태 변경 중"
                checked -> "찜 취소"
                else -> "찜하기"
            },
        )
    }
}

/** 관광지 주소를 이름 아래에 한 번만 표시합니다. */
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
            modifier = Modifier.weight(1f),
            style = YadanTypography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = YadanTextMuted,
        )
    }
}

/** 실행 가능한 연락처는 버튼으로, 모호한 전화 정보는 일반 텍스트로 표시합니다. */
@Composable
private fun TravelSpotContactRow(text: String, icon: ImageVector, onClick: (() -> Unit)?) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (onClick != null) YadanPrimary else YadanTextMuted,
            )

            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = YadanTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (onClick != null) YadanPrimary else YadanTextSecondary,
            )
        }
    }

    if (onClick != null) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RectangleShape,
            contentPadding = PaddingValues(vertical = 12.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = YadanPrimary),
        ) {
            content()
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 12.dp)) {
            content()
        }
    }
}

/** 실제 5줄 초과 여부를 측정해 소개를 펼치거나 접습니다. */
@Composable
private fun TravelSpotOverview(spotId: String, overview: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(spotId, overview) { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    val collapsedMaxLines = 5

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 펼친 본문이 아닌 접힌 본문을 측정해야 접기 버튼이 사라지지 않습니다.
        val collapsedLayout = textMeasurer.measure(
            text = overview,
            style = YadanTypography.bodyMedium,
            maxLines = collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = constraints.maxWidth),
        )
        val canExpand = collapsedLayout.hasVisualOverflow

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TravelSpotSectionTitle(title = "소개")

            Text(
                text = overview,
                modifier = Modifier.fillMaxWidth(),
                style = YadanTypography.bodyMedium,
                color = YadanTextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
                overflow = TextOverflow.Ellipsis,
            )

            if (canExpand) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = YadanPrimary),
                ) {
                    Text(
                        text = if (expanded) "접기" else "더보기",
                        style = YadanTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

/** 관광지 상세 화면의 구역 제목입니다. */
@Composable
private fun TravelSpotSectionTitle(title: String) {
    Text(
        text = title,
        style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = YadanTextPrimary,
    )
}

/** 고정 헤더 아래에서 관광지 상세 조회 진행 상태를 표시합니다. */
@Composable
private fun TravelSpotDetailLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(30.dp), color = YadanPrimary, strokeWidth = 3.dp)
    }
}

/** 상세 조회 실패 시 오류와 재시도 버튼을 표시합니다. */
@Composable
private fun TravelSpotDetailErrorContent(message: String, onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "관광지 정보를 확인할 수 없습니다",
            style = YadanTypography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
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
            modifier = Modifier.padding(top = 20.dp).widthIn(min = 148.dp),
        )
    }
}

@Preview(name = "관광지 상세", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailScreenPreview() {
    TravelSpotDetailPreviewContent(detail = previewTravelSpotDetail())
}

@Preview(name = "관광지 상세 - 긴 이름과 소개", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailLongContentPreview() {
    val detail = previewTravelSpotDetail()
    TravelSpotDetailPreviewContent(
        detail = detail.copy(
            spot = detail.spot.copy(name = "올림픽체조경기장(KSPO DOME) 문화 공연 관광지"),
            overview = "현재는 다양한 공연과 행사가 열리는 공간으로, 관람객을 위한 시설과 주변 산책로를 함께 둘러볼 수 있습니다. ".repeat(8),
        ),
    )
}

@Preview(name = "관광지 상세 - 좁은 화면과 큰 글꼴", showBackground = true, widthDp = 320, heightDp = 740, fontScale = 1.5f)
@Composable
private fun TravelSpotDetailLargeFontPreview() {
    val detail = previewTravelSpotDetail()
    TravelSpotDetailPreviewContent(
        detail = detail.copy(
            spot = detail.spot.copy(
                name = "올림픽체조경기장(KSPO DOME)",
                address = "서울특별시 송파구 올림픽로 424 올림픽공원 안쪽 공연장 입구",
            ),
            overview = "주변 산책로와 공연 시설을 함께 둘러볼 수 있는 관광지입니다. ".repeat(8),
        ),
    )
}

@Preview(name = "관광지 상세 - 연락처 없음", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailWithoutContactsPreview() {
    TravelSpotDetailPreviewContent(detail = previewTravelSpotDetail().copy(telephone = null, homepage = null))
}

@Preview(name = "관광지 상세 - 전화 안내 문구", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailTelephoneInformationPreview() {
    TravelSpotDetailPreviewContent(
        detail = previewTravelSpotDetail().copy(telephone = "공연 관련 문의는 홈페이지를 확인해주세요."),
        onTelephoneClick = null,
    )
}

@Preview(name = "관광지 상세 - 이미지 없음", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailWithoutImagesPreview() {
    TravelSpotDetailPreviewContent(detail = previewTravelSpotDetail().copy(imageUrls = emptyList()))
}

@Preview(name = "관광지 상세 - 이미지 한 장", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailSingleImagePreview() {
    val detail = previewTravelSpotDetail()
    TravelSpotDetailPreviewContent(detail = detail.copy(imageUrls = detail.imageUrls.take(1)))
}

@Preview(name = "관광지 사진 - 로딩", showBackground = true, widthDp = 390, heightDp = 230)
@Composable
private fun TravelSpotImageLoadingPreview() {
    TravelSpotImageStatusPreviewContent(loadState = YadanImageLoadState.LOADING)
}

@Preview(name = "관광지 사진 - 실패", showBackground = true, widthDp = 390, heightDp = 230)
@Preview(name = "관광지 사진 - 실패와 큰 글꼴", showBackground = true, widthDp = 320, heightDp = 230, fontScale = 1.5f)
@Composable
private fun TravelSpotImageErrorPreview() {
    TravelSpotImageStatusPreviewContent(loadState = YadanImageLoadState.ERROR)
}

@Preview(name = "관광지 사진 - 이미지 없음", showBackground = true, widthDp = 390, heightDp = 230)
@Composable
private fun TravelSpotImageFallbackPreview() {
    TravelSpotImageStatusPreviewContent(loadState = YadanImageLoadState.FALLBACK)
}

/** 네트워크 요청 없이 사진 영역의 상태 안내를 확인하는 Preview입니다. */
@Composable
private fun TravelSpotImageStatusPreviewContent(loadState: YadanImageLoadState) {
    YadanbeopseokTheme {
        Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
            YadanAsyncImage(
                imageUrl = null,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape,
                placeholderIcon = null,
            )
            TravelSpotImageStatus(
                loadState = loadState,
                onRetryClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(name = "관광지 상세 - 찜 변경 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailUpdatingDibsPreview() {
    TravelSpotDetailPreviewContent(detail = previewTravelSpotDetail(), isUpdatingDibs = true)
}

@Preview(name = "관광지 상세 - 로딩", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailLoadingPreview() {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
            onHomepageClick = {},
            onTelephoneClick = null,
        )
    }
}

@Preview(name = "관광지 상세 - 오류", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelSpotDetailErrorPreview() {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(isLoading = false, errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요."),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
            onHomepageClick = {},
            onTelephoneClick = null,
        )
    }
}

/** Preview에서만 사용하는 화면 상태와 콜백을 제공합니다. */
@Composable
private fun TravelSpotDetailPreviewContent(
    detail: TravelSpotDetail,
    isUpdatingDibs: Boolean = false,
    onTelephoneClick: (() -> Unit)? = {},
) {
    YadanbeopseokTheme {
        TravelSpotDetailScreen(
            uiState = TravelSpotDetailUiState(detail = detail, isLoading = false, isUpdatingDibs = isUpdatingDibs),
            onBackClick = {},
            onDibsClick = {},
            onRetryClick = {},
            onHomepageClick = {},
            onTelephoneClick = onTelephoneClick,
        )
    }
}

private fun previewTravelSpotDetail(): TravelSpotDetail = TravelSpotDetail(
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
    imageUrls = listOf("preview-image-1", "preview-image-2", "preview-image-3", "preview-image-4", "preview-image-5"),
)
