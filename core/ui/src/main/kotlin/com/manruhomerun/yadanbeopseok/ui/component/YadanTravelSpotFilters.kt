package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory

/**
 * 인기 관광지와 찜한 관광지에서 공통으로 사용하는 지역 선택 메뉴입니다.
 */
@Composable
fun YadanTravelRegionDropdown(
    selectedRegion: Region,
    onRegionSelected: (Region) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            expanded = false
        }
    }

    Column(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = YadanTextPrimary),
        ) {
            Text(
                text = selectedRegion.displayName,
                style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "지역 선택",
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 140.dp),
            shape = YadanShapes.medium,
            containerColor = YadanSurface,
        ) {
            Region.entries.forEach { region ->
                val isSelected = region == selectedRegion

                DropdownMenuItem(
                    text = {
                        Text(
                            text = region.displayName,
                            style = YadanTypography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onRegionSelected(region)
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = YadanPrimary,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/**
 * 전체와 서버가 지원하는 관광지 카테고리를 가로 필터로 표시합니다.
 * 전체를 선택하면 null을 전달하여 category 쿼리를 생략할 수 있게 합니다.
 */
@Composable
fun YadanTravelSpotCategoryFilters(
    selectedCategory: TravelSpotFilterCategory?,
    onCategorySelected: (TravelSpotFilterCategory?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val categories = TravelSpotFilterCategory.entries

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        item(key = "all") {
            YadanFilterChip(
                text = "전체",
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                enabled = enabled,
            )
        }

        items(
            items = categories,
            key = TravelSpotFilterCategory::name,
        ) { category ->
            YadanFilterChip(
                text = category.displayName,
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                enabled = enabled,
            )
        }
    }
}

@Preview(
    name = "관광지 서버 필터",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTravelSpotFiltersPreview() {
    YadanbeopseokTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(YadanBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YadanTravelRegionDropdown(
                selectedRegion = Region.entries.first(),
                onRegionSelected = {},
            )

            YadanTravelSpotCategoryFilters(
                selectedCategory = null,
                onCategorySelected = {},
            )
        }
    }
}
