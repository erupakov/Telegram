package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.formattedAge
import org.telegram.divo.common.toCountryFlagEmoji
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SimilarProfilesRow(
    modifier: Modifier = Modifier,
    similarItems: List<AgencyModel>,
    onClicked: (Int) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.SimilarProfiles),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 18.sp,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(18.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(
                items = similarItems,
                key = { it.userId }
            ) { model ->
                val age = model.birthday
                val city = model.city

                Column(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(4.dp),
                            ambientColor = Color(0x17000000),
                            spotColor = Color(0x17000000)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .clickableWithoutRipple {
                            onClicked(model.userId)
                        }
                ) {
                    DivoAsyncImage(
                        modifier = Modifier
                            .size(158.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        model = model.photoUrl,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = model.name.uppercase(),
                            style = AppTheme.typography.helveticaNeueLtCom,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (age != null || city != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                age?.let {
                                    Text(
                                        text = "${it.formattedAge()} · ",
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                    )
                                }
                                city?.let {
                                    Text(
                                        text = it.countryCode.toCountryFlagEmoji(),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = it.name,
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}