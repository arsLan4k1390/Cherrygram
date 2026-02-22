/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.ui.mainTabs

import android.content.Context
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.glass.GlassTabView
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

object MainTabsManager {

    enum class TabType {
        CHATS,
        CONTACTS,
        CALLS,
        SETTINGS,
        PROFILE,
        SEARCH
    }

    data class Tab(
        var type: TabType,

        @JvmField
        var enabled: Boolean
    )

    fun getEnabledTabs(): List<Tab> {
        return loadTabs().filter { it.enabled }
    }

    fun getAllTabs(): List<Tab> {
        return loadTabs()
    }

    private fun loadTabs(): MutableList<Tab> {
        val allPossibleTypes = TabType.entries.toMutableList()
        val value = CherrygramAppearanceConfig.mainTabsOrder

        val result = mutableListOf<Tab>()

        if (value == null) {
            result.add(Tab(TabType.SETTINGS, true))
            result.add(Tab(TabType.CHATS, true))
            result.add(Tab(TabType.SEARCH, true))
        } else {
            val parts = value.split(",")

            for (p in parts) {
                val enabled = !p.startsWith("!")
                val typeName = if (enabled) p else p.substring(1)

                try {
                    val type = TabType.valueOf(typeName)
                    result.add(Tab(type, enabled))
                    allPossibleTypes.remove(type)
                } catch (_: Exception) {
                }
            }

            for (newType in allPossibleTypes) {
                result.add(Tab(newType, true))
            }
        }

        return result
    }

    fun createTabView(
        context: Context,
        resourceProvider: Theme.ResourcesProvider?,
        currentAccount: Int,
        type: TabType,
        fromSettings: Boolean
    ): GlassTabView {
        return when (type) {
            TabType.CHATS -> GlassTabView.createMainTab(
                context,
                resourceProvider,
                GlassTabView.TabAnimation.CHATS,
                R.string.MainTabsChats
            )

            TabType.CONTACTS -> GlassTabView.createMainTab(
                context,
                resourceProvider,
                GlassTabView.TabAnimation.CONTACTS,
                R.string.MainTabsContacts
            )

            TabType.CALLS -> GlassTabView.createMainTab(
                context,
                resourceProvider,
                GlassTabView.TabAnimation.CALLS,
                R.string.Calls
            )

            TabType.SETTINGS -> {
                if (!hasTab(TabType.PROFILE) && !fromSettings) {
                    GlassTabView.createAvatar(
                        context,
                        resourceProvider,
                        currentAccount,
                        R.string.Settings
                    )
                } else {
                    GlassTabView.createMainTab(
                        context,
                        resourceProvider,
                        GlassTabView.TabAnimation.SETTINGS,
                        R.string.Settings
                    )
                }
            }

            TabType.PROFILE -> GlassTabView.createAvatar(
                context,
                resourceProvider,
                currentAccount,
                R.string.MainTabsProfile
            )

            TabType.SEARCH -> GlassTabView.createStaticTab(
                context,
                resourceProvider,
                R.drawable.ic_ab_search,
                R.string.Search
            )
        }
    }

    fun getPosition(type: TabType): Int {
        val tabs = getEnabledTabs()
        for (i in tabs.indices) {
            if (tabs[i].type == type) {
                return i
            }
        }
        return -1
    }

    fun hasTab(type: TabType): Boolean {
        return getPosition(type) != -1
    }

    fun saveTabs(tabs: List<Tab>) {
        val order = tabs.map { tab ->
            val prefix = if (tab.enabled) "" else "!"
            "$prefix${tab.type.name}"
        }
        CherrygramAppearanceConfig.mainTabsOrder = order.joinToString(",")
    }

}