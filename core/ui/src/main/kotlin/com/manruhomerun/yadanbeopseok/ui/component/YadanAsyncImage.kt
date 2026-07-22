package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 네트워크 이미지를 불러오는 야단법석 공통 이미지 컴포넌트입니다.
 *
 * Coil의 [AsyncImage]를 사용하며 이미지 로딩 중이거나 URL이 없거나
 * 로딩에 실패하면 HTML의 `.ph` 형태를 기반으로 한 placeholder를 표시합니다.
 *
 * 이미지 크기는 컴포넌트 내부에서 고정하지 않습니다.
 * 사용하는 카드나 화면에서 [modifier]로 크기 또는 비율을 지정합니다.
 *
 * @param imageUrl 불러올 이미지 URL입니다.
 * null 또는 빈 문자열이면 fallback 상태를 표시합니다.
 * @param contentDescription 이미지의 접근성 설명입니다.
 * 장식용 이미지라면 null을 전달합니다.
 * @param modifier 이미지의 크기와 배치를 지정할 Modifier입니다.
 * @param shape 이미지를 자르는 모서리 형태입니다.
 * @param contentScale 이미지가 영역에 맞춰지는 방식입니다.
 * @param alignment 이미지가 영역 안에서 정렬되는 방식입니다.
 * @param placeholderIcon 기본 placeholder 중앙에 표시할 아이콘입니다.
 * null이면 아이콘 없이 배경과 패턴만 표시합니다.
 * @param placeholder 로딩 중 표시할 별도 Painter입니다.
 * 전달하면 기본 placeholder 대신 해당 Painter를 사용합니다.
 * @param error 로딩 실패 시 표시할 별도 Painter입니다.
 * @param fallback 이미지 URL이 없을 때 표시할 별도 Painter입니다.
 */
@Composable
fun YadanAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = YadanShapes.medium,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    placeholderIcon: ImageVector? = Icons.Outlined.Image,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
) {
    val context = LocalContext.current
    val hasImageUrl = !imageUrl.isNullOrBlank()

    /*
     * 커스텀 Painter가 없는 경우 Coil의 전환 배경으로 사용합니다.
     * 실제 기본 placeholder의 그라데이션과 아이콘은 별도로 겹쳐 표시합니다.
     */
    val defaultPlaceholderPainter =
        remember {
            ColorPainter(YadanPrimaryTint)
        }

    val resolvedPlaceholder = placeholder ?: defaultPlaceholderPainter
    val resolvedError = error ?: resolvedPlaceholder
    val resolvedFallback = fallback ?: resolvedError

    var loadState by
    remember(imageUrl) {
        mutableStateOf(
            if (hasImageUrl) {
                YadanImageLoadState.LOADING
            } else {
                YadanImageLoadState.FALLBACK
            },
        )
    }

    val imageRequest =
        remember(context, imageUrl) {
            ImageRequest
                .Builder(context)
                .data(
                    imageUrl?.takeIf {
                        it.isNotBlank()
                    },
                )
                .crossfade(true)
                .build()
        }

    /*
     * 호출자가 상태별 Painter를 제공하면 해당 Painter를 우선합니다.
     * 별도 Painter가 없는 상태에서만 야단법석 기본 placeholder를 표시합니다.
     */
    val showDefaultPlaceholder =
        when (loadState) {
            YadanImageLoadState.LOADING ->
                placeholder == null

            YadanImageLoadState.ERROR ->
                error == null && placeholder == null

            YadanImageLoadState.FALLBACK ->
                fallback == null &&
                    error == null &&
                    placeholder == null

            YadanImageLoadState.SUCCESS ->
                false
        }

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            placeholder = resolvedPlaceholder,
            error = resolvedError,
            fallback = resolvedFallback,
            contentScale = contentScale,
            alignment = alignment,
            onLoading = {
                loadState =
                    if (hasImageUrl) {
                        YadanImageLoadState.LOADING
                    } else {
                        YadanImageLoadState.FALLBACK
                    }
            },
            onSuccess = {
                loadState = YadanImageLoadState.SUCCESS
            },
            onError = {
                loadState =
                    if (hasImageUrl) {
                        YadanImageLoadState.ERROR
                    } else {
                        YadanImageLoadState.FALLBACK
                    }
            },
        )

        if (showDefaultPlaceholder) {
            YadanImagePlaceholder(
                icon = placeholderIcon,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * HTML의 공통 이미지 placeholder인 `.ph`를 Compose로 표현합니다.
 *
 * 연한 하늘색 그라데이션 위에 대각선 패턴과 중앙 아이콘을 표시합니다.
 */
@Composable
private fun YadanImagePlaceholder(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
) {
    val backgroundBrush =
        remember {
            Brush.linearGradient(
                colors =
                    listOf(
                        YadanSurface,
                        YadanPrimaryTint,
                    ),
            )
        }

    Box(
        modifier =
            modifier.drawBehind {
                drawRect(brush = backgroundBrush)

                /*
                 * HTML의 7px 패턴과 14px 반복 간격을 적용합니다.
                 * 이 값들은 이미지 placeholder 안에서만 사용하는 값입니다.
                 */
                val stripeWidth = 7.dp.toPx()
                val stripeStep = 14.dp.toPx()
                var stripeStartX = -size.height

                while (stripeStartX < size.width) {
                    drawLine(
                        color =
                            YadanTextPrimary.copy(
                                alpha = 0.035f,
                            ),
                        start =
                            Offset(
                                x = stripeStartX,
                                y = 0f,
                            ),
                        end =
                            Offset(
                                x = stripeStartX + size.height,
                                y = size.height,
                            ),
                        strokeWidth = stripeWidth,
                    )

                    stripeStartX += stripeStep
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint =
                    YadanPrimary.copy(
                        alpha = 0.34f,
                    ),
            )
        }
    }
}

/**
 * Coil 이미지 요청의 현재 표시 상태입니다.
 */
private enum class YadanImageLoadState {
    LOADING,
    SUCCESS,
    ERROR,
    FALLBACK,
}

@Preview(
    name = "Yadan async image",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanAsyncImagePreview() {
    YadanbeopseokTheme {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "관광지 placeholder",
                    style = YadanTypography.labelSmall,
                    color = YadanTextSecondary,
                )

                YadanAsyncImage(
                    imageUrl = null,
                    contentDescription = "관광지 이미지",
                    modifier = Modifier.size(96.dp),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "프로필 placeholder",
                    style = YadanTypography.labelSmall,
                    color = YadanTextSecondary,
                )

                YadanAsyncImage(
                    imageUrl = "",
                    contentDescription = "사용자 프로필 이미지",
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    placeholderIcon = Icons.Outlined.Person,
                )
            }
        }
    }
}
