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

package ch.protonmail.android.mailcontact.presentation.model

import ch.protonmail.android.mailcontact.domain.model.DecryptedContact

fun DecryptedContact.toContactFormUiModel(): ContactFormUiModel {
    return ContactFormUiModel(
        id = this.id,
        displayName = this.formattedName?.value ?: "",
        firstName = this.structuredName?.given ?: "",
        lastName = this.structuredName?.family ?: "",
        emails = this.emails.map {
            InputField.SingleTyped(
                value = it.value,
                selectedType = FieldType.EmailType.valueOf(it.type.name),
            )
        },
        phones = this.telephones.map {
            InputField.SingleTyped(
                value = it.text,
                selectedType = FieldType.PhoneType.valueOf(it.type.name),
            )
        },
        addresses = this.addresses.map {
            InputField.Address(
                streetAddress = it.streetAddress,
                postalCode = it.postalCode,
                locality = it.locality,
                region = it.region,
                country = it.country,
                selectedType = FieldType.AddressType.valueOf(it.type.name),
            )
        },
        birthday = this.birthday?.let {
            InputField.Date(value = it.date)
        },
        notes = this.notes.map {
            InputField.Note(
                value = it.value
            )
        }
    )
}
