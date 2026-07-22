package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Travel

/**
 * 여행 상세 및 일정 편집 화면 상단에 표시하는 여행 정보 헤더입니다.
 *
 * @param travel 표시할 여행입니다.
 * @param currentUserId 현재 로그인한 사용자의 ID입니다.
 * @param dateText 화면에 표시할 여행 기간입니다. 예: `5.22~5.23`
 * @param modifier 헤더의 크기와 배치를 지정합니다.
 * @param onRenameClick 이름 변경 버튼을 눌렀을 때 실행합니다.
 * null이거나 현재 사용자가 방장이 아니면 버튼을 표시하지 않습니다.
 * @param enabled 이름 변경 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanTravelHeader(
    travel: Travel,
    currentUserId: String,
    dateText: String?,
    modifier: Modifier = Modifier,
    onRenameClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val title =
        travel.name
            ?.takeIf { name -> name.isNotBlank() }
            ?: "${travel.region.displayName} 원정 여행"

    val isLeader =
        travel.participants.any { participant ->
            participant.user.id == currentUserId &&
                participant.isLeader
        }

    YadanTravelHeaderContent(
        title = title,
        dateText = dateText,
        isLeader = isLeader,
        modifier = modifier,
        onRenameClick = onRenameClick,
        enabled = enabled,
    )
}

@Composable
private fun YadanTravelHeaderContent(
    title: String,
    dateText: String?,
    isLeader: Boolean,
    modifier: Modifier = Modifier,
    onRenameClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val roleText = if (isLeader) "방장" else "동행자"
    val subtitle =
        dateText
            ?.takeIf { text -> text.isNotBlank() }
            ?.let { text -> "$text · $roleText" }
            ?: roleText

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanPrimaryGradient)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 15.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style =
                        YadanTypography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    color = YadanOnPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 6.dp),
                    style =
                        YadanTypography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = YadanOnPrimary.copy(alpha = 0.9f),
                )
            }

            if (isLeader && onRenameClick != null) {
                Button(
                    onClick = onRenameClick,
                    modifier = Modifier.heightIn(min = 40.dp),
                    enabled = enabled,
                    shape = MaterialTheme.shapes.small,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = YadanOnPrimary,
                            contentColor = YadanPrimaryInk,
                            disabledContainerColor =
                                YadanOnPrimary.copy(alpha = 0.55f),
                            disabledContentColor =
                                YadanPrimaryInk.copy(alpha = 0.55f),
                        ),
                    elevation = null,
                    contentPadding =
                        PaddingValues(
                            horizontal = 10.dp,
                            vertical = 0.dp,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "이름 변경",
                        style =
                            YadanTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Yadan travel header",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelHeaderPreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YadanTravelHeaderContent(
                title = "부산 사직 직관 여행",
                dateText = "5.22~5.23",
                isLeader = true,
                onRenameClick = {},
            )

            YadanTravelHeaderContent(
                title = "광주 KIA 원정 응원 여행",
                dateText = "6.13~6.14",
                isLeader = false,
            )
        }
    }
}
