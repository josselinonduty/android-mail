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

package ch.protonmail.android.maildetail.presentation.previewdata

import java.util.UUID
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomBarState
import ch.protonmail.android.mailcommon.presentation.model.contentDescription
import ch.protonmail.android.mailcommon.presentation.model.iconDrawable
import ch.protonmail.android.mailcommon.presentation.sample.TextMessageSample
import ch.protonmail.android.maildetail.presentation.model.BottomSheetState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMetadataState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.MoveToBottomSheetState
import ch.protonmail.android.maildetail.presentation.sample.ConversationDetailMessageUiModelSample
import ch.protonmail.android.mailmessage.domain.model.MessageId

object ConversationDetailsPreviewData {

    val Success = ConversationDetailState(
        ConversationDetailMetadataState.Data(
            conversationUiModel = ConversationDetailsUiModelPreviewData.WeatherForecast
        ),
        messagesState = ConversationDetailsMessagesState.Data(
            messages = listOf(
                ConversationDetailMessageUiModelSample.AugWeatherForecast,
                ConversationDetailMessageUiModelSample.InvoiceRepliedAll
                    .copy(messageId = MessageId(UUID.randomUUID().toString())),
                ConversationDetailMessageUiModelSample.InvoiceForwarded
                    .copy(messageId = MessageId(UUID.randomUUID().toString())),
                ConversationDetailMessageUiModelSample.ExpiringInvitation
            )
        ),
        bottomBarState = BottomBarState.Data.Shown(
            actions = listOf(
                ActionUiModel(Action.Reply, Action.Reply.iconDrawable(), Action.Reply.contentDescription()),
                ActionUiModel(Action.Archive, Action.Archive.iconDrawable(), Action.Archive.contentDescription())
            )
        ),
        bottomSheetState = BottomSheetState(
            MoveToBottomSheetState.Data(
                moveToDestinations = emptyList(),
                selected = null
            )
        ),
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val SuccessWithRandomMessageIds = ConversationDetailState(
        ConversationDetailMetadataState.Data(
            conversationUiModel = ConversationDetailsUiModelPreviewData.WeatherForecast
        ),
        messagesState = ConversationDetailsMessagesState.Data(
            messages = listOf(
                ConversationDetailMessageUiModelSample.AugWeatherForecast.copy(
                    messageId = MessageId(UUID.randomUUID().toString())
                ),
                ConversationDetailMessageUiModelSample.InvoiceRepliedAll.copy(
                    messageId = MessageId(UUID.randomUUID().toString())
                ),
                ConversationDetailMessageUiModelSample.InvoiceForwarded.copy(
                    messageId = MessageId(UUID.randomUUID().toString())
                ),
                ConversationDetailMessageUiModelSample.ExpiringInvitation.copy(
                    messageId = MessageId(UUID.randomUUID().toString())
                )
            )
        ),
        bottomBarState = BottomBarState.Data.Shown(
            actions = listOf(
                ActionUiModel(Action.Reply, Action.Reply.iconDrawable(), Action.Reply.contentDescription()),
                ActionUiModel(Action.Archive, Action.Archive.iconDrawable(), Action.Archive.contentDescription())
            )
        ),
        bottomSheetState = BottomSheetState(
            MoveToBottomSheetState.Data(
                moveToDestinations = emptyList(),
                selected = null
            )
        ),
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val FailedLoadingConversation = ConversationDetailState(
        conversationState = ConversationDetailMetadataState.Error(TextMessageSample.UnknownError),
        messagesState = ConversationDetailsMessagesState.Loading,
        bottomBarState = BottomBarState.Loading,
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        bottomSheetState = BottomSheetState(MoveToBottomSheetState.Loading),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val FailedLoadingMessages = ConversationDetailState(
        conversationState = ConversationDetailMetadataState.Loading,
        messagesState = ConversationDetailsMessagesState.Error(TextMessageSample.NoNetwork),
        bottomBarState = BottomBarState.Loading,
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        bottomSheetState = BottomSheetState(MoveToBottomSheetState.Loading),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val FailedLoadingBottomBar = ConversationDetailState(
        conversationState = ConversationDetailMetadataState.Loading,
        messagesState = ConversationDetailsMessagesState.Loading,
        bottomBarState = BottomBarState.Error.FailedLoadingActions,
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        bottomSheetState = BottomSheetState(MoveToBottomSheetState.Loading),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val Loading = ConversationDetailState(
        conversationState = ConversationDetailMetadataState.Loading,
        messagesState = ConversationDetailsMessagesState.Loading,
        bottomBarState = BottomBarState.Loading,
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        bottomSheetState = BottomSheetState(MoveToBottomSheetState.Loading),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )

    val NotLoggedIn = ConversationDetailState(
        conversationState = ConversationDetailMetadataState.Error(TextMessageSample.NotLoggedIn),
        messagesState = ConversationDetailsMessagesState.Error(TextMessageSample.NotLoggedIn),
        bottomBarState = BottomBarState.Loading,
        exitScreenEffect = Effect.empty(),
        exitScreenWithMessageEffect = Effect.empty(),
        error = Effect.empty(),
        bottomSheetState = BottomSheetState(MoveToBottomSheetState.Loading),
        openMessageBodyLinkEffect = Effect.empty(),
        openAttachmentEffect = Effect.empty(),
        scrollToMessage = null,
        showReplyActionsFeatureFlag = false
    )
}

class ConversationDetailsPreviewProvider : PreviewParameterProvider<ConversationDetailState> {

    override val values = sequenceOf(
        ConversationDetailsPreviewData.Success,
        ConversationDetailsPreviewData.FailedLoadingConversation,
        ConversationDetailsPreviewData.FailedLoadingMessages,
        ConversationDetailsPreviewData.FailedLoadingBottomBar,
        ConversationDetailsPreviewData.Loading,
        ConversationDetailsPreviewData.NotLoggedIn
    )
}
