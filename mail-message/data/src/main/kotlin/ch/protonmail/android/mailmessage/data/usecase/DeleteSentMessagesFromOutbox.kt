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

package ch.protonmail.android.mailmessage.data.usecase

import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.DeleteSentDraftMessagesStatus
import me.proton.core.domain.entity.UserId
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

/**
 * Delete sent messages from outbox. Currently, we identify outbox messages by the presence of the Sent draft sync state
 * in the DraftSyncEntity table.
 */
class DeleteSentMessagesFromOutbox @Inject constructor(
    private val deleteSentDraftMessagesStatus: DeleteSentDraftMessagesStatus,
    private val scopeProvider: CoroutineScopeProvider
) {

    suspend operator fun invoke(userId: UserId, entities: List<Message>) {

        val sentItemsToClear = entities.filter { message ->
            message.labelIds.contains(SystemLabelId.AllSent.labelId)
        }.map { MessageId(it.id) }

        if (sentItemsToClear.isNotEmpty()) {
            scopeProvider.GlobalIOSupervisedScope.launch {
                // We need to keep Sent messages in Outbox until the next event loop iteration in order to prevent
                // pull to refresh updates overwriting the message label
                delay(EVENT_LOOP_PERIOD_MS)
                for (sentItemId in sentItemsToClear) {
                    deleteSentDraftMessagesStatus(userId, sentItemId)
                }
            }
        }
    }

    companion object {
        const val EVENT_LOOP_PERIOD_MS = 30_000L
    }
}

