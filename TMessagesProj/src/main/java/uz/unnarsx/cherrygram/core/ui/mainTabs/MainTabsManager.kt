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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
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

    @JvmOverloads
    fun getEnabledTabs(includeSearch: Boolean = false): List<Tab> {
        val enabled = loadTabs().filter { it.enabled }
        return if (includeSearch) {
            enabled
        } else {
            enabled.filterNot { it.type == TabType.SEARCH }
        }
    }

    fun getAllTabs(): List<Tab> {
        return loadTabs()
    }

    fun getEnabledTabTypes(): List<TabType> {
        return getEnabledTabs().map { it.type }
    }

    private const val DEFAULT_ORDER = "SETTINGS,CHATS,PROFILE,!CONTACTS,!CALLS,SEARCH"

    private fun loadTabs(): MutableList<Tab> {
        val allPossibleTypes = TabType.entries.toMutableList()
        val value = CherrygramAppearanceConfig.mainTabsOrder ?: DEFAULT_ORDER

        val result = mutableListOf<Tab>()

        for (p in value.split(",")) {
            val disabled = p.startsWith("!")
            val type = try {
                TabType.valueOf(if (disabled) p.substring(1) else p)
            } catch (_: Exception) {
                continue
            }
            if (!allPossibleTypes.remove(type)) continue
            result.add(Tab(type, !disabled || type == TabType.CHATS))
        }

        for (newType in allPossibleTypes) {
            if (newType == TabType.SEARCH) {
                result.add(Tab(newType, true))
            } else {
                val searchIndex = result.indexOfFirst { it.type == TabType.SEARCH }
                result.add(if (searchIndex == -1) result.size else searchIndex, Tab(newType, false))
            }
        }

        return result
    }

    fun createTabView(
        context: Context,
        resourceProvider: Theme.ResourcesProvider?,
        currentAccount: Int,
        type: TabType,
        fromSettings: Boolean,
        showSearch: Boolean
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

            TabType.SEARCH -> {
                if (showSearch) {
                    GlassTabView.createStaticTab(
                        context,
                        resourceProvider,
                        R.drawable.outline_header_search,
                        R.string.Search,
                        false
                    )
                } else {
                    GlassTabView(context).apply {
                        visibility = View.GONE
                        layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                }
            }
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