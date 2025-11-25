/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.donates

import androidx.core.graphics.toColorInt
import org.telegram.messenger.FileLog
import org.telegram.ui.ActionBar.Theme
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

enum class BadgeHelper {

    /** Colors start */
    DONATES(-9051393, -8854529), // Cyan (Blue + Green)
    GOLD(-15514), // Gold (Yellow)
    PREMIUM(-92501, -23362), // Red (Peach)
    PURPLE(-1074689, -609281), // Purple (Purple + Pink)
    BROWN(-1192753, -1192753), // Burgundy
    PINK(0, 0); // Mexican Pink
    /** Colors finish */

    private val darkColor: Int?
    private val lightColor: Int?

    constructor(darkColor: Int, lightColor: Int?) {
        this.darkColor = darkColor
        this.lightColor = lightColor
    }

    constructor(singleColor: Int) {
        this.darkColor = singleColor
        this.lightColor = null
    }

    fun forTheme(): Int = forTheme(!Theme.isCurrentThemeDay())

    fun forTheme(isDark: Boolean): Int {
        return when {
            isDark && darkColor != null -> darkColor
            !isDark && lightColor != null -> lightColor
            darkColor != null -> darkColor
            lightColor != null -> lightColor
            else -> error("No color defined for $this")
        }
    }

    data class UserColor(
        val lightColor: Int,
        val darkColor: Int,
        val alpha: Int
    )

    companion object {

        private val badgeColors = mutableMapOf<Long, UserColor>()

        fun updateBadgeColorsMap(map: Map<Long, UserColor>) {
            synchronized(badgeColors) {
                badgeColors.clear()
                badgeColors.putAll(map)
            }
        }

        fun getUserColor(userId: Long): UserColor? {
            synchronized(badgeColors) {
                return badgeColors[userId]
            }
        }

        fun getEmojiStatusColor(userId: Long, defaultColor: Int, forceDonates: Boolean): Int {

            if (userId == 0L) return defaultColor
            if (forceDonates) return DONATES.forTheme()

            val isPremium = false; // cgPremium
            val isDonated = DonatesManager.didUserDonateForMarketplace(userId)

            getUserColor(userId)?.let { uc ->
                val color = if (Theme.isCurrentThemeDay()) uc.lightColor else uc.darkColor
                if (CherrygramCoreConfig.isDevBuild()) {
                    FileLog.d("UserID $userId: BadgeColorsManager color = ${color.toHexString()} (dynamic)")
                }
                return color
            }

            return when {
                isPremium -> PREMIUM.forTheme() // convertColor("#C7637F", 255) //B45872
                isDonated -> DONATES.forTheme()
                else -> defaultColor
            }
        }

        fun convertColor(color: String, alpha: Int = 255): Int {
            val hex = if (color.startsWith("#")) color else "#$color"

            val rgb = hex.toColorInt() and 0x00FFFFFF

            return (alpha.coerceIn(0, 255) shl 24) or rgb
        }

        private fun Int.toHexString(): String {
            return String.format("#%08X", this)
        }
    }

}
