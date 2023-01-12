package uz.unnarsx.cherrygram.preferences

import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.browser.Browser
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.tgkit.preference.*
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextIconRow

class DonatePreferenceEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("DP_Donate", R.string.DP_Donate)) {
        /*category("Information") {
            textDetail {
                title = "Information"
                detail = "Information"
            }
        }*/

        category(LocaleController.getString("DP_Donate_Method", R.string.DP_Donate_Method)) {
            textIcon {
                title = "VISA (Visa Direct)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("4278310021797824")
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
                title = "Apelsin Donates (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://donate.apelsin.uz/pay/arsLan4k1390")
                }
            }
            textIcon {
                title = "Payme (Uzbekistan)"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://payme.uz/@arslan4k1390")
                }
            }
            textIcon {
                title = "QIWI Wallet"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    Browser.openUrl(bf.parentActivity, "https://qiwi.com/n/ARSLAN4K1390/")
                }
            }
            textIcon {
                title = "QIWI MIR Card"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("2200730250744709")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }
            /*textIcon {
                title = "YooMoney Wallet"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    val openURL = Intent(android.content.Intent.ACTION_VIEW)
                    openURL.data = Uri.parse("https://yoomoney.ru/to/4100116983696293")
                    bf.parentActivity.startActivity(openURL)
                }
            }*/
            /*textIcon {
                title = "TON Coin"
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    AndroidUtilities.addToClipboard("EQCVVayzZkpm4LoHi9yuJQFjxRqi2qM4o0dqMLvZnXOFFE-3")
                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
                }
            }*/
//            textIcon {
//                title = "Bitcoin"
//                divider = true
//
//                listener = TGKitTextIconRow.TGTIListener {
//                    AndroidUtilities.addToClipboard("3JQ7rBmnbJBxQQ25TsFLzy1F74k3jgfcYj")
//                    Toast.makeText(bf.parentActivity, LocaleController.getString("CardNumberCopied", R.string.CardNumberCopied), Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }
}