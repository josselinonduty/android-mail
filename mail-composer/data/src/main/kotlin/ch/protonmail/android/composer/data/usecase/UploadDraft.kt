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

package ch.protonmail.android.composer.data.usecase

import arrow.core.Either
import arrow.core.continuations.either
import ch.protonmail.android.composer.data.remote.DraftRemoteDataSource
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.repository.DraftStateRepository
import ch.protonmail.android.mailcomposer.domain.usecase.IsDraftKnownToApi
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageWithBody
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

internal class UploadDraft @Inject constructor(
    private val messageRepository: MessageRepository,
    private val draftStateRepository: DraftStateRepository,
    private val draftRemoteDataSource: DraftRemoteDataSource,
    private val isDraftKnownToApi: IsDraftKnownToApi
) {

    suspend operator fun invoke(userId: UserId, messageId: MessageId): Either<DataError, Unit> = either {
        Timber.d("Draft: Uploading draft for $messageId")

        val draftState = draftStateRepository.observe(userId, messageId).first().onLeft {
            Timber.w("Sync draft failure $messageId: No draft state found")
        }.bind()

        val message = findLocalMessageWithBody(userId, messageId, draftState.apiMessageId)
        if (message == null) {
            Timber.w("Sync draft failure $messageId: No message found")
            shift<MessageWithBody>(DataError.Local.NoDataCached)
            // Return for the compiler's sake (message optionality). shift is causing a left to be returned just above
            return@either
        }

        if (isDraftKnownToApi(draftState)) {
            draftRemoteDataSource.update(userId, message).onRight {
                draftStateRepository.saveSyncedState(userId, it.message.messageId, it.message.messageId)
            }.onLeft {
                Timber.w("Sync draft failure $messageId: Update API call error $it")
            }.bind()
        } else {
            draftRemoteDataSource.create(userId, message, draftState.action).onRight {
                messageRepository.updateDraftMessageId(userId, messageId, it.message.messageId)
                draftStateRepository.saveSyncedState(userId, messageId, it.message.messageId)
            }.onLeft {
                if (it.shouldLogToSentry()) { Timber.w("Sync draft failure $messageId: Create API call error $it") }
                Timber.d("Sync draft error $messageId: Create API call error $it")
            }.bind()

        }
    }

    private suspend fun findLocalMessageWithBody(
        userId: UserId,
        messageId: MessageId,
        apiMessageId: MessageId?
    ): MessageWithBody? {
        messageRepository.getLocalMessageWithBody(userId, messageId)?.let { messageFoundByMessageId ->
            return messageFoundByMessageId
        }
        apiMessageId?.let {
            messageRepository.getLocalMessageWithBody(userId, apiMessageId)?.let { messageFoundByApiMessageId ->
                return messageFoundByApiMessageId
            }
        }
        return null
    }

    private fun DataError.Remote.shouldLogToSentry() = this != DataError.Remote.CreateDraftRequestNotPerformed

}
