package uz.unnarsx.cherrygram.preferences

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class DonatePreferenceEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("DP_Donate", R.string.DP_Donate)) {
        val isDarkMode: Boolean = !Theme.isCurrentThemeDay()
        category(LocaleController.getString("DP_Donate_Method", R.string.DP_Donate_Method)) {
            textIcon {
                icon = if (isDarkMode) R.drawable.card_visa_dark else R.drawable.card_visa_light
                title = "VISA (Visa Direct)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("4278310028377794")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_master_dark else R.drawable.card_master_light
                title = "MasterCard (MoneySend)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("5477330021782747")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_tirikchilik_dark else R.drawable.card_tirikchilik_light
                title = "Tirikchilik (Uzbekistan)"

                listener = TGKitTextIconRow.TGTIListener {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse("https://tirikchilik.uz/arslan4k1390")
                    bf.parentActivity.startActivity(openURL)
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_humo_dark else R.drawable.card_humo_light
                title = "HUMO (Uzbekistan)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("9860100124035617")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_uzcard_dark else R.drawable.card_uzcard_light
                title = "UzCard (Uzbekistan)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("8600490727700080")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_uzcard_mir_dark else R.drawable.card_uzcard_mir_light
                title = "UzCard-MIR Co-Badge (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("5614681912473893")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_ym_dark else R.drawable.card_ym_light
                title = "YooMoney (RUB)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("4100116983696293")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
        }

        category("Binance") {
            textIcon {
                icon = if (isDarkMode) R.drawable.card_binance_dark else R.drawable.card_binance_light
                title = "Binance ID"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("220943480")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_btc_dark else R.drawable.card_btc_light
                title = "Bitcoin (BTC)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("1Pr6GqqWakgKWW1nDjVyHUYo1AcWbSN453")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_eth_dark else R.drawable.card_eth_light
                title = "Ethereum (ERC20)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("0xa8392346f426443ef7e3d98047bace6dbcc0e7d8")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_usdt_dark else R.drawable.card_usdt_light
                title = "TetherUS - USDT (TRC20)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("TKnPT5rojMf851ejov2Xu4pxKcMfSh4Ws9")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
        }
        category("Telegram Wallet") {
            textIcon {
                icon = if (isDarkMode) R.drawable.card_ton_dark else R.drawable.card_ton_light
                title = "TON Coin (v3R2) // Tonkeeper"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("EQCVVayzZkpm4LoHi9yuJQFjxRqi2qM4o0dqMLvZnXOFFE-3")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_ton_dark else R.drawable.card_ton_light
                title = "TON Coin // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("UQBDb7Px1bDnNyGniB38ZjW3tx2kP3cQL7VcF9XzWvMS3sAa")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_not_dark else R.drawable.card_not_light
                title = "NOT Coin // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("UQBDb7Px1bDnNyGniB38ZjW3tx2kP3cQL7VcF9XzWvMS3sAa")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_usdt_dark else R.drawable.card_usdt_light
                title = "USDT (TRC20) // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("TU61MLJEcQqXCqqvAk651kPieCdUSMsjvf")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                icon = if (isDarkMode) R.drawable.card_btc_dark else R.drawable.card_btc_light
                title = "Bitcoin (BTC) // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("1FsXkKKCpPF5EszZCd4KwhJhjGSjmwzqQZ")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}