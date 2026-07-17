package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryDark
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 공통 버튼의 시각적 유형입니다.
 */
enum class YadanButtonStyle {
    /** 주요 작업에 사용하는 하늘색 버튼입니다. */
    PRIMARY,

    /** 흰색 배경과 테두리가 있는 보조 버튼입니다. */
    GHOST,

    /** 연한 하늘색 배경의 선택 및 보조 작업 버튼입니다. */
    TONAL,

    /** 어두운 배경 위에서 사용하는 반투명 버튼입니다. */
    ON_DARK,
}

/**
 * 야단법석 화면에서 공통으로 사용하는 버튼입니다.
 *
 * @param text 버튼에 표시할 문구입니다.
 * @param onClick 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 외부에서 크기와 배치를 지정할 Modifier입니다.
 * @param style 버튼의 색상 및 테두리 유형입니다.
 * @param enabled 버튼 활성화 여부입니다.
 * @param isLoading 진행 상태 표시 여부입니다. 로딩 중에는 중복 클릭을 막습니다.
 * @param leadingIcon 텍스트 앞에 표시할 아이콘입니다.
 * @param trailingIcon 텍스트 뒤에 표시할 아이콘입니다.
 */
@Composable
fun YadanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YadanButtonStyle = YadanButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val (containerColor, contentColor) =
        when (style) {
            YadanButtonStyle.PRIMARY ->
                YadanPrimary to YadanOnPrimary

            YadanButtonStyle.GHOST ->
                YadanSurface to YadanTextPrimary

            YadanButtonStyle.TONAL ->
                YadanPrimaryTint to YadanPrimaryInk

            YadanButtonStyle.ON_DARK ->
                YadanOnPrimary.copy(alpha = 0.14f) to YadanOnPrimary
        }

    val borderColor =
        when (style) {
            YadanButtonStyle.GHOST ->
                YadanOutline

            YadanButtonStyle.ON_DARK ->
                YadanOnPrimary.copy(alpha = 0.26f)

            else -> null
        }

    /*
     * HTML의 .btn[disabled] { opacity: .42 }처럼
     * 버튼의 배경, 글자, 테두리 전체에 투명도를 적용합니다.
     *
     * 로딩 중에는 클릭만 막고 활성 상태의 색상은 유지합니다.
     */
    val buttonAlpha = if (enabled) 1f else DISABLED_ALPHA

    Button(
        onClick = onClick,
        modifier =
            modifier
                .alpha(buttonAlpha)
                .heightIn(min = BUTTON_MIN_HEIGHT),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,

                /*
                 * Button이 비활성화돼도 원래 색상을 사용합니다.
                 * 비활성 투명도는 Modifier.alpha에서 처리합니다.
                 */
                disabledContainerColor = containerColor,
                disabledContentColor = contentColor,
            ),
        elevation =
            if (style == YadanButtonStyle.PRIMARY) {
                ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp,

                    /*
                     * 로딩과 비활성 상태에서도 그림자를 유지합니다.
                     * 비활성 상태에서는 버튼 전체 투명도와 함께 흐려집니다.
                     */
                    disabledElevation = 4.dp,
                )
            } else {
                null
            },
        border =
            borderColor?.let { color ->
                BorderStroke(
                    width = 1.5.dp,
                    color = color,
                )
            },
        contentPadding =
            PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
    ) {
        val hasLeadingContent = isLoading || leadingIcon != null
        val hasTrailingContent = !isLoading && trailingIcon != null

        /*
         * 한쪽에만 아이콘이 있더라도 반대쪽에 동일한 크기의
         * 빈 공간을 확보하여 텍스트의 중심을 유지합니다.
         */
        val shouldReserveIconSpace =
            hasLeadingContent || hasTrailingContent

        if (shouldReserveIconSpace) {
            Box(
                modifier = Modifier.size(ICON_SIZE),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ICON_SIZE),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp,
                        )
                    }

                    leadingIcon != null -> {
                        leadingIcon()
                    }
                }
            }

            Spacer(modifier = Modifier.width(ICON_SPACING))
        }

        Text(
            text = text,
            // HTML 버튼의 font-weight: 800을 적용합니다.
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
        )

        if (shouldReserveIconSpace) {
            Spacer(modifier = Modifier.width(ICON_SPACING))

            Box(
                modifier = Modifier.size(ICON_SIZE),
                contentAlignment = Alignment.Center,
            ) {
                if (hasTrailingContent) {
                    trailingIcon()
                }
            }
        }
    }
}

private val BUTTON_MIN_HEIGHT = 56.dp
private val BUTTON_CORNER_RADIUS = 16.dp
private val ICON_SIZE = 20.dp
private val ICON_SPACING = 8.dp
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan buttons",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanButtonPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YadanButton(
                text = "원정 일정 만들기",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
            )

            YadanButton(
                text = "다시 선택하기",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                style = YadanButtonStyle.GHOST,
            )

            YadanButton(
                text = "경기 선택 완료",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                style = YadanButtonStyle.TONAL,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                },
            )

            YadanButton(
                text = "원정 일정 만들기",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )

            YadanButton(
                text = "일정을 만드는 중",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                isLoading = true,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = YadanPrimaryDark,
                            shape = MaterialTheme.shapes.large,
                        )
                        .padding(16.dp),
            ) {
                YadanButton(
                    text = "일정 확인하기",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = YadanButtonStyle.ON_DARK,
                )
            }
        }
    }
}
