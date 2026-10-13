package org.telegram.divo.screen.reg_select_role.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme

private const val TITLE_FONT_SIZE = 32f
private const val TITLE_MIN_FONT_SIZE = 20f
private const val TITLE_LINE_HEIGHT_RATIO = 36f / 32f

@Composable
fun HeadText(
    title: String,
    subTitle: String
) {
    Column(
        modifier = Modifier
    ) {
        val text = title.uppercase()
        val style = AppTheme.typography.helveticaNeueLtCom
        val measurer = rememberTextMeasurer()
        BoxWithConstraints {
            // Shrink the title until its longest word fits on one line, so a long word
            // ("ПРОФЕССИОНАЛЬНЫЙ") isn't broken mid-word across lines
            val maxWidthPx = constraints.maxWidth
            val fontSize = remember(text, maxWidthPx, style) {
                val longestWord = text.split(' ', '\n').maxByOrNull { it.length }.orEmpty()
                var size = TITLE_FONT_SIZE
                while (size > TITLE_MIN_FONT_SIZE &&
                    measurer.measure(longestWord, style.copy(fontSize = size.sp), softWrap = false).size.width > maxWidthPx
                ) {
                    size -= 1f
                }
                size
            }
            Text(
                text = text,
                style = style,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * TITLE_LINE_HEIGHT_RATIO).sp,
                color = AppTheme.colors.textPrimary
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = subTitle,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary.copy(0.8f)
        )
    }
}
