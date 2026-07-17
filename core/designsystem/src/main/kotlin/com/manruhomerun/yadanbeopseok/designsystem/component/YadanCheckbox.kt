package com.manruhomerun.yadanbeopseok.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 체크박스의 시각적 크기입니다.
 */
enum class YadanCheckboxSize {
    /**
     * 동행 조건과 동행자 선택에 사용하는 기본 크기입니다.
     *
     * HTML의 `.chk`에 해당하는 27dp 크기입니다.
     */
    DEFAULT,

    /**
     * 개별 약관 항목에 사용하는 작은 크기입니다.
     *
     * HTML의 `.chk.sm`에 해당하는 23dp 크기입니다.
     */
    SMALL,
}

/**
 * 야단법석 화면에서 공통으로 사용하는 원형 체크박스입니다.
 *
 * HTML의 원형 체크 표시를 그대로 재현하면서 Compose의
 * Toggleable 접근성 처리와 최소 터치 영역을 사용합니다.
 *
 * 카드나 행 전체에서 클릭을 처리하는 경우에는 [onCheckedChange]에
 * `null`을 전달하고 부모 컴포넌트에서 선택 상태와 접근성을 처리합니다.
 * 이렇게 하면 카드와 체크박스의 클릭 영역이 중복되지 않습니다.
 *
 * @param checked 현재 선택 여부입니다.
 * @param onCheckedChange 체크 상태가 변경될 때 호출됩니다.
 * `null`이면 체크박스는 표시만 하고 클릭을 직접 처리하지 않습니다.
 * @param modifier 체크박스의 배치에 사용할 Modifier입니다.
 * @param size 체크박스의 시각적 크기입니다.
 * @param enabled 체크박스 활성화 여부입니다.
 */
@Composable
fun YadanCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: YadanCheckboxSize = YadanCheckboxSize.DEFAULT,
    enabled: Boolean = true,
) {
    /*
     * 직접 클릭할 수 있는 경우에만 최소 48dp 터치 영역과
     * Checkbox 역할을 적용합니다.
     *
     * 부모 카드가 클릭을 처리한다면 시각적 크기만 차지합니다.
     */
    val interactionModifier =
        if (onCheckedChange != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                )
        } else {
            Modifier
        }

    Box(
        modifier = modifier.then(interactionModifier),
        contentAlignment = Alignment.Center,
    ) {
        YadanCheckboxVisual(
            checked = checked,
            size = checkboxDiameter(size),
            enabled = enabled,
        )
    }
}

/**
 * HTML 체크박스의 실제 원형 외형을 그립니다.
 */
@Composable
private fun YadanCheckboxVisual(
    checked: Boolean,
    size: Dp,
    enabled: Boolean,
) {
    val containerColor =
        if (checked) {
            YadanPrimary
        } else {
            Color.Transparent
        }

    val borderColor =
        if (checked) {
            YadanPrimary
        } else {
            YadanOutline
        }

    Box(
        modifier =
            Modifier
                .size(size)
                .alpha(
                    if (enabled) {
                        ENABLED_ALPHA
                    } else {
                        DISABLED_ALPHA
                    },
                )
                .background(
                    color = containerColor,
                    shape = CircleShape,
                )
                .border(
                    width = CHECKBOX_BORDER_WIDTH,
                    color = borderColor,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            /*
             * HTML에서는 기본형과 소형 모두
             * 체크 아이콘 크기로 17px을 사용합니다.
             */
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(CHECK_ICON_SIZE),
                tint = YadanOnPrimary,
            )
        }
    }
}

/**
 * 체크박스 유형에 대응하는 HTML 크기를 반환합니다.
 */
private fun checkboxDiameter(
    size: YadanCheckboxSize,
): Dp =
    when (size) {
        YadanCheckboxSize.DEFAULT -> DEFAULT_CHECKBOX_SIZE
        YadanCheckboxSize.SMALL -> SMALL_CHECKBOX_SIZE
    }

private val DEFAULT_CHECKBOX_SIZE = 27.dp
private val SMALL_CHECKBOX_SIZE = 23.dp
private val CHECK_ICON_SIZE = 17.dp
private val CHECKBOX_BORDER_WIDTH = 2.dp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan checkbox - HTML states",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanCheckboxPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CheckboxSizePreview(
                title = "기본 27dp",
                size = YadanCheckboxSize.DEFAULT,
            )

            CheckboxSizePreview(
                title = "약관용 23dp",
                size = YadanCheckboxSize.SMALL,
            )
        }
    }
}

/**
 * 각 크기의 선택 및 미선택 상태를 함께 보여줍니다.
 */
@Composable
private fun CheckboxSizePreview(
    title: String,
    size: YadanCheckboxSize,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = YadanTextMuted,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckboxStatePreview(
                label = "선택 안 됨",
                checked = false,
                size = size,
            )

            CheckboxStatePreview(
                label = "선택됨",
                checked = true,
                size = size,
            )
        }
    }
}

/**
 * Preview에서 체크박스의 한 가지 상태를 표시합니다.
 */
@Composable
private fun CheckboxStatePreview(
    label: String,
    checked: Boolean,
    size: YadanCheckboxSize,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        YadanCheckbox(
            checked = checked,
            onCheckedChange = {},
            size = size,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = YadanTextMuted,
        )
    }
}
