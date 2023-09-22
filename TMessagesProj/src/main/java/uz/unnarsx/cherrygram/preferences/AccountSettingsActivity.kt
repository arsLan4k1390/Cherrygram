package uz.unnarsx.cherrygram.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.*
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC.*
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeDescription
import org.telegram.ui.Cells.*
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter
import java.util.*

@SuppressLint("RtlHardcoded")
class AccountSettingsActivity : BaseFragment() {

    private var listView: RecyclerListView? = null
    private var listAdapter: ListAdapter? = null
    private var rowCount = 0
    private var accountRow = 0
    private var deleteAccountRow = 0

    override fun onFragmentCreate(): Boolean {
        super.onFragmentCreate()
        updateRows()
        return true
    }

    @SuppressLint("NewApi")
    override fun createView(context: Context): View {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setTitle(LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount))

        if (AndroidUtilities.isTablet()) {
            actionBar.occupyStatusBar = false
        }
        actionBar.setActionBarMenuOnItemClick(object : ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id == -1) {
                    finishFragment()
                }
            }
        })

        listAdapter = ListAdapter(context)
        fragmentView = FrameLayout(context)
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray))
        val frameLayout = fragmentView as FrameLayout
        listView = RecyclerListView(context)
        listView!!.isVerticalScrollBarEnabled = false
        listView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP or Gravity.LEFT))

        listView!!.adapter = listAdapter
        listView!!.setOnItemClickListener { view: View?, position: Int ->
            if (position == deleteAccountRow) {
                val builder = AlertDialog.Builder(parentActivity)
                builder.setMessage(LocaleController.getString("TosDeclineDeleteAccount", R.string.TosDeclineDeleteAccount))
                builder.setTitle(LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount))
                builder.setPositiveButton(LocaleController.getString("Deactivate", R.string.Deactivate)) { dialog: DialogInterface?, which: Int ->
                    if (BuildConfig.DEBUG) return@setPositiveButton
                    val progressDialog = AlertDialog(parentActivity, 3)
                    progressDialog.setCanCancel(false)
                    val dialogs = ArrayList(messagesController.allDialogs)
                    for (TLdialog in dialogs) {
                        if (TLdialog is TL_dialogFolder) {
                            continue
                        }
                        val peer = messagesController.getPeer(TLdialog.id.toInt().toLong())
                        if (peer.channel_id != 0L) {
                            val chat = messagesController.getChat(peer.channel_id)
                            if (!chat.broadcast) {
                                messageHelper.deleteUserHistoryWithSearch(this@AccountSettingsActivity, TLdialog.id, 0, null)
                            }
                        }
                        if (peer.user_id != 0L) {
                            messagesController.deleteDialog(TLdialog.id, 0, true)
                        }
                    }
                    Utilities.globalQueue.postRunnable({
                        val req = TL_account_deleteAccount()
                        req.reason = "Cherry"
                        connectionsManager.sendRequest(req) { response: TLObject?, error: TL_error? ->
                            AndroidUtilities.runOnUIThread {
                                try {
                                    progressDialog.dismiss()
                                } catch (e: Exception) {
                                    FileLog.e(e)
                                }
                                if (response is TL_boolTrue) {
                                    messagesController.performLogout(0)
                                } else if (error == null || error.code != -1000) {
                                    var errorText = LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred)
                                    if (error != null) {
                                        errorText += """
                                           ${error.text}
                                           """.trimIndent()
                                    }
                                    val builder1 = AlertDialog.Builder(parentActivity)
                                    builder1.setTitle(LocaleController.getString("CG_AppName", R.string.CG_AppName))
                                    builder1.setMessage(errorText)
                                    builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
                                    builder1.show()
                                }
                            }
                        }
                    }, 20000)
                    progressDialog.show()
                }
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
                val dialog = builder.create()
                dialog.setOnShowListener {
                    val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE) as TextView
                    button.setTextColor(Theme.getColor(Theme.key_text_RedBold))
                    button.isEnabled = false
                    val buttonText = button.text
                    object : CountDownTimer(60000, 100) {
                        override fun onTick(millisUntilFinished: Long) {
                            button.text = String.format(Locale.getDefault(), "%s (%d)", buttonText, millisUntilFinished / 1000 + 1)
                        }

                        override fun onFinish() {
                            button.text = buttonText
                            button.isEnabled = true
                        }
                    }.start()
                }
                showDialog(dialog)
            }
        }
        return fragmentView
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (listAdapter != null) {
            listAdapter!!.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRows() {
        rowCount = 0
        accountRow = rowCount++
        deleteAccountRow = rowCount++

        if (listAdapter != null) {
            listAdapter!!.notifyDataSetChanged()
        }
    }

    override fun getThemeDescriptions(): ArrayList<ThemeDescription> {
        val themeDescriptions = ArrayList<ThemeDescription>()
        themeDescriptions.add(ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, arrayOf<Class<*>>(EmptyCell::class.java, TextSettingsCell::class.java, TextCheckCell::class.java, HeaderCell::class.java, TextDetailSettingsCell::class.java, NotificationsCheckCell::class.java), null, null, null, Theme.key_windowBackgroundWhite))
        themeDescriptions.add(ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray))

        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue))
        themeDescriptions.add(ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue))
        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue))
        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle))
        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue))
        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground))
        themeDescriptions.add(ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem))

        themeDescriptions.add(ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(View::class.java), Theme.dividerPaint, null, null, Theme.key_divider))

        themeDescriptions.add(ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, arrayOf<Class<*>>(ShadowSectionCell::class.java), null, null, null, Theme.key_windowBackgroundGrayShadow))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextSettingsCell::class.java), arrayOf("textView"), null, null, null, Theme.key_windowBackgroundWhiteBlackText))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextSettingsCell::class.java), arrayOf("valueTextView"), null, null, null, Theme.key_windowBackgroundWhiteValueText))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(NotificationsCheckCell::class.java), arrayOf("textView"), null, null, null, Theme.key_windowBackgroundWhiteBlackText))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(NotificationsCheckCell::class.java), arrayOf("valueTextView"), null, null, null, Theme.key_windowBackgroundWhiteGrayText2))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(NotificationsCheckCell::class.java), arrayOf("checkBox"), null, null, null, Theme.key_switchTrack))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(NotificationsCheckCell::class.java), arrayOf("checkBox"), null, null, null, Theme.key_switchTrackChecked))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextCheckCell::class.java), arrayOf("textView"), null, null, null, Theme.key_windowBackgroundWhiteBlackText))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextCheckCell::class.java), arrayOf("valueTextView"), null, null, null, Theme.key_windowBackgroundWhiteGrayText2))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextCheckCell::class.java), arrayOf("checkBox"), null, null, null, Theme.key_switchTrack))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextCheckCell::class.java), arrayOf("checkBox"), null, null, null, Theme.key_switchTrackChecked))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(HeaderCell::class.java), arrayOf("textView"), null, null, null, Theme.key_windowBackgroundWhiteBlueHeader))

        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextDetailSettingsCell::class.java), arrayOf("textView"), null, null, null, Theme.key_windowBackgroundWhiteBlackText))
        themeDescriptions.add(ThemeDescription(listView, 0, arrayOf<Class<*>>(TextDetailSettingsCell::class.java), arrayOf("valueTextView"), null, null, null, Theme.key_windowBackgroundWhiteGrayText2))

        return themeDescriptions
    }

    private inner class ListAdapter(private val mContext: Context) :
        SelectionAdapter() {
        override fun getItemCount(): Int {
            return rowCount
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                1 -> {
                    holder.itemView.background = Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow)
                }
                2 -> {
                    val textCell = holder.itemView as TextSettingsCell
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
                    if (position == deleteAccountRow) {
                        textCell.setText(LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount), false)
                        textCell.setTextColor(Theme.getColor(Theme.key_text_RedRegular))
                    }
                }
                3 -> {
                    val textCell = holder.itemView as TextCheckCell
                    textCell.setEnabled(true, null)
                }
                4 -> {
                    val headerCell = holder.itemView as HeaderCell
                    if (position == accountRow) {
                        headerCell.setText(LocaleController.getString("SP_Category_Account", R.string.SP_Category_Account))
                    }
                }
            }
        }

        override fun isEnabled(holder: RecyclerView.ViewHolder): Boolean {
            val type = holder.itemViewType
            return type == 2 || type == 3
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view: View? = null
            when (viewType) {
                1 -> view = ShadowSectionCell(mContext)
                2 -> {
                    view = TextSettingsCell(mContext)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }
                3 -> {
                    view = TextCheckCell(mContext)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }
                4 -> {
                    view = HeaderCell(mContext)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }
            }
            view!!.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            return RecyclerListView.Holder(view)
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                deleteAccountRow -> 2
                accountRow -> 4
                else -> 3
            }
        }
    }
}