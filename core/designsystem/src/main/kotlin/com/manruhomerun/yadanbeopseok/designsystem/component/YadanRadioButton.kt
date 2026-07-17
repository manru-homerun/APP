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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/**
 * 야단법석 화면에서 단일 선택 상태를 표시하는 원형 라디오 버튼입니다.
 *
 * B·01 경기 선택 화면의 HTML `.gp-radio` 디자인에 대응합니다.
 * Material 기본 RadioButton은 내부 점을 사용하지만, 야단법석 디자인은
 * 선택 상태에서 흰색 체크 아이콘을 사용하므로 외형을 직접 구성합니다.
 *
 * Compose의 Selectable 접근성과 최소 터치 영역은 그대로 사용합니다.
 *
 * 카드 전체가 선택을 처리하는 경우에는 [onClick]에 `null`을 전달하고,
 * 부모 카드에서 단일 선택 상태와 접근성을 처리합니다.
 *
 * @param selected 현재 항목의 선택 여부입니다.
 * @param onClick 라디오 버튼을 눌렀을 때 실행할 작업입니다.
 * `null`이면 선택 상태만 표시하고 클릭을 직접 처리하지 않습니다.
 * @param modifier 라디오 버튼의 배치에 사용할 Modifier입니다.
 * @param enabled 라디오 버튼 활성화 여부입니다.
 */
@Composable
fun YadanRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    /*
     * 라디오 버튼이 클릭을 직접 처리할 때만 최소 터치 영역과
     * RadioButton 역할을 적용합니다.
     */
    val interactionModifier =
        if (onClick != null) {
            Modifier
                .minimumInteractiveComponentSize()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
        } else {
            Modifier
        }

    Box(
        modifier = modifier.then(interactionModifier),
        contentAlignment = Alignment.Center,
    ) {
        YadanRadioButtonVisual(
            selected = selected,
            enabled = enabled,
        )
    }
}

/**
 * HTML `.gp-radio`의 실제 원형 외형을 그립니다.
 */
@Composable
private fun YadanRadioButtonVisual(
    selected: Boolean,
    enabled: Boolean,
) {
    val containerColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanSurface
        }

    val borderColor =
        if (selected) {
            YadanPrimary
        } else {
            YadanOutline
        }

    Box(
        modifier =
            Modifier
                .size(RADIO_BUTTON_SIZE)
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
                    width = RADIO_BUTTON_BORDER_WIDTH,
                    color = borderColor,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            /*
             * HTML의 선택 상태는 Material 기본 라디오 점이 아니라
             * 16px 크기의 흰색 체크 아이콘을 사용합니다.
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

private val RADIO_BUTTON_SIZE = 24.dp
private val RADIO_BUTTON_BORDER_WIDTH = 2.dp
private val CHECK_ICON_SIZE = 16.dp

private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.42f

@Preview(
    name = "Yadan radio button - HTML states",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanRadioButtonPreview() {
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
                text = "경기 선택 상태",
                style = MaterialTheme.typography.labelMedium,
                color = YadanTextMuted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButtonStatePreview(
                    label = "선택 안 됨",
                    selected = false,
                )

                RadioButtonStatePreview(
                    label = "선택됨",
                    selected = true,
                )
            }
        }
    }
}

/**
 * Preview에서 라디오 버튼의 한 가지 상태를 표시합니다.
 */
@Composable
private fun RadioButtonStatePreview(
    label: String,
    selected: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        YadanRadioButton(
            selected = selected,
            onClick = {},
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = YadanTextMuted,
        )
    }
}
