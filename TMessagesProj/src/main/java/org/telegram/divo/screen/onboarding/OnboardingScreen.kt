package org.telegram.divo.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButton
import org.telegram.divo.components.pager.PageIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import kotlin.math.absoluteValue

import androidx.compose.ui.platform.ComposeView

@Composable
private fun IntroCompose(onNext: () -> Unit) {
    OnboardingHost(navigateNext = onNext)
}

object OnboardingScreen {
    @JvmStatic
    fun mountOnboarding(composeView: ComposeView,onNext: () -> Unit
    ) {
        composeView.setContent {
            OnboardingHost(onNext)
        }
    }
}




@Preview
@Composable
fun OnboardingHost(navigateNext: () -> Unit = {}) {
    val pages = listOf(
        OnboardPage(R.drawable.divo_onboarding_1_img, "STEP INTO THE FASHION WORLD"),
        OnboardPage(R.drawable.divo_onboarding_2_img, "FROM SELFIE TO SPOTLIGHT"),
        OnboardPage(R.drawable.divo_onboarding_3_img, "WHERE NEW MODELS ARE BORN")
    )
    OnboardingScreen(pages = pages, onContinue = navigateNext)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    pages: List<OnboardPage>,
    onContinue: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

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
                        .padding(bottom = 175.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {

                    Image(
                        modifier = Modifier.height(27.dp),
                        painter = painterResource(R.drawable.divo_logo_onboarding),
                        contentDescription = null,
                    )
                    Spacer(Modifier.height(16.dp))

                    TextTitle(
                        data.subtitle.uppercase(),
                        color = AppTheme.colors.textColor
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(
                numberOfPages = pages.size,
                selectedPage = pagerState.currentPage,
                selectedColor = Color(0xFFC57B53),
                defaultColor = Color(0xffBFC6CC),
                defaultRadius = 8.dp,
                selectedLength = 24.dp,
                space = 8.dp,
                modifier = Modifier
            )
            Spacer(Modifier.height(16.dp))

            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = "Continue"
            ) {
                onContinue()
            }
        }
    }
}
