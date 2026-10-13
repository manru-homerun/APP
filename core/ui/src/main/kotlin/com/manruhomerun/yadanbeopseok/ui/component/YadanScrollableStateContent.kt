package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 본문을 대체하는 안내를 가운데 배치하고, 본문보다 길어지면 스크롤을 허용합니다.
 * 높이가 정해진 본문에서만 사용하며 LazyColumn 항목 안에서는 사용하지 않습니다.
 */
@Composable
fun YadanScrollableStateContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 24.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().padding(contentPadding)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
