package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 입력창의 크기 유형입니다.
 */
enum class YadanTextFieldSize {
    /**
     * 프로필 수정과 일반적인 정보 입력에 사용하는 크기입니다.
     */
    DEFAULT,

    /**
     * 회원가입 닉네임 화면처럼 입력창을 강조할 때 사용합니다.
     */
    LARGE,
}

/**
 * 한 줄 텍스트를 입력하는 공통 입력창입니다.
 *
 * 가입 단계의 큰 닉네임 입력창과 프로필 수정 입력창에 대응합니다.
 * 라벨, 글자 수, 검증 문구는 화면마다 구성이 다르므로 외부에서 배치합니다.
 * 화면에 표시하는 검증 문구와 동일한 값을 [errorMessage]로 전달합니다.
 *
 * @param value 현재 입력된 값입니다.
 * @param onValueChange 입력값이 변경될 때 호출됩니다.
 * @param modifier 입력창의 배치에 사용할 Modifier입니다.
 * @param placeholder 입력값이 없을 때 표시할 안내 문구입니다.
 * @param size 입력창의 크기 유형입니다.
 * @param enabled 입력 가능 여부입니다.
 * @param readOnly 읽기 전용 여부입니다.
 * @param isError 오류 상태 여부입니다.
 * @param errorMessage 오류 상태일 때 접근성 서비스가 안내할 문구입니다.
 * @param showClearButton 입력 내용 지우기 버튼 표시 여부입니다.
 * @param clearContentDescription 지우기 버튼의 접근성 설명입니다.
 * @param keyboardOptions 키보드 종류와 IME 버튼 설정입니다.
 * @param keyboardActions IME 버튼을 눌렀을 때 실행할 동작입니다.
 */
@Composable
fun YadanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    size: YadanTextFieldSize = YadanTextFieldSize.DEFAULT,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "입력값을 확인해주세요",
    showClearButton: Boolean = true,
    clearContentDescription: String = "입력 내용 지우기",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    /*
     * HTML에서는 값이 입력된 화면이 모두 강조 상태로 표현됩니다.
     * 실제 입력 중인 경우에도 강조되도록 포커스 상태를 함께 확인합니다.
     */
    val isActive =
        enabled && (isFocused || value.isNotEmpty())

    /*
     * 일반 입력창은 HTML의 13px과 가장 가까운 공통 14dp 모서리를,
     * 큰 입력창은 카드와 동일한 18dp 모서리를 사용합니다.
     */
    val fieldShape =
        when (size) {
            YadanTextFieldSize.DEFAULT -> YadanShapes.medium
            YadanTextFieldSize.LARGE -> YadanShapes.large
        }

    val horizontalPadding =
        when (size) {
            YadanTextFieldSize.DEFAULT -> DEFAULT_HORIZONTAL_PADDING
            YadanTextFieldSize.LARGE -> LARGE_HORIZONTAL_PADDING
        }

    val verticalPadding =
        when (size) {
            YadanTextFieldSize.DEFAULT -> DEFAULT_VERTICAL_PADDING
            YadanTextFieldSize.LARGE -> LARGE_VERTICAL_PADDING
        }

    val contentSpacing =
        when (size) {
            YadanTextFieldSize.DEFAULT -> DEFAULT_CONTENT_SPACING
            YadanTextFieldSize.LARGE -> LARGE_CONTENT_SPACING
        }

    /*
     * 크기와 줄 높이는 새로 선언하지 않고
     * 기존 야단법석 타이포그래피를 그대로 사용합니다.
     */
    val inputTextStyle =
        when (size) {
            YadanTextFieldSize.DEFAULT ->
                YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = YadanTextPrimary,
                )

            YadanTextFieldSize.LARGE ->
                YadanTypography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = YadanTextPrimary,
                )
        }

    val placeholderTextStyle =
        inputTextStyle.copy(
            fontWeight = FontWeight.SemiBold,
            color = YadanTextMuted,
        )

    val borderColor =
        when {
            isError -> YadanError
            isActive -> YadanPrimary
            else -> YadanOutline
        }

    val borderWidth =
        if (isError || isActive) {
            ACTIVE_BORDER_WIDTH
        } else {
            DEFAULT_BORDER_WIDTH
        }

    val cursorColor =
        if (isError) {
            YadanError
        } else {
            YadanPrimary
        }

    /*
     * 가입용 큰 입력창은 HTML에서 card의 작은 그림자를 사용합니다.
     * 일반 프로필 입력창에는 그림자를 적용하지 않습니다.
     */
    val shadowModifier =
        if (size == YadanTextFieldSize.LARGE) {
            Modifier.shadow(
                elevation = LARGE_FIELD_ELEVATION,
                shape = fieldShape,
                clip = false,
            )
        } else {
            Modifier
        }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (isError) {
                        Modifier.semantics {
                            error(errorMessage)
                        }
                    } else {
                        Modifier
                    },
                )
                .alpha(
                    if (enabled) {
                        ENABLED_ALPHA
                    } else {
                        DISABLED_ALPHA
                    },
                )
                .then(shadowModifier)
                .background(
                    color = YadanSurface,
                    shape = fieldShape,
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = fieldShape,
                )
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding,
                ),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        textStyle = inputTextStyle,
        cursorBrush = SolidColor(cursorColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(contentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = placeholderTextStyle,
                            maxLines = 1,
                        )
                    }

                    innerTextField()
                }

                if (
                    showClearButton &&
                    value.isNotEmpty() &&
                    !readOnly
                ) {
                    /*
                     * 별도 지우기 버튼을 만들지 않고 기존의 작은
                     * YadanIconButton을 재사용합니다.
                     */
                    YadanIconButton(
                        onClick = {
                            onValueChange("")
                        },
                        style = YadanIconButtonStyle.MUTED,
                        size = YadanIconButtonSize.SMALL,
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = clearContentDescription,
                        )
                    }
                }
            }
        },
    )
}

/*
 * 아래 값들은 입력창의 크기 유형에서만 사용하는 배치값입니다.
 * 다른 컴포넌트와 우연히 값이 같더라도 의미가 다르므로 내부에 둡니다.
 */
private val DEFAULT_HORIZONTAL_PADDING = 15.dp
private val DEFAULT_VERTICAL_PADDING = 10.dp
private val DEFAULT_CONTENT_SPACING = 2.dp

private val LARGE_HORIZONTAL_PADDING = 16.dp
private val LARGE_VERTICAL_PADDING = 16.dp
private val LARGE_CONTENT_SPACING = 10.dp

private val DEFAULT_BORDER_WIDTH = 1.5.dp
private val ACTIVE_BORDER_WIDTH = 2.dp

private val LARGE_FIELD_ELEVATION = 2.dp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan text fields",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanTextFieldPreview() {
    var onboardingNickname by remember {
        mutableStateOf("야구좋아")
    }
    var profileNickname by remember {
        mutableStateOf("준호")
    }
    var emptyValue by remember {
        mutableStateOf("")
    }

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "가입 닉네임",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanTextField(
                value = onboardingNickname,
                onValueChange = {
                    onboardingNickname = it
                },
                placeholder = "닉네임을 입력해주세요",
                size = YadanTextFieldSize.LARGE,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
            )

            Text(
                text = "프로필 닉네임",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanTextField(
                value = profileNickname,
                onValueChange = {
                    profileNickname = it
                },
                placeholder = "닉네임을 입력해주세요",
                isError = true,
                errorMessage = "이미 사용 중인 닉네임입니다",
            )

            Text(
                text = "입력 전",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanTextField(
                value = emptyValue,
                onValueChange = {
                    emptyValue = it
                },
                placeholder = "내용을 입력해주세요",
            )
        }
    }
}
