/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailmailbox.domain.model

import ch.protonmail.android.mailpagination.domain.entity.PageItem
import ch.protonmail.android.mailmessage.domain.entity.Message
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId

enum class MailboxItemType {
    Message,
    Conversation
}

data class MailboxItem(
    val type: MailboxItemType,
    override val id: String,
    override val userId: UserId,
    override val time: Long,
    override val size: Long,
    override val order: Long,
    override val read: Boolean,
    override val keywords: String,
    val labels: List<Label> = emptyList(),
    val subject: String,
    val sender: String,
) : PageItem {
    override val labelIds: List<LabelId> = labels.map { it.labelId }
}

fun Message.toMailboxItem(labels: Map<LabelId, Label>) = MailboxItem(
    type = MailboxItemType.Message,
    id = messageId.id,
    userId = userId,
    sender = sender.name,
    subject = subject,
    labels = labelIds.mapNotNull { labels[it] },
    time = time,
    size = size,
    order = order,
    read = !unread,
    keywords = keywords
)
