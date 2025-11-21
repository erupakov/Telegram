package org.telegram.divo.screen.event_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
//import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.telegram.messenger.R

// ====== PUBLIC ENTRY ======

@Preview
@Composable
fun EventDetailsScreen(
        headerImageUrl: String = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_details.png?alt=media",
        title: String = "FASHION MODEL EVENT",
        dateTime: String = "May 27 · 5:00 PM · 🇺🇸 New York",
        participants: String = "1024",
        views: String = "2.4k",
        organizerName: String = "@nyfw",
        organizerStatus: String = "Online",
        about: String = "Casting of models for a contract with the magazine on the initiative of the NYFW magazine in NY",
        parameters: List<ApplyParam> = listOf(
                ApplyParam("Weight", "50–55 kg", Icons.Outlined.MoreVert), // иконки-заглушки
                ApplyParam("Age", "20–25 y.o", Icons.Outlined.MoreVert),
                ApplyParam("Gender", "Only womans", Icons.Outlined.MoreVert)
        ),
        galleryUrls: List<String> = listOf(
                "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fevent_image_1.png?alt=media",
                "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fevent_image_2.png?alt=media",
                "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fevent_image_3.png?alt=media"
        ),
        previousEvents: List<PrevEventCard> = List(6) {
            i -> PrevEventCard(id = i, title = if (i % 2 == 0) "Conference" else "Casting", likes = "1K", imageUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_1.png?alt=media") },
        onBack: () -> Unit = {},
        onLike: () -> Unit = {},
        onShare: () -> Unit = {},
        onBookmark: () -> Unit = {},
        onMore: () -> Unit = {},
        onApply: () -> Unit = {},
        topBarAppearOffset: Dp = 220.dp,         // на какой высоте появится TopAppBar
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val triggerPx = with(density) { topBarAppearOffset.toPx() }
    val showTopBar by remember {
        derivedStateOf {
            // Показываем TopBar, когда прокрутили хедер
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset.toFloat() > triggerPx
        }
    }

    Scaffold(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
            topBar = {
                AnimatedVisibility(
                        visible = showTopBar,
                        enter = fadeIn(),
                        exit = fadeOut()
                ) {
//                SmallTopAppBar(
//                    title = {
//                        Text(
//                            text = title,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    },
//                    navigationIcon = {
//                        Text(
//                            "Back",
//                            modifier = Modifier
//                                .padding(start = 16.dp)
//                                .clickable { onBack() },
//                            fontSize = 16.sp
//                        )
//                    },
//                    actions = {
//                        IconButton(onClick = onLike) { Text("♥") }
//                        IconButton(onClick = onShare) { Icon(Icons.Outlined.Share, null) }
//                        IconButton(onClick = onBookmark) { Icon(Icons.Outlined.BookmarkBorder, null) }
//                        IconButton(onClick = onMore) { Icon(Icons.Outlined.MoreVert, null) }
//                    }
//                )
                }
            }
    ) { padding ->
        EventDetailsContent(
                modifier = Modifier.padding(padding),
                listState = listState,
                headerImageUrl = headerImageUrl,
                title = title,
                dateTime = dateTime,
                participants = participants,
                views = views,
                organizerName = organizerName,
                organizerStatus = organizerStatus,
                about = about,
                parameters = parameters,
                galleryUrls = galleryUrls,
                previousEvents = previousEvents,
                onBack = onBack,
                onLike = onLike,
                onShare = onShare,
                onBookmark = onBookmark,
                onMore = onMore,
                onApply = onApply,
                topBarAppearOffset = topBarAppearOffset
        )
    }
}

// ====== INTERNAL UI ======

@Composable
private fun EventDetailsContent(
        modifier: Modifier,
        listState: LazyListState,
        headerImageUrl: String,
        title: String,
        dateTime: String,
        participants: String,
        views: String,
        organizerName: String,
        organizerStatus: String,
        about: String,
        parameters: List<ApplyParam>,
        galleryUrls: List<String>,
        previousEvents: List<PrevEventCard>,
        onBack: () -> Unit,
        onLike: () -> Unit,
        onShare: () -> Unit,
        onBookmark: () -> Unit,
        onMore: () -> Unit,
        onApply: () -> Unit,
        topBarAppearOffset: Dp
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // HEADER
        item(key = "header") {
            EventHeader(
                    imageUrl = headerImageUrl,
                    title = title,
                    dateTime = dateTime,
                    participants = participants,
                    views = views,
                    onBack = onBack,
                    onLike = onLike,
                    onShare = onShare,
                    onBookmark = onBookmark,
                    onMore = onMore,
                    onApply = onApply,
                    minHeight = topBarAppearOffset + 80.dp
            )
        }

        // ORGANIZER CARD
        item(key = "organizer") {
            OrganizerCard(
                    name = organizerName,
                    status = organizerStatus,
                    role = "Organization"
            )
        }

        // ABOUT
        item(key = "aboutTitle") {
            SectionTitle("About")
        }
        item(key = "about") {
            Text(
                    text = about,
                    fontSize = 14.sp,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp
            )
            Spacer(Modifier.height(12.dp))
        }

        // PARAMETERS
        item(key = "paramsTitle") {
            SectionTitle("Parameters for Applying")
        }
        items(parameters, key = { it.title }) { p ->
            ParamRow(title = p.title, value = p.value)
            Divider(color = Color(0xFFE8E8E8))
        }
        item { Spacer(Modifier.height(8.dp)) }

        // GALLERY (3 thumbnails)
        item(key = "gallery") {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                galleryUrls.forEach { url ->
                    Thumbnail(url, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // PREVIOUS EVENTS
        item(key = "prevTitle") {
            SectionTitle("PREVIOUS EVENTS", upper = true)
        }

        item {
            LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                items(previousEvents) { item ->
                    PreviousEventCard(item, Modifier
                            .height(180.dp)
                            .width(230.dp))

                }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EventHeader(
        imageUrl: String,
        title: String,
        dateTime: String,
        participants: String,
        views: String,
        onBack: () -> Unit,
        onLike: () -> Unit,
        onShare: () -> Unit,
        onBookmark: () -> Unit,
        onMore: () -> Unit,
        onApply: () -> Unit,
        minHeight: Dp
) {
    Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
    ) {
        // Картинка
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )
        } else {
            // Заглушка
            Box(
                    Modifier
                            .fillMaxSize()
                            .background(Color(0xFF222222))
            )
        }

        // Градиент затемнения
        Box(
                Modifier
                        .matchParentSize()
                        .background(
                                Brush.verticalGradient(
                                        0f to Color.Black.copy(alpha = 0.45f),
                                        0.65f to Color.Transparent,
                                        1f to Color.Black.copy(alpha = 0.70f)
                                )
                        )
        )

        // Top row (кнопки поверх картинки)
        Row(
                Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {

            }) {
                Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_divo_back),
                        contentDescription = null,
                        tint = Color.White)

                Text(
                        "Back",
                        color = Color.White,
                        modifier = Modifier
                                .clip(RoundedCornerShape(20))
                                .clickable { onBack() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSmallText("1K")
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onShare) { Icon(Icons.Outlined.Share, null, tint = Color.White) }
                IconButton(onClick = onBookmark) { Icon(Icons.Outlined.Share, null, tint = Color.White) }
                IconButton(onClick = onMore) { Icon(Icons.Outlined.MoreVert, null, tint = Color.White) }
            }
        }

        // Center content (бейдж + заголовок + дата/город)
        Column(
                Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Pill(text = "Casting")
            Spacer(Modifier.height(10.dp))
            Text(
                    text = title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                    text = dateTime,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
            )
        }

        // Bottom stats + Apply
        Row(
                Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            StatBlock(number = participants, label = "participants")
            Spacer(Modifier.width(16.dp))
            StatBlock(number = views, label = "views")
            Spacer(Modifier.weight(1f))
            Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF825E))
            ) { Text("Apply") }
        }
    }
}

@Composable
private fun IconSmallText(text: String) {
    Box(
            Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun Pill(text: String) {
    Box(
            Modifier
                    .clip(RoundedCornerShape(20))
                    .background(Color(0xFF2E2E2E).copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun StatBlock(number: String, label: String) {
    Column {
        Text(
                text = number,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
        )
        Text(
                text = label,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
        )
    }
}

@Composable
private fun OrganizerCard(
        name: String,
        status: String,
        role: String
) {
    Surface(
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
    ) {
        Row(
                Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар-заглушка
            Box(
                    Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(status, fontSize = 12.sp, color = Color.Gray)
            }
            Text(role, fontSize = 12.sp, color = Color.Gray)
        }
        Divider(color = Color(0xFFE8E8E8))
    }
}

@Composable
private fun SectionTitle(text: String, upper: Boolean = false) {
    Text(
            text = if (upper) text.uppercase() else text,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ParamRow(title: String, value: String) {
    Row(
            Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка-заглушка
        Box(
                Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDDDD))
        )
        Spacer(Modifier.width(12.dp))
        Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
        )
        Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF6A6A6A)
        )
    }
}

@Composable
private fun Thumbnail(url: String, modifier: Modifier = Modifier) {
    Box(modifier) {
        if (url.isNotBlank()) {
            AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )
        } else {

        }
    }
}

data class PrevEventCard(val id: Int, val title: String, val likes: String, val imageUrl: String)

@Composable
private fun PreviousEventCard(card: PrevEventCard, modifier: Modifier = Modifier) {
    Box(
            modifier
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E1E1E))
    ) {
        if (card.imageUrl.isNotBlank()) {
            AsyncImage(
                    model = card.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )
        }
        // тёмный градиент снизу
        Box(
                Modifier
                        .matchParentSize()
                        .background(
                                Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        1f to Color.Black.copy(alpha = 0.55f)
                                )
                        )
        )
        // бейджи
        Row(
                Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(card.title)
            Spacer(Modifier.width(6.dp))
            IconSmallText(card.likes)
        }
    }
}

// ====== MODELS ======

data class ApplyParam(
        val title: String,
        val value: String,
        val // иконка не используется, оставлена для расширения
        unused: Any? = null
)
