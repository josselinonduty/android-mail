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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.usecase.ObserveExclusiveMailLabels
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import javax.inject.Inject

class MoveMessages @Inject constructor(
    private val messageRepository: MessageRepository,
    private val decrementUnreadCount: DecrementUnreadCount,
    private val incrementUnreadCount: IncrementUnreadCount,
    private val observeExclusiveMailLabels: ObserveExclusiveMailLabels
) {

    suspend operator fun invoke(
        userId: UserId,
        messageIds: List<MessageId>,
        labelId: LabelId
    ): Either<DataError.Local, Unit> = either {
        val exclusiveMailLabels = observeExclusiveMailLabels(userId).first().allById.mapKeys { it.key.labelId }
        val messagesWithExclusiveLabels =
            messageRepository.getLocalMessages(userId, messageIds).associateWith {
                it.labelIds.firstOrNull { labelId -> labelId in exclusiveMailLabels }
            }
        updateUnreadMessagesCount(userId, messagesWithExclusiveLabels, labelId)
        val messageIdsWithExclusiveLabels = messagesWithExclusiveLabels.mapKeys { it.key.messageId }
        messageRepository.moveTo(userId, messageIdsWithExclusiveLabels, labelId).bind()
    }

    private suspend fun updateUnreadMessagesCount(
        userId: UserId,
        messagesWithExclusiveLabels: Map<Message, LabelId?>,
        destinationLabel: LabelId
    ) = messagesWithExclusiveLabels.onEach { messageWithLabels ->
        val exclusiveLabelId = messageWithLabels.value
        if (exclusiveLabelId != null && messageWithLabels.key.unread) {
            decrementUnreadCount(userId, exclusiveLabelId)
            incrementUnreadCount(userId, destinationLabel)
        }
    }

}
