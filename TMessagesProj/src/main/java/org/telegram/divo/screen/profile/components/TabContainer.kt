package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.telegram.divo.components.DivoTabSelector
import org.telegram.divo.components.TabConfig

enum class ProfileDestination {
    SONGS,
    ALBUM,
    AGENCY,
    PLAYLISTS,
    EVENT,
}

enum class ProfileInfoDestination {
    BIOGRAPHY,
    APPEARANCE,
    EXPERIENCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabContainer(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    destinations: List<TabConfig>,
    tabWidth: Dp? = null
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        DivoTabSelector(
            modifier = Modifier,
            tabs = destinations,
            selectedIndex = pagerState.currentPage,
            onTabSelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            horizontalPadding = 16.dp,
            tabWidth = tabWidth
        )
    }
}
