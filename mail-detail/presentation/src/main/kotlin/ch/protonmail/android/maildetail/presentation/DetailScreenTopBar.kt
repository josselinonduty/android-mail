/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.maildetail.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTheme3
import me.proton.core.compose.theme.overline

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DetailScreenTopBar(
    modifier: Modifier = Modifier,
    title: String,
    isStarred: Boolean,
    messageCount: Int? = null,
    actions: DetailScreenTopBar.Actions,
    scrollBehavior: TopAppBarScrollBehavior
) {
    ProtonTheme3 {
        LargeTopAppBar(
            modifier = modifier,
            title = {
                Column {
                    messageCount?.let { count ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = pluralStringResource(R.plurals.message_count_label_text, count, count),
                            fontSize = ProtonTheme.typography.overline.fontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                    val isFullyExpanded = scrollBehavior.state.collapsedFraction == 0F
                    Text(
                        maxLines = if (isFullyExpanded) 2 else 1,
                        text = title,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                }
            },
            navigationIcon = {
                IconButton(onClick = actions.onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.presentation_back))
                }
            },
            actions = {
                fun onStarIconClicked() = if (isStarred) {
                    actions.onUnStarClick()
                } else {
                    actions.onStarClick()
                }
                IconButton(onClick = ::onStarIconClicked) {
                    Icon(
                        modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                        painter = getStarredIcon(isStarred),
                        contentDescription = NO_CONTENT_DESCRIPTION,
                        tint = getStarredIconColor(isStarred)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = ProtonTheme.colors.backgroundNorm,
                scrolledContainerColor = ProtonTheme.colors.backgroundSecondary,
                navigationIconContentColor = ProtonTheme.colors.iconNorm,
                titleContentColor = ProtonTheme.colors.textNorm
            )
        )
    }
}

@Composable
private fun getStarredIconColor(isStarred: Boolean) = if (isStarred) {
    colorResource(id = R.color.sunglow)
} else {
    ProtonTheme.colors.textNorm
}

@Composable
private fun getStarredIcon(isStarred: Boolean) = painterResource(
    id = if (isStarred) {
        R.drawable.ic_proton_star_filled
    } else {
        R.drawable.ic_proton_star
    }
)

object DetailScreenTopBar {

    data class Actions(
        val onBackClick: () -> Unit,
        val onStarClick: () -> Unit,
        val onUnStarClick: () -> Unit
    )
}
