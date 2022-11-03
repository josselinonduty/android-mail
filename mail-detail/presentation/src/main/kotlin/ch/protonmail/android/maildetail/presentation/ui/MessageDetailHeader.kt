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

package ch.protonmail.android.maildetail.presentation.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.MessageDetailHeaderUiModel
import ch.protonmail.android.maildetail.presentation.model.ParticipantUiModel
import ch.protonmail.android.mailcommon.presentation.compose.Avatar
import ch.protonmail.android.maildetail.presentation.previewdata.MessageDetailHeaderPreviewData
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmall
import me.proton.core.compose.theme.defaultSmallStrong

@Composable
private fun MessageDetailHeader(
    modifier: Modifier = Modifier,
    uiModel: MessageDetailHeaderUiModel
) {

    val isExpanded = rememberSaveable(inputs = arrayOf()) {
        mutableStateOf(false)
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isExpanded.value
            ) { isExpanded.value = !isExpanded.value }
            .padding(
                start = ProtonDimens.SmallSpacing,
                end = ProtonDimens.DefaultSpacing,
                bottom = ProtonDimens.SmallSpacing
            )
    ) {

        val (
            avatarRef,
            senderRef,
            iconsRef,
            timeRef,
            moreButtonRef,
            allRecipientsRef,
            toRecipientsTitleRef,
            toRecipientsRef,
            ccRecipientsTitleRef,
            ccRecipientsRef,
            bccRecipientsTitleRef,
            bccRecipientsRef,
            labelsRef,
            extendedTimeRef,
            locationRef,
            sizeRef
        ) = createRefs()

        val (
            trackerProtectionInfoRef,
            encryptionInfoRef,
            hideDetailsRef
        ) = createRefs()

        Avatar(
            modifier = modifier.constrainAs(avatarRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
            avatarUiModel = uiModel.avatar
        )

        Sender(
            modifier = modifier.constrainAs(senderRef) {
                width = Dimension.fillToConstraints
                top.linkTo(parent.top, margin = ProtonDimens.SmallSpacing)
                start.linkTo(avatarRef.end, margin = ProtonDimens.SmallSpacing)
                end.linkTo(iconsRef.start, margin = ProtonDimens.SmallSpacing)
            },
            participantUiModel = uiModel.sender
        )

        Icons(
            modifier = modifier.constrainAs(iconsRef) {
                top.linkTo(timeRef.top)
                bottom.linkTo(timeRef.bottom)
                end.linkTo(timeRef.start)
            },
            uiModel = uiModel,
            isExpanded = isExpanded.value
        )

        Time(
            modifier = modifier.constrainAs(timeRef) {
                top.linkTo(parent.top, margin = ProtonDimens.SmallSpacing)
                end.linkTo(parent.end)
            },
            time = uiModel.time
        )

        MoreButton(
            modifier = modifier.constrainAs(moreButtonRef) {
                top.linkTo(timeRef.bottom, margin = ProtonDimens.ExtraSmallSpacing)
                end.linkTo(parent.end)
            }
        )

        AllRecipients(
            modifier = modifier.constrainAs(allRecipientsRef) {
                width = Dimension.fillToConstraints
                top.linkTo(senderRef.bottom, margin = ProtonDimens.ExtraSmallSpacing)
                start.linkTo(avatarRef.end, margin = ProtonDimens.SmallSpacing)
                end.linkTo(moreButtonRef.start, margin = ProtonDimens.SmallSpacing)
                visibility = if (isExpanded.value) Visibility.Gone else Visibility.Visible
            },
            allRecipients = uiModel.allRecipients
        )

        RecipientsTitle(
            modifier = modifier.constrainAs(toRecipientsTitleRef) {
                constrainRecipientsTitle(
                    reference = toRecipientsRef,
                    recipients = uiModel.toRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipientsTitle = R.string.to
        )
        Recipients(
            modifier = modifier.constrainAs(toRecipientsRef) {
                constrainRecipients(
                    topReference = allRecipientsRef,
                    startReference = avatarRef,
                    endReference = moreButtonRef,
                    recipients = uiModel.toRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipients = uiModel.toRecipients
        )

        RecipientsTitle(
            modifier = modifier.constrainAs(ccRecipientsTitleRef) {
                constrainRecipientsTitle(
                    reference = ccRecipientsRef,
                    recipients = uiModel.ccRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipientsTitle = R.string.cc
        )
        Recipients(
            modifier = modifier.constrainAs(ccRecipientsRef) {
                constrainRecipients(
                    topReference = toRecipientsRef,
                    startReference = avatarRef,
                    endReference = moreButtonRef,
                    recipients = uiModel.ccRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipients = uiModel.ccRecipients
        )

        RecipientsTitle(
            modifier = modifier.constrainAs(bccRecipientsTitleRef) {
                constrainRecipientsTitle(
                    reference = bccRecipientsRef,
                    recipients = uiModel.bccRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipientsTitle = R.string.bcc
        )
        Recipients(
            modifier = modifier.constrainAs(bccRecipientsRef) {
                constrainRecipients(
                    topReference = ccRecipientsRef,
                    startReference = avatarRef,
                    endReference = moreButtonRef,
                    recipients = uiModel.bccRecipients,
                    isExpanded = isExpanded.value
                )
            },
            recipients = uiModel.bccRecipients
        )

        Box(
            modifier = modifier.constrainAs(labelsRef) {
                top.linkTo(
                    bccRecipientsRef.bottom,
                    margin = if (isExpanded.value) ProtonDimens.SmallSpacing else ProtonDimens.ExtraSmallSpacing,
                    goneMargin = if (isExpanded.value) ProtonDimens.SmallSpacing else ProtonDimens.ExtraSmallSpacing
                )
                visibility = if (uiModel.labels.isNotEmpty()) Visibility.Visible else Visibility.Gone
            }
        )

        ExtendedHeaderRow(
            modifier = modifier.constrainAs(extendedTimeRef) {
                constrainExtendedHeaderRow(
                    topReference = labelsRef,
                    endReference = moreButtonRef,
                    isExpanded = isExpanded.value
                )
            },
            icon = R.drawable.ic_proton_calendar_today,
            text = uiModel.extendedTime
        )

        ExtendedHeaderRow(
            modifier = modifier.constrainAs(locationRef) {
                constrainExtendedHeaderRow(
                    topReference = extendedTimeRef,
                    endReference = moreButtonRef,
                    isExpanded = isExpanded.value
                )
            },
            icon = uiModel.locationIcon,
            text = uiModel.location
        )

        ExtendedHeaderRow(
            modifier = modifier.constrainAs(sizeRef) {
                constrainExtendedHeaderRow(
                    topReference = locationRef,
                    endReference = moreButtonRef,
                    isExpanded = isExpanded.value
                )
            },
            icon = R.drawable.ic_proton_storage,
            text = uiModel.size
        )

        ExtendedHeaderRow(
            modifier = modifier.constrainAs(trackerProtectionInfoRef) {
                constrainExtendedHeaderRow(
                    topReference = sizeRef,
                    endReference = moreButtonRef,
                    isExpanded = isExpanded.value
                )
            },
            icon = R.drawable.ic_proton_shield,
            text = "Placeholder text"
        )

        ExtendedHeaderRow(
            modifier = modifier.constrainAs(encryptionInfoRef) {
                constrainExtendedHeaderRow(
                    topReference = trackerProtectionInfoRef,
                    endReference = moreButtonRef,
                    isExpanded = isExpanded.value
                )
            },
            icon = uiModel.encryptionPadlock,
            text = uiModel.encryptionInfo
        )

        HideDetails(
            modifier = modifier
                .constrainAs(hideDetailsRef) {
                    top.linkTo(
                        encryptionInfoRef.bottom,
                        margin = ProtonDimens.SmallSpacing,
                        goneMargin = ProtonDimens.SmallSpacing
                    )
                    start.linkTo(avatarRef.end, margin = ProtonDimens.SmallSpacing)
                    visibility = if (isExpanded.value) Visibility.Visible else Visibility.Gone
                }
                .clickable { isExpanded.value = !isExpanded.value }
        )
    }
}

@Composable
private fun Sender(
    modifier: Modifier = Modifier,
    participantUiModel: ParticipantUiModel
) {
    Column(modifier = modifier) {
        Text(
            text = participantUiModel.participantName,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.defaultSmallStrong
        )
        Spacer(modifier = Modifier.height(ProtonDimens.ExtraSmallSpacing))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmallNonClickableIcon(iconId = participantUiModel.participantPadlock)
            ParticipantText(text = participantUiModel.participantAddress)
        }
    }
}

@Composable
private fun Icons(
    modifier: Modifier = Modifier,
    uiModel: MessageDetailHeaderUiModel,
    isExpanded: Boolean
) {
    Row(modifier = modifier) {
        if (uiModel.shouldShowTrackerProtectionIcon && !isExpanded) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_shield)
        }
        if (uiModel.shouldShowAttachmentIcon) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_paper_clip)
        }
        if (uiModel.shouldShowStar) {
            SmallNonClickableIcon(iconId = R.drawable.ic_proton_star_filled, tintId = R.color.notification_warning)
        }
        if (!isExpanded) {
            SmallNonClickableIcon(iconId = uiModel.locationIcon)
        }
    }
}

@Composable
private fun Time(
    modifier: Modifier = Modifier,
    time: TextUiModel
) {
    Text(
        modifier = modifier,
        text = time.string(),
        maxLines = 1,
        style = ProtonTheme.typography.caption
    )
}

@Composable
private fun MoreButton(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier.clickable(
            onClickLabel = stringResource(id = R.string.more_button_content_description),
            role = Role.Button,
            onClick = {}
        ),
        painter = painterResource(id = R.drawable.ic_proton_three_dots_horizontal),
        contentDescription = NO_CONTENT_DESCRIPTION
    )
}

@Composable
private fun AllRecipients(
    modifier: Modifier = Modifier,
    allRecipients: String
) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier.padding(end = ProtonDimens.ExtraSmallSpacing),
            text = stringResource(R.string.to),
            style = ProtonTheme.typography.caption
        )
        Text(
            text = allRecipients,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.captionWeak
        )
    }
}

@Composable
private fun Recipients(
    modifier: Modifier = Modifier,
    recipients: List<ParticipantUiModel>
) {
    Column(modifier = modifier) {
        recipients.forEachIndexed { index, participant ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantText(text = participant.participantName, textColor = ProtonTheme.colors.textNorm)
                SmallNonClickableIcon(iconId = participant.participantPadlock)
                ParticipantText(text = participant.participantAddress)
            }
            if (index != recipients.size - 1) {
                Spacer(modifier = Modifier.height(ProtonDimens.SmallSpacing))
            }
        }
    }
}

@Composable
private fun RecipientsTitle(
    modifier: Modifier = Modifier,
    @StringRes recipientsTitle: Int
) {
    Text(modifier = modifier, text = stringResource(id = recipientsTitle), style = ProtonTheme.typography.caption)
}

@Composable
private fun ParticipantText(
    text: String,
    textColor: Color = ProtonTheme.colors.interactionNorm
) {
    Text(
        text = text,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = ProtonTheme.typography.caption
    )
}

@Composable
private fun ExtendedHeaderRow(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    text: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallNonClickableIcon(iconId = icon)
        Spacer(modifier = Modifier.width(ProtonDimens.DefaultSpacing))
        Text(text = text, style = ProtonTheme.typography.captionWeak)
    }
}

@Composable
private fun HideDetails(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.hide_details),
        color = ProtonTheme.colors.interactionNorm,
        style = ProtonTheme.typography.defaultSmall
    )
}

private fun ConstrainScope.constrainRecipientsTitle(
    reference: ConstrainedLayoutReference,
    recipients: List<ParticipantUiModel>,
    isExpanded: Boolean
) {
    top.linkTo(reference.top)
    end.linkTo(reference.start, margin = ProtonDimens.DefaultSpacing)
    visibility = if (recipients.isNotEmpty() && isExpanded) Visibility.Visible else Visibility.Gone
}

private fun ConstrainScope.constrainRecipients(
    topReference: ConstrainedLayoutReference,
    startReference: ConstrainedLayoutReference,
    endReference: ConstrainedLayoutReference,
    recipients: List<ParticipantUiModel>,
    isExpanded: Boolean
) {
    width = Dimension.fillToConstraints
    top.linkTo(
        topReference.bottom,
        margin = ProtonDimens.SmallSpacing,
        goneMargin = ProtonDimens.SmallSpacing
    )
    start.linkTo(startReference.end, margin = ProtonDimens.SmallSpacing)
    end.linkTo(endReference.start, margin = ProtonDimens.SmallSpacing)
    visibility = if (recipients.isNotEmpty() && isExpanded) Visibility.Visible else Visibility.Gone
}

private fun ConstrainScope.constrainExtendedHeaderRow(
    topReference: ConstrainedLayoutReference,
    endReference: ConstrainedLayoutReference,
    isExpanded: Boolean
) {
    width = Dimension.fillToConstraints
    top.linkTo(
        topReference.bottom,
        margin = ProtonDimens.SmallSpacing,
        goneMargin = ProtonDimens.SmallSpacing
    )
    start.linkTo(parent.start, margin = ProtonDimens.MediumSpacing)
    end.linkTo(endReference.start, margin = ProtonDimens.SmallSpacing)
    visibility = if (isExpanded) Visibility.Visible else Visibility.Gone
}

@Preview(showBackground = true)
@Composable
fun MessageDetailHeaderPreview() {
    ProtonTheme {
        MessageDetailHeader(
            uiModel = MessageDetailHeaderPreviewData.MessageHeader
        )
    }
}
