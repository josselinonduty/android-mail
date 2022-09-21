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

package ch.protonmail.android.mailmailbox.domain.usecase

import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.mailmailbox.domain.mapper.ConversationMailboxItemMapper
import ch.protonmail.android.mailmailbox.domain.mapper.MessageMailboxItemMapper
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType.Conversation
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType.Message
import ch.protonmail.android.mailmailbox.domain.model.MailboxPageKey
import ch.protonmail.android.mailmessage.domain.entity.Recipient
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailpagination.domain.entity.OrderDirection
import ch.protonmail.android.mailpagination.domain.entity.PageKey
import ch.protonmail.android.testdata.conversation.ConversationWithContextTestData
import ch.protonmail.android.testdata.label.LabelTestData.buildLabel
import ch.protonmail.android.testdata.mailbox.MailboxTestData.buildMailboxItem
import ch.protonmail.android.testdata.message.MessageTestData.buildMessage
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import ch.protonmail.android.testdata.user.UserIdTestData.userId1
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.LabelType.MessageLabel
import me.proton.core.label.domain.repository.LabelRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMultiUserMailboxItemsTest {

    private val messageRepository = mockk<MessageRepository> {
        coEvery { this@mockk.getMessages(userId, any()) } returns listOf(
            // userId
            buildMessage(userId, "1", time = 1000, labelIds = emptyList()),
            buildMessage(userId, "2", time = 2000, labelIds = listOf("4")),
            buildMessage(userId, "3", time = 3000, labelIds = listOf("0", "1"))
        )
        coEvery { this@mockk.getMessages(userId1, any()) } returns listOf(
            // userId
            buildMessage(userId1, "1", time = 1000, labelIds = emptyList()),
            buildMessage(userId1, "2", time = 2000, labelIds = listOf("4")),
            buildMessage(userId1, "3", time = 3000, labelIds = listOf("0", "1"))
        )
    }
    private val conversationRepository = mockk<ConversationRepository> {
        coEvery { getConversations(userId, any()) } returns listOf(
            // userId
            ConversationWithContextTestData.conversation1Labeled,
            ConversationWithContextTestData.conversation2Labeled,
            ConversationWithContextTestData.conversation3Labeled
        )
        coEvery { getConversations(userId1, any()) } returns listOf(
            // userId
            ConversationWithContextTestData.User2.conversation1Labeled,
            ConversationWithContextTestData.User2.conversation2Labeled,
            ConversationWithContextTestData.User2.conversation3Labeled
        )
    }
    private val labelRepository = mockk<LabelRepository> {
        coEvery { this@mockk.getLabels(userId, any()) } returns listOf(
            buildLabel(userId, MessageLabel, "0"),
            buildLabel(userId, MessageLabel, "1"),
            buildLabel(userId, MessageLabel, "2"),
            buildLabel(userId, MessageLabel, "3"),
            buildLabel(userId, MessageLabel, "4")
        )
        coEvery { this@mockk.getLabels(userId1, any()) } returns listOf(
            buildLabel(userId1, MessageLabel, "0"),
            buildLabel(userId1, MessageLabel, "1"),
            buildLabel(userId1, MessageLabel, "2"),
            buildLabel(userId1, MessageLabel, "3"),
            buildLabel(userId1, MessageLabel, "4")
        )
    }

    private val messageMailboxItemMapper = MessageMailboxItemMapper()
    private val conversationMailboxItemMapper = ConversationMailboxItemMapper()

    private lateinit var usecase: GetMultiUserMailboxItems

    @Before
    fun setUp() {
        usecase = GetMultiUserMailboxItems(
            GetMailboxItems(
                labelRepository,
                messageRepository,
                conversationRepository,
                messageMailboxItemMapper,
                conversationMailboxItemMapper
            )
        )
    }

    @Test
    fun `invoke for Message, getLabels and getMessages`() = runTest {
        // Given
        val pageKey = PageKey(orderDirection = OrderDirection.Ascending, size = 6)
        val mailboxPageKey = MailboxPageKey(listOf(userId, userId1), pageKey)

        // When
        val mailboxItems = usecase.invoke(Message, mailboxPageKey)

        // Then
        coVerify { labelRepository.getLabels(userId, MessageLabel) }
        coVerify { labelRepository.getLabels(userId1, MessageLabel) }
        coVerify { labelRepository.getLabels(userId, LabelType.MessageFolder) }
        coVerify { labelRepository.getLabels(userId1, LabelType.MessageFolder) }
        coVerify { messageRepository.getMessages(userId, pageKey) }
        coVerify { messageRepository.getMessages(userId1, pageKey) }
        val senders = listOf(Recipient("address", "name"))
        val mailboxItemsOrderedByTimeAscending = listOf(
            buildMailboxItem(userId, "1", time = 1000, labelIds = emptyList(), type = Message, senders = senders),
            buildMailboxItem(userId1, "1", time = 1000, labelIds = emptyList(), type = Message, senders = senders),
            buildMailboxItem(
                userId,
                "2",
                time = 2000,
                labelIds = listOf(LabelId("4")),
                type = Message,
                senders = senders
            ),
            buildMailboxItem(
                userId1,
                "2",
                time = 2000,
                labelIds = listOf(LabelId("4")),
                type = Message,
                senders = senders
            ),
            buildMailboxItem(
                userId,
                "3",
                time = 3000,
                labelIds = listOf(LabelId("0"), LabelId("1")),
                type = Message,
                senders = senders
            ),
            buildMailboxItem(
                userId1,
                "3",
                time = 3000,
                labelIds = listOf(LabelId("0"), LabelId("1")),
                type = Message,
                senders = senders
            )
        )
        assertEquals(expected = mailboxItemsOrderedByTimeAscending, actual = mailboxItems)
    }

    @Test
    fun `invoke for Conversation, getLabels and getConversations`() = runTest {
        // Given
        val pageKey = PageKey(orderDirection = OrderDirection.Ascending, size = 6)
        val mailboxPageKey = MailboxPageKey(listOf(userId, userId1), pageKey)

        // When
        val mailboxItems = usecase.invoke(Conversation, mailboxPageKey)

        // Then
        coVerify { labelRepository.getLabels(userId, MessageLabel) }
        coVerify { labelRepository.getLabels(userId1, MessageLabel) }
        coVerify { labelRepository.getLabels(userId, LabelType.MessageFolder) }
        coVerify { labelRepository.getLabels(userId1, LabelType.MessageFolder) }
        coVerify { conversationRepository.getConversations(userId, pageKey) }
        coVerify { conversationRepository.getConversations(userId1, pageKey) }
        val mailboxItemsOrderedByTimeAscending = listOf(
            buildMailboxItem(
                userId,
                "1",
                time = 1000,
                labelIds = listOf(LabelId("0")),
                type = Conversation,
                hasAttachments = true
            ),
            buildMailboxItem(
                userId1,
                "1",
                time = 1000,
                labelIds = listOf(LabelId("0")),
                type = Conversation,
                hasAttachments = true
            ),
            buildMailboxItem(
                userId,
                "2",
                time = 2000,
                labelIds = listOf(LabelId("4")),
                type = Conversation,
                hasAttachments = true
            ),
            buildMailboxItem(
                userId1,
                "2",
                time = 2000,
                labelIds = listOf(LabelId("4")),
                type = Conversation,
                hasAttachments = true
            ),
            buildMailboxItem(
                userId,
                "3",
                time = 3000,
                labelIds = listOf(LabelId("0"), LabelId("1")),
                type = Conversation,
                hasAttachments = true
            ),
            buildMailboxItem(
                userId1,
                "3",
                time = 3000,
                labelIds = listOf(LabelId("0"), LabelId("1")),
                type = Conversation,
                hasAttachments = true
            )
        )
        assertEquals(expected = mailboxItemsOrderedByTimeAscending, actual = mailboxItems)
    }
}
