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

package ch.protonmail.android.mailmessage.domain.repository

import arrow.core.Either
import arrow.core.Nel
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.entity.Message
import ch.protonmail.android.mailmessage.domain.entity.MessageId
import ch.protonmail.android.mailmessage.domain.entity.MessageWithBody
import ch.protonmail.android.mailpagination.domain.model.PageKey
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId

@Suppress("TooManyFunctions")
interface MessageRepository {

    /**
     * Load all [Message] from local cache for [userId] filtered by [PageKey].
     */
    suspend fun getLocalMessages(userId: UserId, pageKey: PageKey = PageKey()): List<Message>

    /**
     * Return true if all [Message] are considered locally valid according the given [pageKey].
     */
    suspend fun isLocalPageValid(
        userId: UserId,
        pageKey: PageKey,
        items: List<Message>,
    ): Boolean

    /**
     * Fetch all [Message] from network for [userId] filtered by [PageKey].
     */
    suspend fun getRemoteMessages(userId: UserId, pageKey: PageKey): Either<DataError.Remote, List<Message>>

    /**
     * Mark local data as stale for [userId], by [labelId].
     */
    suspend fun markAsStale(userId: UserId, labelId: LabelId)

    /**
     * Gets a [Message] metadata for [userId] from the local storage
     * @return either the [Message] or a [DataError.Local]
     */
    fun observeCachedMessage(userId: UserId, messageId: MessageId): Flow<Either<DataError.Local, Message>>

    /**
     * Get all the [Message]s metadata for a given [ConversationId], for [userId] from the local storage
     */
    fun observeCachedMessages(
        userId: UserId,
        conversationId: ConversationId
    ): Flow<Either<DataError.Local, Nel<Message>>>

    /**
     * Observe the [MessageWithBody] for a given [MessageId], for [userId]
     */
    fun observeMessageWithBody(userId: UserId, messageId: MessageId): Flow<Either<DataError, MessageWithBody>>

    /**
     * Get the [MessageWithBody] for a given [MessageId], for [userId]
     */
    suspend fun getMessageWithBody(userId: UserId, messageId: MessageId): Either<DataError, MessageWithBody>

    /**
     * Get the [MessageWithBody] for a given [MessageId] and [userId] from the local storage.
     */
    suspend fun getLocalMessageWithBody(userId: UserId, messageId: MessageId): MessageWithBody?

    suspend fun upsertMessageWithBody(userId: UserId, messageWithBody: MessageWithBody): Boolean

    /**
     * Moves the given [messageId] from the [fromLabel] to the [toLabel]
     */
    suspend fun moveTo(
        userId: UserId,
        messageId: MessageId,
        fromLabel: LabelId?,
        toLabel: LabelId
    ): Either<DataError.Local, Message>

    /**
     * Set the message with the given [messageId] as unread
     */
    suspend fun markUnread(userId: UserId, messageId: MessageId): Either<DataError.Local, Message>

    /**
     * Set the message with the given [messageId] as read
     */
    suspend fun markRead(userId: UserId, messageId: MessageId): Either<DataError.Local, Message>

    suspend fun isMessageRead(userId: UserId, messageId: MessageId): Either<DataError.Local, Boolean>

    /**
     * Removes [labelsToBeRemoved] and adds [labelsToBeAdded] from the message with the given [messageId]
     */
    suspend fun relabel(
        userId: UserId,
        messageIds: List<MessageId>,
        labelsToBeRemoved: List<LabelId> = emptyList(),
        labelsToBeAdded: List<LabelId> = emptyList()
    ): Either<DataError.Local, List<Message>>
}
