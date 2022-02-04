/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailmessage.data.remote.resource

import ch.protonmail.android.mailconversation.domain.ConversationId
import ch.protonmail.android.mailmessage.domain.entity.AttachmentId
import ch.protonmail.android.mailmessage.domain.entity.MailTo
import ch.protonmail.android.mailmessage.domain.entity.Message
import ch.protonmail.android.mailmessage.domain.entity.MessageAttachment
import ch.protonmail.android.mailmessage.domain.entity.MessageBody
import ch.protonmail.android.mailmessage.domain.entity.MessageId
import ch.protonmail.android.mailmessage.domain.entity.MessageWithBody
import ch.protonmail.android.mailmessage.domain.entity.UnsubscribeMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.util.kotlin.toBooleanOrFalse

@Serializable
data class MessageWithBodyResource(
    @SerialName("ID")
    val id: String,
    @SerialName("Order")
    val order: Long,
    @SerialName("ConversationID")
    val conversationId: String,
    @SerialName("Subject")
    val subject: String,
    @SerialName("Unread")
    val unread: Int,
    @SerialName("Sender")
    val sender: SenderResource,
    @SerialName("ToList")
    val toList: List<RecipientResource>,
    @SerialName("CCList")
    val ccList: List<RecipientResource>,
    @SerialName("BCCList")
    val bccList: List<RecipientResource>,
    @SerialName("Time")
    val time: Long,
    @SerialName("Size")
    val size: Long,
    @SerialName("ExpirationTime")
    val expirationTime: Long,
    @SerialName("IsReplied")
    val isReplied: Int,
    @SerialName("IsRepliedAll")
    val isRepliedAll: Int,
    @SerialName("IsForwarded")
    val isForwarded: Int,
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("LabelIDs")
    val labelIds: List<String>,
    @SerialName("ExternalID")
    val externalId: String?,
    @SerialName("NumAttachments")
    val numAttachments: Int,
    @SerialName("Flags")
    val flags: Int,
    // --- Extended properties ---
    @SerialName("Body")
    val body: String,
    @SerialName("Header")
    val header: String,
    @SerialName("ParsedHeaders")
    val parsedHeaders: Map<String, JsonElement>,
    @SerialName("Attachments")
    val attachments: List<AttachmentResource>,
    @SerialName("MIMEType")
    val mimeType: String,
    @SerialName("SpamScore")
    val spamScore: String,
    @SerialName("ReplyTo")
    val replyTo: SenderResource,
    @SerialName("ReplyTos")
    val replyTos: List<SenderResource>,
    @SerialName("UnsubscribeMethods")
    val unsubscribeMethods: List<UnsubscribeMethodResource>? = null,
) {
    fun toMessageWithBody(userId: UserId) = MessageWithBody(
        message = Message(
            userId = userId,
            messageId = MessageId(id),
            conversationId = ConversationId(conversationId),
            order = order,
            subject = subject,
            unread = unread.toBooleanOrFalse(),
            sender = sender.toSender(),
            toList = toList.map { it.toRecipient() },
            ccList = ccList.map { it.toRecipient() },
            bccList = bccList.map { it.toRecipient() },
            time = time,
            size = size,
            expirationTime = expirationTime,
            isReplied = isReplied.toBooleanOrFalse(),
            isRepliedAll = isRepliedAll.toBooleanOrFalse(),
            isForwarded = isForwarded.toBooleanOrFalse(),
            addressId = AddressId(addressId),
            labelIds = labelIds.map { LabelId(it) },
            externalId = externalId,
            numAttachments = numAttachments,
            flags = flags
        ),
        messageBody = MessageBody(
            userId = userId,
            messageId = MessageId(id),
            body = body,
            header = header,
            parsedHeaders = parsedHeaders,
            attachments = attachments.map { it.toMessageAttachment() },
            mimeType = mimeType,
            spamScore = spamScore,
            replyTo = replyTo.toSender(),
            replyTos = replyTos.map { it.toSender() },
            unsubscribeMethods = unsubscribeMethods?.map { it.toUnsubscribeMethod() }
        )
    )
}

@Serializable
data class AttachmentResource(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String,
    @SerialName("Size")
    val size: Long,
    @SerialName("MIMEType")
    val mimeType: String,
    @SerialName("Disposition")
    val disposition: String? = null,
    @SerialName("KeyPackets")
    val keyPackets: String? = null,
    @SerialName("Signature")
    val signature: String? = null,
    @SerialName("EncSignature")
    val encSignature: String? = null,
    @SerialName("Headers")
    val headers: Map<String, String>,
) {
    fun toMessageAttachment() = MessageAttachment(
        attachmentId = AttachmentId(id),
        name = name,
        size = size,
        mimeType = mimeType,
        disposition = disposition,
        keyPackets = keyPackets,
        signature = signature,
        encSignature = encSignature,
        headers = headers
    )
}

@Serializable
data class UnsubscribeMethodResource(
    @SerialName("HttpClient")
    val httpClient: String?,
    @SerialName("OneClick")
    val oneClick: String?,
    @SerialName("Mailto")
    val mailTo: MailToResource?,
) {
    fun toUnsubscribeMethod() = UnsubscribeMethod(
        httpClient = httpClient,
        oneClick = oneClick,
        mailTo = mailTo?.toMailTo()
    )
}

@Serializable
data class MailToResource(
    @SerialName("ToList")
    val toList: List<String>,
    @SerialName("Subject")
    val subject: String,
    @SerialName("Body")
    val body: String,
) {
    fun toMailTo() = MailTo(
        toList = toList,
        subject = subject,
        body = body
    )
}
