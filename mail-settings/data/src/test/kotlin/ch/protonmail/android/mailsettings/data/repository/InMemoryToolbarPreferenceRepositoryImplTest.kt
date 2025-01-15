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

package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailsettings.domain.model.SettingsToolbarType
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference.ActionSelection
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference.Defaults
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference.ToolbarActions
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ActionsToolbarSetting
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MobileSettings
import me.proton.core.mailsettings.domain.entity.ToolbarAction
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

internal class InMemoryToolbarPreferenceRepositoryImplTest {

    private val mailSettingsRepository = mockk<MailSettingsRepository>()

    private val accountManager = mockk<AccountManager> {
        every { this@mockk.getPrimaryUserId() } returns flowOf(UserId("test-id"))
    }

    private val repo: InMemoryToolbarPreferenceRepositoryImpl =
        InMemoryToolbarPreferenceRepositoryImpl(mailSettingsRepository, accountManager)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns default actions if user has no preference set`() = runTest {
        // Given
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns null
            every { this@mockk.mobileSettings } returns null
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            // Then
            val expected = expectedDefaultPreference(convMode = false)
            val item = awaitItem()
            assertEquals(expected, item)
        }
    }

    @Test
    fun `returns correctly mapped actions if user has a preference set`() = runTest {
        // Given
        val mobileSettings = MobileSettings(
            listToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
                    .map { ToolbarAction.enumOf(it.value) }
            ),
            messageToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.ReplyOrReplyAll,
                    ToolbarAction.MarkAsReadOrUnread
                )
                    .map { ToolbarAction.enumOf(it.value) }
            ),
            conversationToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.MoveToSpam,
                    ToolbarAction.MoveToTrash,
                    ToolbarAction.MoveTo
                )
                    .map { ToolbarAction.enumOf(it.value) }
            )
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns null
            every { this@mockk.mobileSettings } returns mobileSettings
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            // Then
            val expected = expectedDefaultPreference(
                convMode = false,
                inboxActions = listOf(
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                ),
                messageActions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.ReplyOrReplyAll,
                    ToolbarAction.MarkAsReadOrUnread
                ),
                conversationActions = listOf(
                    ToolbarAction.MoveToSpam,
                    ToolbarAction.MoveToTrash,
                    ToolbarAction.MoveTo
                )
            )
            val item = awaitItem()
            assertEquals(expected, item)
        }
    }

    @Test
    fun `returns correctly partitioned actions when user has a preference`() = runTest {
        // Given
        val mobileSettings = MobileSettings(
            listToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
                    .map { ToolbarAction.enumOf(it.value) }
            ),
            messageToolbar = null,
            conversationToolbar = null
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns ViewMode.enumOf(ViewMode.ConversationGrouping.value)
            every { this@mockk.mobileSettings } returns mobileSettings
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            // Then
            val expected = expectedDefaultPreference(
                convMode = true,
                inboxActions = listOf(
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `removes an action from the selection when deselected`() = runTest {
        // Given
        val mobileSettingsMock = MobileSettings(
            listToolbar = null,
            messageToolbar = null,
            conversationToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
                    .map { ToolbarAction.enumOf(it.value) }
            )
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns ViewMode.enumOf(ViewMode.ConversationGrouping.value)
            every { this@mockk.mobileSettings } returns mobileSettingsMock
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            awaitItem()
            repo.toggleSelection(ToolbarAction.Print.value, tab = SettingsToolbarType.Message, toggled = false)

            // Then
            val expected = expectedDefaultPreference(
                convMode = true,
                conversationActions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.MoveToSpam
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `reorders an action when requested`() = runTest {
        // Given
        val mobileSettingsMock = MobileSettings(
            listToolbar = null,
            messageToolbar = null,
            conversationToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
                    .map { ToolbarAction.enumOf(it.value) }
            )
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns ViewMode.enumOf(ViewMode.ConversationGrouping.value)
            every { this@mockk.mobileSettings } returns mobileSettingsMock
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            awaitItem()
            repo.reorder(fromIndex = 1, toIndex = 3, tab = SettingsToolbarType.Message)

            // Then
            val expected = expectedDefaultPreference(
                convMode = true,
                conversationActions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam,
                    ToolbarAction.MoveTo
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `adds an action from the selection when selected`() = runTest {
        // Given
        val mobileSettingsMock = MobileSettings(
            listToolbar = null,
            messageToolbar = null,
            conversationToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam
                )
                    .map { ToolbarAction.enumOf(it.value) }
            )
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns ViewMode.enumOf(ViewMode.ConversationGrouping.value)
            every { this@mockk.mobileSettings } returns mobileSettingsMock
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            awaitItem()
            repo.toggleSelection(ToolbarAction.MoveToArchive.value, tab = SettingsToolbarType.Message, toggled = true)

            // Then
            val expected = expectedDefaultPreference(
                convMode = true,
                conversationActions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print,
                    ToolbarAction.MoveToSpam,
                    ToolbarAction.MoveToArchive
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `resets to defaults when reset`() = runTest {
        // Given
        val mobileSettingsMock = MobileSettings(
            listToolbar = ActionsToolbarSetting(
                isCustom = false,
                actions = listOf(
                    ToolbarAction.ReportPhishing,
                    ToolbarAction.MoveTo,
                    ToolbarAction.Print
                )
                    .map { ToolbarAction.enumOf(it.value) }
            ),
            messageToolbar = null,
            conversationToolbar = null
        )
        val settingsMock = mockk<MailSettings> {
            every { this@mockk.viewMode } returns ViewMode.enumOf(ViewMode.ConversationGrouping.value)
            every { this@mockk.mobileSettings } returns mobileSettingsMock
        }
        val resp = DataResult.Success(ResponseSource.Remote, settingsMock)
        coEvery { mailSettingsRepository.getMailSettingsFlow(any(), any()) } returns flowOf(resp)

        // When
        repo.inMemoryPreferences().test {
            awaitItem()
            repo.resetToDefault(tab = SettingsToolbarType.Inbox)

            // Then
            val expected = expectedDefaultPreference(convMode = true)
            assertEquals(expected, awaitItem())
        }
    }

    private fun expectedDefaultPreference(
        convMode: Boolean,
        messageActions: List<ToolbarAction>? = null,
        conversationActions: List<ToolbarAction>? = null,
        inboxActions: List<ToolbarAction>? = null
    ) = ToolbarActionsPreference(
        messageToolbar = expectedActions(messageActions, Defaults.MessageActions, Defaults.AllMessageActions),
        conversationToolbar = expectedActions(conversationActions, Defaults.MessageActions, Defaults.AllMessageActions),
        listToolbar = expectedActions(inboxActions, Defaults.InboxActions, Defaults.AllInboxActions),
        isConversationMode = convMode
    )

    private fun expectedActions(
        custom: List<ToolbarAction>?,
        defaults: List<ToolbarAction>,
        all: List<ToolbarAction>
    ) = ToolbarActions(
        current = ActionSelection(
            selected = custom ?: defaults,
            all = all
        ),
        default = defaults
    )
}
