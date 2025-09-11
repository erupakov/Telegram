package org.telegram.divo.screen.reg_select_role

import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.TextDescription
import org.telegram.divo.components.TextItemDescription
import org.telegram.divo.components.TextItemTitle
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButton
import org.telegram.messenger.R


// ---- Theme bits (adjust if you already have these in your design system) ----
private val Copper = Color(0xFFBF7A54)
private val CopperLine = Color(0xFFDF966d)

private val CardStroke = Color(0x66FFFFFF)
private val CardBg = Color(0x1A000000) // translucent dark

// ---- Public API --------------------------------------------------------------
enum class Role { NEW_TALENT, MODEL, AGENCY_SCOUTS }

data class RoleOption(
    val role: Role,
    val title: String,
    val description: String,
    val imageRes: Int
)

@Preview
@Composable
fun RoleSelectionHost(
    onContinue: (Role) -> Unit = {},
    onSelect: (Role) -> Unit = {}
) {
    val options = listOf(
        RoleOption(
            role = Role.NEW_TALENT,
            title = "New Talent",
            description = "I don’t have any experience / have little experience. I’m new in.",
            imageRes = R.drawable.divo_role_new_talent
        ),
        RoleOption(
            role = Role.MODEL,
            title = "Model",
            description = "I have working experience as a model. I’m professional.",
            imageRes = R.drawable.divo_role_model
        ),
        RoleOption(
            role = Role.AGENCY_SCOUTS,
            title = "Agencies & Scouts",
            description = "Looking for / working with models.",
            imageRes = R.drawable.divo_role_agency
        )
    )

    RoleSelectionScreen(
        options = options,
        onSelect = onSelect,
        onContinue = onContinue,
        backgroundImageRes = R.drawable.divo_role_background // optional backdrop
    )
}

/**
 * Role selection screen. Uses your existing text components.
 */
@Composable
fun RoleSelectionScreen(
    modifier: Modifier = Modifier,
    options: List<RoleOption>,
    selected: Role? = null,
    onSelect: (Role) -> Unit,
    onContinue: (Role) -> Unit,
    backgroundImageRes: Int = R.drawable.divo_role_background
) {
    var current by remember(selected) { mutableStateOf(selected) }

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    TextTitle(
                        text = "CHOOSE A ROLE",
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                        // center the big title
                        // (your TextTitle supports TextAlign via parameter; pass in style if needed)
                    )

                    Spacer(Modifier.height(12.dp))

                    TextDescription(
                        text = "Select the role that your profile\nwill correspond to. The role can\nbe changed at any time",
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    LazyColumn(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(options) { item ->
                            RoleCard(
                                option = item,
                                selected = current == item.role,
                                onClick = {
                                    current = item.role
                                    onSelect(item.role)
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            UIButton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom =
                8.dp),
                enabled = current != null,
                text = "Continue"
            ) {
                current?.let {
                    onContinue(it)
                }
            }
        }
    }
}

// ---- Card --------------------------------------------------------------------
@Composable
private fun RoleCard(
    option: RoleOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val stroke = if (selected) Copper else CardStroke

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .border(width = 1.dp, color = stroke, shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.background(color = Color.White.copy(alpha = 0.2f)).blur(23.dp).fillMaxSize())

        Row(
            Modifier.fillMaxSize().padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(option.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                TextItemTitle(
                    text = option.title.uppercase(),
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                TextItemDescription(
                    text = option.description,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.width(12.dp))

            SelectionMark(modifier = Modifier.padding(end = 16.dp), selected = selected)
        }
    }
}

@Composable
private fun SelectionMark(
    modifier: Modifier,
    selected: Boolean) {
    Box(modifier){
        val ring = if (selected) CopperLine else Color.White.copy(alpha = 0.7f)
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, ring, CircleShape)
                .clip(CircleShape)
                .background(if (selected) Copper else Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.divo_check_ic),
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
