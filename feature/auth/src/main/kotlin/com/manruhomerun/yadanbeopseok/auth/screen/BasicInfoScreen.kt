package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanRadioButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTextField
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTextFieldSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Gender
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlinx.datetime.number

/**
 * 신규 회원이 닉네임, 성별 및 생년월일을 입력하는 화면입니다.
 */
@Composable
fun BasicInfoScreen(
    nickname: String,
    isNicknameValid: Boolean,
    nicknameValidationMessage: String?,
    selectedGender: Gender?,
    birthDate: LocalDate?,
    birthDateValidationMessage: String?,
    isNextEnabled: Boolean,
    onNicknameChange: (String) -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDatePickerVisible by
    rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(YadanBackground)
                .imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "기본 정보",
            onNavigationClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 20.dp,
                        bottom = 28.dp,
                    ),
        ) {
            Text(
                text = "기본 정보를 알려주세요",
                style =
                    YadanTypography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "야단법석에서 사용할 정보를 입력해주세요.",
                style = YadanTypography.bodyMedium,
                color = YadanTextMuted,
            )

            Spacer(modifier = Modifier.height(28.dp))

            BasicInfoSectionTitle(
                text = "닉네임",
            )

            Spacer(modifier = Modifier.height(10.dp))

            YadanTextField(
                value = nickname,
                onValueChange = { changedNickname ->
                    onNicknameChange(
                        changedNickname.take(NICKNAME_MAX_LENGTH),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "닉네임을 입력해주세요",
                size = YadanTextFieldSize.LARGE,
                isError =
                    nicknameValidationMessage != null &&
                        !isNicknameValid,
                errorMessage =
                    nicknameValidationMessage
                        ?: DEFAULT_NICKNAME_ERROR_MESSAGE,
                clearContentDescription = "닉네임 지우기",
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            if (isNextEnabled) {
                                onNextClick()
                            }
                        },
                    ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            NicknameValidationRow(
                nicknameLength = nickname.length,
                isNicknameValid = isNicknameValid,
                validationMessage = nicknameValidationMessage,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "2~12자 · 한글, 영문, 숫자 사용 가능",
                modifier = Modifier.padding(horizontal = 4.dp),
                style = YadanTypography.bodySmall,
                color = YadanTextMuted,
            )

            Spacer(modifier = Modifier.height(28.dp))

            BasicInfoSectionTitle(
                text = "성별",
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GenderSelectionItem(
                    gender = Gender.MALE,
                    selected = selectedGender == Gender.MALE,
                    onClick = {
                        onGenderSelect(Gender.MALE)
                    },
                    modifier = Modifier.weight(1f),
                )

                GenderSelectionItem(
                    gender = Gender.FEMALE,
                    selected = selectedGender == Gender.FEMALE,
                    onClick = {
                        onGenderSelect(Gender.FEMALE)
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            BasicInfoSectionTitle(
                text = "생년월일",
            )

            Spacer(modifier = Modifier.height(10.dp))

            BirthDateSelectionCard(
                birthDate = birthDate,
                onClick = {
                    isDatePickerVisible = true
                },
            )

            if (birthDateValidationMessage != null) {
                Spacer(modifier = Modifier.height(9.dp))

                ValidationMessage(
                    message = birthDateValidationMessage,
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 26.dp,
                    ),
        ) {
            YadanButton(
                text = "다음",
                onClick = onNextClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isNextEnabled,
                trailingIcon = {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "입력한 정보는 가입 후에도 수정할 수 있어요",
                modifier = Modifier.fillMaxWidth(),
                style =
                    YadanTypography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = YadanTextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }

    if (isDatePickerVisible) {
        BirthDatePickerDialog(
            initialDate = birthDate,
            onDismiss = {
                isDatePickerVisible = false
            },
            onDateConfirm = { selectedDate ->
                onBirthDateChange(selectedDate)
                isDatePickerVisible = false
            },
        )
    }
}

/**
 * 기본 정보 입력 항목의 제목을 표시합니다.
 */
@Composable
private fun BasicInfoSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style =
            YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
        color = YadanTextPrimary,
    )
}

/**
 * 남자 또는 여자 중 하나를 선택할 수 있는 항목입니다.
 */
@Composable
private fun GenderSelectionItem(
    gender: Gender,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        modifier =
            modifier
                .heightIn(min = 58.dp)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        style =
            if (selected) {
                YadanCardStyle.SELECTED
            } else {
                YadanCardStyle.DEFAULT
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 15.dp,
                        vertical = 14.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = gender.displayName,
                modifier = Modifier.weight(1f),
                style =
                    YadanTypography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color =
                    if (selected) {
                        YadanPrimaryInk
                    } else {
                        YadanTextPrimary
                    },
            )

            /*
             * 카드 전체에서 선택 이벤트를 처리하므로
             * 라디오 버튼에는 별도 클릭 동작을 전달하지 않습니다.
             */
            YadanRadioButton(
                selected = selected,
                onClick = null,
            )
        }
    }
}

/**
 * 선택된 생년월일 또는 선택 안내 문구를 표시합니다.
 */
@Composable
private fun BirthDateSelectionCard(
    birthDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    YadanCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 15.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = YadanPrimary,
            )

            Text(
                text =
                    birthDate?.toKoreanDateString()
                        ?: "생년월일을 선택해주세요",
                modifier = Modifier.weight(1f),
                style =
                    YadanTypography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color =
                    if (birthDate == null) {
                        YadanTextMuted
                    } else {
                        YadanTextPrimary
                    },
            )
        }
    }
}

/**
 * 닉네임 검증 결과와 현재 글자 수를 표시합니다.
 */
@Composable
private fun NicknameValidationRow(
    nicknameLength: Int,
    isNicknameValid: Boolean,
    validationMessage: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (validationMessage != null) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ValidationIcon(
                    isValid = isNicknameValid,
                )

                Text(
                    text = validationMessage,
                    style =
                        YadanTypography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color =
                        if (isNicknameValid) {
                            YadanPrimaryInk
                        } else {
                            YadanError
                        },
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = "$nicknameLength / $NICKNAME_MAX_LENGTH",
            style =
                YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = YadanTextMuted,
        )
    }
}

/**
 * 입력값 검증 성공 또는 실패 상태를 표시합니다.
 */
@Composable
private fun ValidationIcon(
    isValid: Boolean,
) {
    if (isValid) {
        Box(
            modifier =
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(YadanPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = YadanOnPrimary,
            )
        }
    } else {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = YadanError,
        )
    }
}

/**
 * 입력값 검증 오류 문구를 접근성 정보와 함께 표시합니다.
 */
@Composable
private fun ValidationMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .padding(horizontal = 4.dp)
                .semantics {
                    error(message)
                },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = YadanError,
        )

        Text(
            text = message,
            style =
                YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = YadanError,
        )
    }
}

/**
 * 사용자가 생년월일을 선택하는 Material 날짜 선택 창입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateConfirm: (LocalDate) -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDate?.toUtcEpochMilliseconds(),
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toKotlinLocalDate()
                        ?.let(onDateConfirm)
                },
                enabled =
                    datePickerState.selectedDateMillis != null,
            ) {
                Text(
                    text = "확인",
                    style = YadanTypography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = "취소",
                    style = YadanTypography.labelLarge,
                )
            }
        },
        shape = YadanShapes.large,
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
        )
    }
}

private fun LocalDate.toKoreanDateString(): String =
    "${year}년 ${month.number}월 ${day}일"

private fun LocalDate.toUtcEpochMilliseconds(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toKotlinLocalDate(): LocalDate =
    Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date

private const val NICKNAME_MAX_LENGTH = 12
private const val DEFAULT_NICKNAME_ERROR_MESSAGE =
    "닉네임을 확인해주세요."

@Preview(
    name = "Basic information",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun BasicInfoScreenPreview() {
    YadanbeopseokTheme {
        BasicInfoScreen(
            nickname = "야구좋아",
            isNicknameValid = true,
            nicknameValidationMessage = "사용 가능한 닉네임이에요",
            selectedGender = Gender.MALE,
            birthDate = LocalDate(1998, 5, 17),
            birthDateValidationMessage = null,
            isNextEnabled = true,
            onNicknameChange = {},
            onGenderSelect = {},
            onBirthDateChange = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
