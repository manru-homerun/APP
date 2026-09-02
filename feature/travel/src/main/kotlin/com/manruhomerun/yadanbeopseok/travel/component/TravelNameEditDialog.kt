package com.manruhomerun.yadanbeopseok.travel.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTextField
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme

/** 여행 이름을 입력받고, 앞뒤 공백을 제거한 이름을 전달합니다. */
@Composable
internal fun TravelNameEditDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    var draftName by rememberSaveable(currentName) { mutableStateOf(currentName) }
    val normalizedName = draftName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "여행 이름 변경", style = YadanTypography.titleMedium) },
        text = {
            YadanTextField(
                value = draftName,
                onValueChange = { draftName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "여행 이름을 입력해주세요",
                clearContentDescription = "여행 이름 지우기",
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(normalizedName) }, enabled = normalizedName.isNotEmpty()) {
                Text(text = "변경")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}

@Preview(name = "여행 이름 변경")
@Composable
private fun TravelNameEditDialogPreview() = YadanbeopseokTheme {
    TravelNameEditDialog(currentName = "부산 사직 직관 여행", onDismiss = {}, onConfirm = {})
}
