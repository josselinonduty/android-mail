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

package ch.protonmail.android.maildetail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.isOfflineError
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailcommon.presentation.model.BottomBarEvent
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailcontact.domain.usecase.ObserveContacts
import ch.protonmail.android.mailconversation.domain.usecase.ObserveConversation
import ch.protonmail.android.maildetail.domain.model.DecryptedMessageBody
import ch.protonmail.android.maildetail.domain.model.GetDecryptedMessageBodyError
import ch.protonmail.android.maildetail.domain.model.LabelSelectionList
import ch.protonmail.android.maildetail.domain.model.MessageWithLabels
import ch.protonmail.android.maildetail.domain.usecase.GetDecryptedMessageBody
import ch.protonmail.android.maildetail.domain.usecase.MarkConversationAsUnread
import ch.protonmail.android.maildetail.domain.usecase.MarkMessageAndConversationReadIfAllMessagesRead
import ch.protonmail.android.maildetail.domain.usecase.MoveConversation
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationDetailActions
import ch.protonmail.android.maildetail.domain.usecase.ObserveConversationMessagesWithLabels
import ch.protonmail.android.maildetail.domain.usecase.ObserveMessageWithLabels
import ch.protonmail.android.maildetail.domain.usecase.RelabelConversation
import ch.protonmail.android.maildetail.domain.usecase.StarConversation
import ch.protonmail.android.maildetail.domain.usecase.UnStarConversation
import ch.protonmail.android.maildetail.presentation.mapper.ActionUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMessageUiModelMapper
import ch.protonmail.android.maildetail.presentation.mapper.ConversationDetailMetadataUiModelMapper
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.CollapseDecryptedMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailEvent.ExpandDecryptedMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailOperation
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.CollapseMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.DismissBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.ExpandMessage
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.LabelAsConfirmed
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.LabelAsToggleAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MarkUnread
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MessageBodyLinkClicked
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToDestinationConfirmed
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.MoveToDestinationSelected
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestLabelAsBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestMoveToBottomSheet
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.RequestScrollTo
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Star
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.Trash
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction.UnStar
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.LabelAsBottomSheetState
import ch.protonmail.android.maildetail.presentation.model.MoveToBottomSheetState
import ch.protonmail.android.maildetail.presentation.reducer.ConversationDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveCustomMailLabels
import ch.protonmail.android.maillabel.domain.usecase.ObserveExclusiveDestinationMailLabels
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.toCustomUiModel
import ch.protonmail.android.maillabel.presentation.toUiModels
import ch.protonmail.android.mailmessage.domain.entity.MessageId
import ch.protonmail.android.mailsettings.domain.usecase.ObserveFolderColorSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
class ConversationDetailViewModel @Inject constructor(
    observePrimaryUserId: ObservePrimaryUserId,
    private val actionUiModelMapper: ActionUiModelMapper,
    private val conversationMessageMapper: ConversationDetailMessageUiModelMapper,
    private val conversationMetadataMapper: ConversationDetailMetadataUiModelMapper,
    private val markConversationAsUnread: MarkConversationAsUnread,
    private val moveConversation: MoveConversation,
    private val relabelConversation: RelabelConversation,
    private val observeContacts: ObserveContacts,
    private val observeConversation: ObserveConversation,
    private val observeConversationMessages: ObserveConversationMessagesWithLabels,
    private val observeDetailActions: ObserveConversationDetailActions,
    private val observeDestinationMailLabels: ObserveExclusiveDestinationMailLabels,
    private val observeFolderColor: ObserveFolderColorSettings,
    private val observeCustomMailLabels: ObserveCustomMailLabels,
    private val reducer: ConversationDetailReducer,
    private val starConversation: StarConversation,
    private val unStarConversation: UnStarConversation,
    private val savedStateHandle: SavedStateHandle,
    private val getDecryptedMessageBody: GetDecryptedMessageBody,
    private val observeMessageWithLabels: ObserveMessageWithLabels,
    private val getContacts: GetContacts,
    private val markMessageAndConversationReadIfAllMessagesRead: MarkMessageAndConversationReadIfAllMessagesRead,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val primaryUserId: Flow<UserId> = observePrimaryUserId().filterNotNull()
    private val mutableDetailState = MutableStateFlow(initialState)
    private val conversationId = requireConversationId()

    val state: StateFlow<ConversationDetailState> = mutableDetailState.asStateFlow()

    init {
        Timber.d("Open detail screen for conversation ID: $conversationId")
        observeConversationMetadata(conversationId)
        observeConversationMessages(conversationId)
        observeBottomBarActions(conversationId)
    }

    @OptIn(FlowPreview::class)
    @Suppress("ComplexMethod")
    fun submit(action: ConversationDetailViewAction) {
        when (action) {
            is Star -> starConversation()
            is UnStar -> unStarConversation()
            is MarkUnread -> markAsUnread()
            is Trash -> moveConversationToTrash()
            is DismissBottomSheet -> dismissBottomSheet(action)
            is RequestMoveToBottomSheet -> showMoveToBottomSheetAndLoadData(action)
            is MoveToDestinationSelected -> moveToDestinationSelected(action.mailLabelId)
            is MoveToDestinationConfirmed ->
                onBottomSheetDestinationConfirmed(action.mailLabelText)

            is RequestLabelAsBottomSheet -> showLabelAsBottomSheetAndLoadData(action)
            is LabelAsToggleAction -> onLabelToggled(action.labelId)
            is LabelAsConfirmed -> onLabelAsConfirmed(action.archiveSelected)
            is ExpandMessage -> onDecryptAndExpandMessage(action.messageId)
            is CollapseMessage -> onCollapseMessage(action.messageId)
            is MessageBodyLinkClicked -> onMessageBodyLinkClicked(action)
            is RequestScrollTo -> onRequestScrollTo(action)
            is ConversationDetailViewAction.ShowAllAttachmentsForMessage ->
                showAllAttachmentsForMessage(action.messageId)
        }
    }

    private fun observeConversationMetadata(conversationId: ConversationId) {
        primaryUserId.flatMapLatest { userId ->
            observeConversation(userId, conversationId, refreshData = false)
                .mapLatest { either ->
                    either.fold(
                        ifLeft = {
                            if (it.isOfflineError()) {
                                ConversationDetailEvent.NoNetworkError
                            } else {
                                ConversationDetailEvent.ErrorLoadingConversation
                            }
                        },
                        ifRight = { ConversationDetailEvent.ConversationData(conversationMetadataMapper.toUiModel(it)) }
                    )
                }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun observeConversationMessages(conversationId: ConversationId) {
        primaryUserId
            .flatMapLatest { userId ->
                combine(
                    observeContacts(userId),
                    observeConversationMessages(userId, conversationId).ignoreLocalErrors()
                ) { contactsEither, messagesEither ->
                    val contacts = contactsEither.getOrElse {
                        Timber.i("Failed getting contacts for displaying initials. Fallback to display name")
                        emptyList()
                    }
                    val messages = messagesEither.getOrElse {
                        return@combine ConversationDetailEvent.ErrorLoadingMessages
                    }
                    val (messagesUiModels, expandedMessageId) = buildMessagesUiModels(messages, contacts)
                    ConversationDetailEvent.MessagesData(messagesUiModels, expandedMessageId)
                }
            }
            .onEach(::emitNewStateFrom)
            .launchIn(viewModelScope)
    }

    private suspend fun buildMessagesUiModels(
        messages: NonEmptyList<MessageWithLabels>,
        contacts: List<Contact>
    ): Pair<NonEmptyList<ConversationDetailMessageUiModel>, MessageId?> {
        return if (state.value.messagesState == ConversationDetailsMessagesState.Loading) {
            buildInitialListWithExpandedMessage(messages, contacts)
        } else {
            buildListFromCurrentState(messages, contacts)
        }
    }

    private suspend fun buildInitialListWithExpandedMessage(
        messages: NonEmptyList<MessageWithLabels>,
        contacts: List<Contact>
    ): Pair<NonEmptyList<ConversationDetailMessageUiModel>, MessageId?> {
        val newestNonDraftMessageId = messages
            .filterNot { it.message.isDraft() }
            .maxByOrNull { it.message.time }
            ?.message
            ?.messageId

        val conversationWithOnlyDrafts = newestNonDraftMessageId == null

        val messagesList = if (conversationWithOnlyDrafts) {
            messages.map { messageWithLabels -> buildCollapsedMessage(messageWithLabels, contacts) }
        } else {
            messages.map { messageWithLabels ->
                val userId = primaryUserId.first()
                if (messageWithLabels.message.messageId == newestNonDraftMessageId) {
                    val decryptedBody = decryptMessageBody(userId, messageWithLabels.message.messageId)
                    if (decryptedBody == null) {
                        buildCollapsedMessage(messageWithLabels, contacts)
                    } else {
                        buildExpandedMessage(
                            messageWithLabels,
                            contacts,
                            decryptedBody
                        )
                    }
                } else {
                    buildCollapsedMessage(messageWithLabels, contacts)
                }
            }
        }

        return Pair(messagesList, newestNonDraftMessageId)
    }

    private fun buildListFromCurrentState(
        messages: NonEmptyList<MessageWithLabels>,
        contacts: List<Contact>
    ): Pair<NonEmptyList<ConversationDetailMessageUiModel>, MessageId?> {
        val messagesList = messages.map { messageWithLabels ->
            when (val currentMessage = findCurrentStateOfMessage(messageWithLabels.message.messageId)) {
                is ConversationDetailMessageUiModel.Expanded -> currentMessage
                else -> buildCollapsedMessage(messageWithLabels, contacts)
            }
        }

        return Pair(messagesList, null)
    }

    private fun buildCollapsedMessage(
        messageWithLabels: MessageWithLabels,
        contacts: List<Contact>
    ): ConversationDetailMessageUiModel.Collapsed = conversationMessageMapper.toUiModel(
        messageWithLabels,
        contacts
    )

    private fun buildExpandedMessage(
        messageWithLabels: MessageWithLabels,
        contacts: List<Contact>,
        decryptedBody: DecryptedMessageBody
    ): ConversationDetailMessageUiModel.Expanded = conversationMessageMapper.toUiModel(
        messageWithLabels,
        contacts,
        decryptedBody,
    )

    private fun findCurrentStateOfMessage(messageId: MessageId): ConversationDetailMessageUiModel? {
        val messagesState = (state.value as? ConversationDetailState)
            ?.messagesState as? ConversationDetailsMessagesState.Data

        return messagesState?.messages?.firstOrNull { it.messageId == messageId }
    }

    private fun observeBottomBarActions(conversationId: ConversationId) {
        primaryUserId.flatMapLatest { userId ->
            observeDetailActions(userId, conversationId, refreshConversations = false).mapLatest { either ->
                either.fold(
                    ifLeft = { ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.ErrorLoadingActions) },
                    ifRight = { actions ->
                        val actionUiModels = actions.map { actionUiModelMapper.toUiModel(it) }
                        ConversationDetailEvent.ConversationBottomBarEvent(BottomBarEvent.ActionsData(actionUiModels))
                    }
                )
            }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun showMoveToBottomSheetAndLoadData(initialEvent: ConversationDetailViewAction) {
        primaryUserId.flatMapLatest { userId ->
            combine(
                observeDestinationMailLabels(userId),
                observeFolderColor(userId)
            ) { folders, color ->
                ConversationDetailEvent.ConversationBottomSheetEvent(
                    MoveToBottomSheetState.MoveToBottomSheetEvent.ActionData(
                        folders.toUiModels(color).let { it.folders + it.systems }
                    )
                )
            }
        }.onStart {
            emitNewStateFrom(initialEvent)
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun showLabelAsBottomSheetAndLoadData(initialEvent: ConversationDetailViewAction) {
        viewModelScope.launch {
            emitNewStateFrom(initialEvent)

            val userId = primaryUserId.first()
            val labels = observeCustomMailLabels(userId).first()
            val color = observeFolderColor(userId).first()
            val conversationWithMessagesAndLabels = observeConversationMessages(userId, conversationId).first()

            val mappedLabels = labels.tapLeft {
                Timber.e("Error while observing custom labels")
            }.getOrElse { emptyList() }

            val messagesWithLabels = conversationWithMessagesAndLabels.tapLeft {
                Timber.e("Error while observing conversation messages")
            }.getOrElse { emptyList() }

            val (selectedLabels, partiallySelectedLabels) = mappedLabels.getLabelSelectionState(messagesWithLabels)

            val event = ConversationDetailEvent.ConversationBottomSheetEvent(
                LabelAsBottomSheetState.LabelAsBottomSheetEvent.ActionData(
                    customLabelList = mappedLabels.map { it.toCustomUiModel(color, emptyMap(), null) },
                    selectedLabels = selectedLabels,
                    partiallySelectedLabels = partiallySelectedLabels
                )
            )
            emitNewStateFrom(event)
        }
    }

    private fun onLabelToggled(labelId: LabelId) {
        viewModelScope.launch { emitNewStateFrom(LabelAsToggleAction(labelId)) }
    }

    private fun onLabelAsConfirmed(archiveSelected: Boolean) {
        viewModelScope.launch {
            val userId = primaryUserId.first()
            val labels = observeCustomMailLabels(userId).first().tapLeft {
                Timber.e("Error while observing custom labels when relabeling got confirmed: $it")
            }.getOrElse { emptyList() }
            val messagesWithLabels = observeConversationMessages(userId, conversationId).first().tapLeft {
                Timber.e("Error while observing conversation message when relabeling got confirmed: $it")
            }.getOrElse { emptyList() }

            val previousSelection = labels.getLabelSelectionState(messagesWithLabels)

            val labelAsData = mutableDetailState.value.bottomSheetState?.contentState as? LabelAsBottomSheetState.Data
                ?: throw IllegalStateException("BottomSheetState is not LabelAsBottomSheetState.Data")

            val updatedSelections = labelAsData.getLabelSelectionState()

            if (archiveSelected) {
                moveConversation(
                    userId = userId,
                    conversationId = conversationId,
                    labelId = SystemLabelId.Archive.labelId
                ).tapLeft { Timber.e("Error while archiving conversation when relabeling got confirmed: $it") }
            }
            val operation = relabelConversation(
                userId = userId,
                conversationId = conversationId,
                currentSelections = previousSelection,
                updatedSelections = updatedSelections
            ).fold(
                ifLeft = {
                    Timber.e("Error while relabeling conversation: $it")
                    ConversationDetailEvent.ErrorLabelingConversation
                },
                ifRight = { LabelAsConfirmed(archiveSelected) }
            )
            emitNewStateFrom(operation)
        }
    }

    private fun List<MailLabel.Custom>.getLabelSelectionState(messages: List<MessageWithLabels>): LabelSelectionList {
        val previousSelectedLabels = mutableListOf<LabelId>()
        val previousPartiallySelectedLabels = mutableListOf<LabelId>()
        this.forEach { label ->
            if (messages.allContainsLabel(label.id.labelId)) {
                previousSelectedLabels.add(label.id.labelId)
            } else if (messages.partiallyContainsLabel(label.id.labelId)) {
                previousPartiallySelectedLabels.add(label.id.labelId)
            }
        }
        return LabelSelectionList(
            selectedLabels = previousSelectedLabels,
            partiallySelectionLabels = previousPartiallySelectedLabels
        )
    }

    private fun LabelAsBottomSheetState.Data.getLabelSelectionState(): LabelSelectionList {
        val selectedLabels = this.labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.Selected }
            .map { it.labelUiModel.id.labelId }

        val partiallySelectedLabels = this.labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.PartiallySelected }
            .map { it.labelUiModel.id.labelId }
        return LabelSelectionList(
            selectedLabels = selectedLabels,
            partiallySelectionLabels = partiallySelectedLabels
        )
    }

    private fun List<MessageWithLabels>.allContainsLabel(labelId: LabelId): Boolean {
        return this.all { messageWithLabel ->
            messageWithLabel.labels.any { it.labelId == labelId }
        }
    }

    private fun List<MessageWithLabels>.partiallyContainsLabel(labelId: LabelId): Boolean {
        return this.any { messageWithLabel ->
            messageWithLabel.labels.any { it.labelId == labelId }
        }
    }

    private fun requireConversationId(): ConversationId {
        val conversationId = savedStateHandle.get<String>(ConversationDetailScreen.ConversationIdKey)
            ?: throw IllegalStateException("No Conversation id given")
        return ConversationId(conversationId)
    }

    private suspend fun emitNewStateFrom(event: ConversationDetailOperation) {
        val newState: ConversationDetailState = reducer.newStateFrom(state.value, event)
        mutableDetailState.emit(newState)
    }

    private fun markAsUnread() {
        primaryUserId.mapLatest { userId ->
            markConversationAsUnread(userId, conversationId).fold(
                ifLeft = { ConversationDetailEvent.ErrorMarkingAsUnread },
                ifRight = { MarkUnread }
            )
        }.onEach(::emitNewStateFrom)
            .launchIn(viewModelScope)
    }

    private fun moveConversationToTrash() {
        primaryUserId.mapLatest { userId ->
            moveConversation(userId, conversationId, SystemLabelId.Trash.labelId).fold(
                ifLeft = { ConversationDetailEvent.ErrorMovingToTrash },
                ifRight = { Trash }
            )
        }.onEach(::emitNewStateFrom)
            .launchIn(viewModelScope)
    }

    private fun starConversation() {
        primaryUserId.mapLatest { userId ->
            starConversation(userId, conversationId).fold(
                ifLeft = { ConversationDetailEvent.ErrorAddStar },
                ifRight = { Star }
            )
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun unStarConversation() {
        Timber.d("UnStar conversation clicked")
        primaryUserId.mapLatest { userId ->
            unStarConversation(userId, conversationId).fold(
                ifLeft = { ConversationDetailEvent.ErrorRemoveStar },
                ifRight = { UnStar }
            )
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    private fun dismissBottomSheet(action: ConversationDetailViewAction) {
        viewModelScope.launch { emitNewStateFrom(action) }
    }

    private fun moveToDestinationSelected(mailLabelId: MailLabelId) {
        viewModelScope.launch {
            emitNewStateFrom(MoveToDestinationSelected(mailLabelId))
        }
    }

    private fun onBottomSheetDestinationConfirmed(mailLabelText: String) {
        primaryUserId.mapLatest { userId ->
            val bottomSheetState = state.value.bottomSheetState?.contentState
            if (bottomSheetState is MoveToBottomSheetState.Data) {
                bottomSheetState.selected?.let { mailLabelUiModel ->
                    moveConversation(userId, conversationId, mailLabelUiModel.id.labelId).fold(
                        ifLeft = { ConversationDetailEvent.ErrorMovingConversation },
                        ifRight = { MoveToDestinationConfirmed(mailLabelText) }
                    )
                } ?: throw IllegalStateException("No destination selected")
            } else {
                ConversationDetailEvent.ErrorMovingConversation
            }
        }.onEach { event ->
            emitNewStateFrom(event)
        }.launchIn(viewModelScope)
    }

    @FlowPreview
    private fun onDecryptAndExpandMessage(messageId: MessageId) {
        findCurrentStateOfMessage(messageId)?.let {
            if (it is ConversationDetailMessageUiModel.Collapsed) onExpandingMessage(it)
        }
        primaryUserId.mapLatest { userId ->
            val contacts = getContacts(userId).getOrElse { emptyList() }
            val decryptedBody = decryptMessageBody(userId, messageId)
            decryptedBody?.let { body -> Triple(userId, body, contacts) }
        }
            .filterNotNull()
            .flatMapConcat { (userId, body, contacts) ->
                observeMessageWithLabels(userId, messageId).map { messageWithLabels ->
                    Triple(messageWithLabels.getOrNull(), body, contacts)
                }
            }
            .onEach { (messageWithLabels, body, contacts) ->
                if (messageWithLabels != null) {
                    onExpandMessage(messageId, messageWithLabels, contacts, body)
                }
            }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    private suspend fun decryptMessageBody(
        userId: UserId,
        messageId: MessageId
    ): DecryptedMessageBody? = getDecryptedMessageBody(userId, messageId)
        .onRight { markMessageAndConversationAsRead(userId, messageId) }
        .onLeft { emitMessageBodyDecryptError(it, messageId) }
        .getOrNull()

    private suspend fun onExpandMessage(
        messageId: MessageId,
        messageWithLabels: MessageWithLabels,
        contacts: List<Contact>,
        messageBody: DecryptedMessageBody
    ) {
        emitNewStateFrom(
            ExpandDecryptedMessage(
                messageId = messageId,
                conversationDetailMessageUiModel = buildExpandedMessage(
                    messageWithLabels,
                    contacts,
                    messageBody
                )
            )
        )
    }

    private fun onCollapseMessage(messageId: MessageId) {
        primaryUserId.flatMapLatest { userId ->
            combine(
                observeContacts(userId),
                observeMessageWithLabels(userId, messageId)
            ) { contactsEither, messageEither ->
                val contacts = contactsEither.getOrElse { emptyList() }
                val message = messageEither.getOrNull()

                message?.let { Pair(it, contacts) }
            }
        }
            .filterNotNull()
            .onEach { (message, contacts) ->
                emitNewStateFrom(
                    CollapseDecryptedMessage(
                        messageId = messageId,
                        conversationDetailMessageUiModel = buildCollapsedMessage(message, contacts)
                    )
                )
            }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    private suspend fun markMessageAndConversationAsRead(userId: UserId, messageId: MessageId) {
        viewModelScope.launch(ioDispatcher) {
            markMessageAndConversationReadIfAllMessagesRead(userId, messageId, conversationId)
        }
    }

    private fun onMessageBodyLinkClicked(action: MessageBodyLinkClicked) {
        viewModelScope.launch { emitNewStateFrom(action) }
    }

    private fun onRequestScrollTo(action: RequestScrollTo) {
        viewModelScope.launch { emitNewStateFrom(action) }
    }

    private suspend fun emitMessageBodyDecryptError(
        error: GetDecryptedMessageBodyError,
        messageId: MessageId
    ) {
        val errorState = when (error) {
            is GetDecryptedMessageBodyError.Data -> if (error.dataError.isOfflineError()) {
                ConversationDetailEvent.ErrorExpandingRetrievingMessageOffline(messageId)
            } else {
                ConversationDetailEvent.ErrorExpandingRetrieveMessageError(messageId)
            }

            is GetDecryptedMessageBodyError.Decryption ->
                ConversationDetailEvent.ErrorExpandingDecryptMessageError(messageId)
        }

        emitNewStateFrom(errorState)
    }

    private fun onExpandingMessage(collapsed: ConversationDetailMessageUiModel.Collapsed) {
        viewModelScope.launch {
            emitNewStateFrom(ConversationDetailEvent.ExpandingMessage(collapsed.messageId, collapsed))
        }
    }

    private fun showAllAttachmentsForMessage(messageId: MessageId) {
        val dataState = state.value.messagesState as? ConversationDetailsMessagesState.Data
        if (dataState == null) {
            Timber.e("Messages state is not data to perform show all attachments operation")
            return
        }
        dataState.messages.firstOrNull { it.messageId == messageId }
            ?.takeIf { it is ConversationDetailMessageUiModel.Expanded }
            ?.let { it as ConversationDetailMessageUiModel.Expanded }
            ?.let {
                val operation = ConversationDetailEvent.ShowAllAttachmentsForMessage(
                    messageId = messageId,
                    conversationDetailMessageUiModel = it.copy(
                        messageBodyUiModel = it.messageBodyUiModel.copy(
                            attachments = it.messageBodyUiModel.attachments?.copy(
                                limit = it.messageBodyUiModel.attachments.attachments.size
                            )
                        )
                    )
                )
                viewModelScope.launch { emitNewStateFrom(operation) }
            }
    }

    companion object {

        val initialState = ConversationDetailState.Loading
    }
}

/**
 * Filters [DataError.Local] from messages flow, as we don't want to show them to the user, because the fetch is being
 *  done on the conversation flow.
 */
private fun Flow<Either<DataError, NonEmptyList<MessageWithLabels>>>.ignoreLocalErrors():
    Flow<Either<DataError, NonEmptyList<MessageWithLabels>>> = filter { either ->
    either.fold(
        ifLeft = { error -> error !is DataError.Local },
        ifRight = { true }
    )
}
