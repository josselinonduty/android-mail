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
package ch.protonmail.android.uitest.robot.contacts

import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * [AddContactRobot] class contains actions and verifications for Add/Edit Contacts.
 */
@Suppress("unused", "ExpressionBodySyntax")
class AddContactRobot(
    private val composeTestRule: ComposeContentTestRule
) {

    fun setNameEmailAndSave(name: String, email: String): ContactsRobot {
        return displayName(name)
            .email(email)
            .saveNewContact()
    }

    fun editNameEmailAndSave(name: String, email: String): ContactDetailsRobot {
        displayName(name)
            .email(email)
            .saveEditContact()
        return ContactDetailsRobot(composeTestRule)
    }

    private fun displayName(name: String): AddContactRobot {
        return this
    }

    private fun email(email: String): AddContactRobot {
        return this
    }

    private fun saveNewContact(): ContactsRobot {
        return ContactsRobot(composeTestRule)
    }

    private fun saveEditContact(): ContactDetailsRobot {
        return ContactDetailsRobot(composeTestRule)
    }
}
