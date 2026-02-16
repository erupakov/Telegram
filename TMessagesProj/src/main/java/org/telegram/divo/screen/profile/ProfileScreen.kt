package org.telegram.divo.screen.profile

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.PortfolioItemImage
import org.telegram.divo.components.PortfolioUploadPreview
import org.telegram.divo.components.TelegramPhoto
import org.telegram.divo.components.TelegramPhotoBackground
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.items.ButtonAddWorkHistory
import org.telegram.divo.items.PofileNameItem
import org.telegram.divo.items.ProfileBioItem
import org.telegram.divo.items.ProfileSocialItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.BackupImageView

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onAddPortfolioClicked: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    // Refresh data when screen becomes visible
    LifecycleResumeEffect(Unit) {
        viewModel.getData()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileViewModel.ProfileEffect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
                else -> {}
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value

    ProfileScreenParts(
        uiState = uiState,
        onEditClicked = {
            onEditClicked()
        },
        onEditLinksClicked = {
            onEditLinksClicked()
        },
        onNavigateBack = onNavigateBack,
        showWorkHistory = showWorkHistory,
        onAddPortfolioClicked = onAddPortfolioClicked,
        onEditBackgroundClicked = onEditBackgroundClicked,
        onSocialLinkClicked = { url -> viewModel.openSocialLink(url) }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyWithPagerInside() {
    val pagerState = rememberPagerState(pageCount = { 3 })

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Text("HEADER", Modifier.align(Alignment.Center))
            }
        }

        stickyHeader {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("TABS", Modifier.align(Alignment.Center))
            }
        }

        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillParentMaxHeight() // 🔑 ключ
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(50) {
                        Text(
                            "Page $page item $it",
                            Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileCollapsingScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileViewModel.ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val tabsHeight = 52.dp
    val headerMax = 260.dp
    val headerMin = 96.dp

    val density = LocalDensity.current
    val headerMaxPx = with(density) { headerMax.toPx() }
    val headerMinPx = with(density) { headerMin.toPx() }
    val collapseRange = headerMaxPx - headerMinPx
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) } // 0..collapseRange

    // Nested scroll: сначала схлопываем header, потом отдаём скролл списку
    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy == 0f) return Offset.Zero

                // dy < 0 = скролл вверх (схлопнуть), dy > 0 = вниз (раскрыть)
                val newOffset = (collapseOffsetPx - dy).coerceIn(0f, collapseRange)
                val consumed = newOffset - collapseOffsetPx
                collapseOffsetPx = newOffset

                // мы "потребили" часть скролла на схлопывание/раскрытие header
                return Offset(0f, -consumed)
            }
        }
    }

    val headerHeightPx = headerMaxPx - collapseOffsetPx
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    val name = (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")

    // синхронизация tab -> pager
    LaunchedEffect(selectedTab) { pagerState.animateScrollToPage(selectedTab) }
    // синхронизация pager -> tab
    LaunchedEffect(pagerState.currentPage) { selectedTab = pagerState.currentPage }


//    Column {
//        ToolBar(
//            title = name,
//            titleVisible = true, //titleVisible,
//            modifier = Modifier
//                .background(Color.Transparent),
//            onEditClicked = {
//                onEditClicked()
//            },
//            onNavigateBack = {
//                onNavigateBack()
//            }
//        )
        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(nestedScroll)
        ) {

            Column(Modifier.fillMaxSize()) {

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(headerHeightDp)
                ) {
//                    ProfileHeaderContent(
//                        uiState,
//                        onEditLinksClicked,
//                        showWorkHistory
//                    )

                }

                TabContainer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(tabsHeight)
                )

                // PAGER (занимает оставшееся место)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // ВАЖНО: каждая страница обычно со своим LazyColumn
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(50) { idx ->
                            Text("Tab $page item $idx", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
//    }


}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreenParts(
    modifier: Modifier = Modifier,
    uiState: ProfileViewModel.ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onAddPortfolioClicked: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {},
    onSocialLinkClicked: (String) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val titleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    // Show background from profile if available, otherwise fallback to default
    if (uiState.backgroundPhoto != null) {
        TelegramPhotoBackground(
            photo = uiState.backgroundPhoto,
            dialogId = uiState.userFull.id,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        OnePhotoTopSharpBottomBlur(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.divo_profile_background_test)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 36.dp),
    ) {

        val name =
            (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")

        ToolBar(
            title = name,
            titleVisible = titleVisible,
            modifier = Modifier
                .background(Color.Transparent),
            onEditClicked = {
                onEditClicked()
            },
            onNavigateBack = {
                onNavigateBack()
            },
            onEditBackgroundClicked = onEditBackgroundClicked
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item {
                ProfileHeaderContent(
                    uiState = uiState,
                    onEditLinksClicked = onEditLinksClicked,
                    showWorkHistory = showWorkHistory,
                    onSocialLinkClicked = onSocialLinkClicked
                )
            }
            stickyHeader {
                TabContainer()
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillParentMaxHeight().fillMaxWidth().background(color = Color.White)
                ) { page ->
                    when (page) {
                        0 -> PortfolioGrid(
                            portfolioItems = uiState.portfolioItems,
                            isUploading = uiState.portfolioUploading,
                            uploadLocalPath = uiState.portfolioUploadLocalPath,
                            onAddClicked = onAddPortfolioClicked,
                            dialogId = uiState.userFull.id
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.userPhotos) { photo ->
                                TelegramPhoto(
                                    photo = photo,
                                    dialogId = uiState.userFull.id,
                                    modifier = Modifier,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnePhotoTopSharpBottomBlur(
    painter: Painter,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 100.dp
) {
    Box(modifier) {

        // Слой 1: та же картинка — резкая
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Слой 2: та же картинка — blurred, но видна только снизу через градиент-маску
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * 0.45f, // где начинается blur
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
    }
}


@Composable
private fun ToolBar(
    modifier: Modifier = Modifier,
    title: String,
    titleVisible: Boolean = false,
    onEditClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onNavigateBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    titleVisible
                ) {
                    Text(title)
                }
            }
            IconButton(onClick = { onEditClicked() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
            Box {
                IconButton(onClick = { showDropdownMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Background Image") },
                        onClick = {
                            showDropdownMenu = false
                            onEditBackgroundClicked()
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ProfileHeaderContent(
    uiState: ProfileViewModel.ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
    onSocialLinkClicked: (String) -> Unit = {}
) {
    var selectedBioTab by rememberSaveable { mutableIntStateOf(0) }

    Column {
        PofileNameItem(
            modifier = Modifier.padding(top = 150.dp),
            firstName = uiState.userFull.user.first_name ?: "",
            lastName = uiState.userFull.user.last_name ?: "",
            roleLabel = "Model",
            uiState
        )

        ButtonAddWorkHistory(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
            onClick = {
                showWorkHistory()
            }
        )

        // Biography / Appearance Tabs
        BiographyAppearanceTabs(
            selectedTab = selectedBioTab,
            onTabSelected = { selectedBioTab = it },
            uiState = uiState
        )

        // Social Links Section
        SocialLinksSection(
            socialLinks = uiState.socialLinks,
            onEditLinksClicked = onEditLinksClicked,
            onSocialLinkClicked = onSocialLinkClicked
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BiographyAppearanceTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    uiState: ProfileViewModel.ProfileViewState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.blackAlpha12)
    ) {
        // Tab buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton(
                text = "BIOGRAPHY",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "APPEARANCE",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // Tab content
        when (selectedTab) {
            0 -> BiographyContent(bio = uiState.userFull.about ?: "")
            1 -> AppearanceContent(params = uiState.physicalParams)
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AppTheme.colors.accentColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BiographyContent(bio: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = bio.ifEmpty { "No biography available" },
            color = Color.White,
            fontSize = 14.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )

        if (bio.length > 100) {
            Text(
                text = if (expanded) "SEE LESS" else "SEE MORE",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AppearanceContent(params: ProfileViewModel.PhysicalParams) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left column
            Column(modifier = Modifier.weight(1f)) {
                if (params.gender.isNotEmpty()) {
                    AppearanceRow("Gender", params.gender)
                }
                if (params.height > 0) {
                    AppearanceRow("Height (cm)", params.height.toString())
                }
                if (params.hips > 0) {
                    AppearanceRow("Hips (cm)", params.hips.toString())
                }
                if (params.hairLength > 0 || expanded) {
                    AppearanceRow("Hair length (cm)", params.hairLength.toString())
                }
                if (params.eyeColor.isNotEmpty()) {
                    AppearanceRow("Eye color", params.eyeColor)
                }
                if (params.breastSize.isNotEmpty() && params.gender == "Female") {
                    AppearanceRow("Breast size", params.breastSize)
                }
            }

            // Right column
            Column(modifier = Modifier.weight(1f)) {
                if (params.age > 0) {
                    AppearanceRow("Age (y.o)", params.age.toString())
                }
                if (params.waist > 0) {
                    AppearanceRow("Waist (cm)", params.waist.toString())
                }
                if (params.shoeSize > 0) {
                    AppearanceRow("Shoe size (EU)", params.shoeSize.toString())
                }
                if (params.hairColor.isNotEmpty()) {
                    AppearanceRow("Hair color", params.hairColor)
                }
                if (params.skinColor.isNotEmpty()) {
                    AppearanceRow("Skin color", params.skinColor)
                }
            }
        }

        Text(
            text = "SEE MORE",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { expanded = !expanded }
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun AppearanceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SocialLinksSection(
    socialLinks: ProfileViewModel.SocialLinks,
    onEditLinksClicked: () -> Unit,
    onSocialLinkClicked: (String) -> Unit
) {
    // Check if any social links exist
    val hasAnyLinks = socialLinks.instagram.isNotBlank() ||
            socialLinks.tiktok.isNotBlank() ||
            socialLinks.youtube.isNotBlank() ||
            socialLinks.website.isNotBlank()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Links", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onEditLinksClicked) {
                Text("Edit Links".uppercase(), color = Color.White, fontSize = 12.sp)
            }
        }

        if (hasAnyLinks) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (socialLinks.instagram.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_instagram,
                        username = extractUsername(socialLinks.instagram),
                        onClick = { onSocialLinkClicked(socialLinks.instagram) }
                    )
                }
                if (socialLinks.tiktok.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_tiktok,
                        username = extractUsername(socialLinks.tiktok),
                        onClick = { onSocialLinkClicked(socialLinks.tiktok) }
                    )
                }
                if (socialLinks.youtube.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_youtube,
                        username = extractPath(socialLinks.youtube),
                        onClick = { onSocialLinkClicked(socialLinks.youtube) }
                    )
                }
                if (socialLinks.website.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_web,
                        username = extractDomain(socialLinks.website),
                        onClick = { onSocialLinkClicked(socialLinks.website) }
                    )
                }
            }
        } else {
            Text(
                text = "No social links added yet",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SocialLinkItem(
    @DrawableRes iconResId: Int,
    username: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.blackAlpha12)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "@$username",
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun extractUsername(url: String): String {
    if (url.isBlank()) return ""
    return url
        .replace("https://", "")
        .replace("http://", "")
        .replace("www.", "")
        .substringAfter("/")
        .removePrefix("@")
        .trim('/')
        .ifEmpty { url }
}

private fun extractPath(url: String): String {
    if (url.isBlank()) return ""
    return url
        .replace("https://", "")
        .replace("http://", "")
        .replace("www.", "")
        .substringAfter("/")
        .trim('/')
        .ifEmpty { url }
}

private fun extractDomain(url: String): String {
    if (url.isBlank()) return ""
    return url
        .replace("https://", "")
        .replace("http://", "")
        .replace("www.", "")
        .substringBefore("/")
        .ifEmpty { url }
}

enum class Destination(
    val route: String,
    @DrawableRes
    val iconResId: Int,
    val contentDescription: String
) {
    SONGS("songs", R.drawable.divo_profile_tab_1, "Songs"),
    ALBUM("album", R.drawable.divo_profile_tab_2, "Album"),
    PLAYLISTS("playlist", R.drawable.divo_profile_tab_3, "Playlist")
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TabContainer() {

    Box(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.blackAlpha12)
    ) {

        val startDestination = Destination.SONGS
        var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

        PrimaryTabRow(
            selectedTabIndex = selectedDestination, modifier = Modifier,
            containerColor = Color.Transparent,
            indicator = {
                Spacer(
                    Modifier
                        .width(56.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                )
            }
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                Tab(
                    modifier = Modifier.width(100.dp),
                    selected = selectedDestination == index,
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(destination.iconResId),
                            modifier = Modifier.size(24.dp),
                            contentDescription = destination.contentDescription
                        )
                    },
                    onClick = {
                        selectedDestination = index
                    },
                    selectedContentColor = Color.White
                )
            }
        }
    }
}

@Composable
fun PortfolioGrid(
    portfolioItems: List<TLRPC.TL_profile_portfolioItem>,
    isUploading: Boolean,
    uploadLocalPath: String?,
    onAddClicked: () -> Unit,
    dialogId: Long,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PortfolioAddButton(
                isUploading = isUploading,
                uploadLocalPath = uploadLocalPath,
                onClick = onAddClicked
            )
        }

        items(portfolioItems) { item ->
            PortfolioItemImage(
                portfolioItem = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                cornerRadiusDp = 8
            )
        }
    }
}

@Composable
private fun PortfolioAddButton(
    isUploading: Boolean,
    uploadLocalPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.blackAlpha12)
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading && uploadLocalPath != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    PortfolioUploadPreview(
                        filePath = uploadLocalPath,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadiusDp = 8
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        color = AppTheme.colors.accentColor,
                        strokeWidth = 3.dp
                    )
                }
            }
            isUploading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = AppTheme.colors.accentColor,
                    strokeWidth = 3.dp
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoPage(
    modifier: Modifier = Modifier,
) {
}
