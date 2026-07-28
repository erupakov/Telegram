package org.telegram.divo.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.items.PageIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import kotlin.math.absoluteValue

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.common.arch.setDivoContent
import org.telegram.divo.style.DivoFont

object OnboardingScreen {
    @JvmStatic
    fun mountOnboarding(composeView: ComposeView,onNext: () -> Unit, onSkipped: () -> Unit
    ) {
        composeView.setDivoContent {
            OnboardingHost(onNext, onSkipped)
        }
    }
}

@Preview
@Composable
fun OnboardingHost(navigateNext: () -> Unit = {}, onSkipped: () -> Unit = {}) {
    val pages = listOf(
        OnboardPage(R.drawable.divo_onboarding_1_img, R.string.OnboardingTitleGetSeenByTheRightPeople, R.string.OnboardingSubtitleGetSeenByTheRightPeople),
        OnboardPage(R.drawable.divo_onboarding_2_img, R.string.OnboardingTitleRealCastingsRealOpportunities, R.string.OnboardingSubtitleRealCastingsRealOpportunities),
        OnboardPage(R.drawable.divo_onboarding_3_img, R.string.OnboardingTitleDiscoveredFasterWithAI, R.string.OnboardingSubtitleDiscoveredFasterWithAI)
    )
    OnboardingScreen(pages = pages, onContinue = navigateNext, onSkipped = onSkipped)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    pages: List<OnboardPage>,
    onContinue: () -> Unit,
    onSkipped: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val topInset = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val data = pages[page]
            val pageOffset =
                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val alpha = 1f - pageOffset.coerceIn(0f, 1f)
            val scale = 1f + 0.05f * pageOffset

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                    }
            ) {
                Image(
                    painter = painterResource(id = data.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 132.dp + bottomInset),
                    verticalArrangement = Arrangement.Bottom
                ) {

                    Image(
                        modifier = Modifier.height(27.dp),
                        painter = painterResource(R.drawable.divo_logo_onboarding),
                        contentDescription = null,
                    )
                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = stringResource(data.titleRes).uppercase(),
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        color = AppTheme.colors.textColor
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(data.subtitleRes),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontFamily = DivoFont.HelveticaNeue,
                        color = AppTheme.colors.textColor.copy(0.8f)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp + bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(
                numberOfPages = pages.size,
                selectedPage = pagerState.currentPage,
                selectedColor = AppTheme.colors.accentOrange,
                defaultColor = Color(0xffBFC6CC),
                defaultRadius = 8.dp,
                selectedLength = 24.dp,
                space = 8.dp,
                modifier = Modifier
            )
            Spacer(Modifier.height(32.dp))

            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.ButtonContinue)
            ) {
                if (pagerState.currentPage < pages.lastIndex) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onContinue()
                }
            }
        }

        if (pagerState.currentPage < pages.lastIndex) {
            Text(
                text = stringResource(R.string.OnboardingSkip),
                color = Color.White,
                fontFamily = DivoFont.HelveticaNeue,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = topInset + 24.dp, end = 16.dp)
                    .clickableWithoutRipple { onSkipped() }
                    .padding(8.dp)
            )
        }
    }
}
