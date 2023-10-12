package uz.unnarsx.cherrygram.preferences

import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextIconRow

class DonatePreferenceEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("DP_Donate", R.string.DP_Donate)) {
        category(LocaleController.getString("DP_Donate_Method", R.string.DP_Donate_Method)) {
            textIcon {
                title = "VISA (Visa Direct)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("4278310028769180")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "MasterCard (MoneySend)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("5397170000155375")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "HUMO (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("9860100124035617")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "UzCard (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("8600490439085465")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "UzCard-MIR Co-Badge (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("5614681912473893")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
        }

        category("Binance") {
            textIcon {
                title = "Binance ID"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("220943480")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "Bitcoin (BTC)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("1Pr6GqqWakgKWW1nDjVyHUYo1AcWbSN453")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "Ethereum (ERC20)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("0xa8392346f426443ef7e3d98047bace6dbcc0e7d8")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "TetherUS - USDT (TRC20)"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("TKnPT5rojMf851ejov2Xu4pxKcMfSh4Ws9")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
        }
        category("Telegram Wallet") {
            textIcon {
                title = "TON Coin (v3R2) // Tonkeeper"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("EQCVVayzZkpm4LoHi9yuJQFjxRqi2qM4o0dqMLvZnXOFFE-3")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "TON Coin // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("UQBDb7Px1bDnNyGniB38ZjW3tx2kP3cQL7VcF9XzWvMS3sAa")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "USDT (TRC20) // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("TU61MLJEcQqXCqqvAk651kPieCdUSMsjvf")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }
            textIcon {
                title = "Bitcoin (BTC) // @wallet"

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("1FsXkKKCpPF5EszZCd4KwhJhjGSjmwzqQZ")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}