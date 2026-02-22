/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.distance;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.ThemePreviewMessagesCell;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilledTabsView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SimpleThemeDescription;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.Stories.StoriesUtilities;
import org.telegram.ui.UserInfoActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.Extra;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class MessagesAndProfilesPreferencesEntry extends BaseFragment {

    private FrameLayout contentView;
    private ColoredActionBar colorBar;

    private final int PAGE_MESSAGE = 0;
    private final int PAGE_PROFILE = 1;

    public Page messagePage;
    public Page profilePage;

    public Page getCurrentPage() {
        return viewPager.getCurrentPosition() == 0 ? messagePage : profilePage;
    }

    private class Page extends FrameLayout {

        private ProfilePreview profilePreview;

        private RecyclerListView listView;
        private RecyclerView.Adapter listAdapter;

        private int selectedColor;
        private long selectedEmoji;
        private ThemePreviewMessagesCell messagesCellPreview;

        int rowCount;

        int previewRow = -1;
        int messagePreviewDivisorRow = -1;
        int headerRow = -1;

        int timeWithSecondsSwitchRow = -1;
        int premiumStatusSwitchRow = -1;
        int replyBackgroundSwitchRow = -1;
        int replyColorSwitchRow = -1;
        int replyEmojiSwitchRow = -1;

        int infoHeaderRow;
        int idDcPreviewRow;
        int birthdayPreviewRow;
        int businessHoursPreviewRow;
        int businessLocationPreviewRow;
        int channelPreviewRow;

        int channelPreviewSwitchRow = -1;
        int showDcIdSwitchRow = -1;
        int birthdayPreviewSwitchRow = -1;
        int businessPreviewSwitchRow = -1;
        int profilePreviewDivisorRow = -1;
        int profileBackgroundSwitchRow = -1;
        int profileEmojiSwitchRow = -1;

        private final int VIEW_TYPE_MESSAGE = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_SWITCH = 2;
//        private final int VIEW_TYPE_TEXT_SETTING = 3;
        private final int VIEW_TYPE_TEXT_DETAIL = 4;
        private final int VIEW_TYPE_CHANNEL = 5;
        private final int VIEW_TYPE_SHADOW = 6;

        private final int type;
        public Page(Context context, int type) {
            super(context);
            this.type = type;

            TLRPC.User user = getUserConfig().getCurrentUser();

            if (type == PAGE_PROFILE) {
                if (user.premium && UserObject.getProfileColorId(user) != -1) {
                    selectedColor = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor() ? UserObject.getProfileColorId(user) : -1;
                } else {
                    selectedColor = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor() ? Constants.PROFILE_BACKGROUND_COLOR_ID_RED : -1;
                }

                if (user.premium && UserObject.getProfileEmojiId(user) != 0) {
                    selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji() ? UserObject.getProfileEmojiId(user) : 0;
                } else {
                    selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji() ? Constants.CHERRY_EMOJI_ID : 0;
                }
            } else {
                if (user.premium && UserObject.getColorId(user) != -1) {
                    selectedColor = CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors() ? UserObject.getColorId(user) : -1;
                } else {
                    selectedColor = CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors() ? Constants.REPLY_BACKGROUND_COLOR_ID : -1;
                }

                if (user.premium && UserObject.getEmojiId(user) != 0) {
                    selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji() ? UserObject.getEmojiId(user) : 0;
                } else {
                    selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji() ? Constants.CHERRY_EMOJI_ID : 0;
                }
            }

            listView = new RecyclerListView(getContext(), getResourceProvider()) {
                @Override
                protected void onMeasure(int widthSpec, int heightSpec) {
                    super.onMeasure(widthSpec, heightSpec);
                }

                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                }
            };
            ((DefaultItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);
            listView.setLayoutManager(new LinearLayoutManager(getContext()));
            listView.setAdapter(listAdapter = new RecyclerListView.SelectionAdapter() {
                @Override
                public boolean isEnabled(RecyclerView.ViewHolder holder) {
                    return holder.getItemViewType() == VIEW_TYPE_SWITCH /*|| holder.getItemViewType() == VIEW_TYPE_TEXT_SETTING*/
                            || holder.getItemViewType() == VIEW_TYPE_TEXT_DETAIL || holder.getItemViewType() == VIEW_TYPE_CHANNEL;
                }

                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view;
                    switch (viewType) {
                        case VIEW_TYPE_MESSAGE:
                            ThemePreviewMessagesCell messagesCell = messagesCellPreview = new ThemePreviewMessagesCell(
                                    getContext(), parentLayout, user.premium
                                    && UserObject.getEmojiId(getUserConfig().getCurrentUser()) != 0
                                            ? ThemePreviewMessagesCell.TYPE_PEER_COLOR : ThemePreviewMessagesCell.TYPE_PEER_COLOR_CHERRY
                            );
                            messagesCell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                            messagesCell.fragment = MessagesAndProfilesPreferencesEntry.this;
                            view = messagesCell;
                            break;
                        case VIEW_TYPE_HEADER:
                            HeaderCell header = new HeaderCell(getContext());
                            header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                            view = header;
                            break;
                        case VIEW_TYPE_SWITCH:
                            TextCheckCell switchCell = new TextCheckCell(getContext());
                            switchCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                            view = switchCell;
                            break;
                        case VIEW_TYPE_TEXT_DETAIL:
                            TextDetailCell textDetailCell = new TextDetailCell(getContext(), getResourceProvider(), true, true);
                            textDetailCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                            textDetailCell.setContentDescriptionValueFirst(true);
                            view = textDetailCell;
                            break;
                        case VIEW_TYPE_CHANNEL:
                            view = new ProfileChannelCell(MessagesAndProfilesPreferencesEntry.this);
                            view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                            break;
                        case VIEW_TYPE_SHADOW:
                        default: {
                            view = new ShadowSectionCell(getContext());
                            break;
                        }
                    }
                    return new RecyclerListView.Holder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    switch (getItemViewType(position)) {
                        case VIEW_TYPE_HEADER: {
                            HeaderCell headerCell = (HeaderCell) holder.itemView;
                            if (position == infoHeaderRow) {
                                headerCell.setText(getString(R.string.Info));
                            } else if (position == headerRow) {
                                if (type == PAGE_MESSAGE) {
                                    headerCell.setText(getString(R.string.CP_CustomizeMessage));
                                } else {
                                    headerCell.setText(getString(R.string.CP_CustomizeProfile));
                                }
                            }
                            break;
                        }
                        case VIEW_TYPE_SWITCH: {
                            TextCheckCell switchCell = (TextCheckCell) holder.itemView;
                            switchCell.updateRTL();
                            if (position == timeWithSecondsSwitchRow) {
                                switchCell.setTextAndValueAndCheck(getString(R.string.CP_ShowSeconds), getString(R.string.CP_ShowSeconds_Desc), CherrygramAppearanceConfig.INSTANCE.getShowSeconds(), true, true);
                            } else if (position == premiumStatusSwitchRow) {
                                switchCell.setTextAndValueAndCheck(getString(R.string.CP_DisablePremiumStatuses), getString(R.string.CP_DisablePremiumStatuses_Desc), CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses(), true, true);
                            } else if (position == replyBackgroundSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ReplyBackground), CherrygramAppearanceConfig.INSTANCE.getReplyBackground(), false);
                            } else if (position == replyColorSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ReplyCustomColors), CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors(), false);
                            } else if (position == replyEmojiSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ReplyBackgroundEmoji), CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji(), false);
                            } else if (position == channelPreviewSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ProfileChannelPreview), CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview(), false);
                            } else if (position == showDcIdSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.AP_ShowID), CherrygramAppearanceConfig.INSTANCE.getShowIDDC(), false);
                            } else if (position == birthdayPreviewSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ProfileBirthDatePreview), CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview(), false);
                            } else if (position == businessPreviewSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ProfileBusinessPreview), CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview(), false);
                            } else if (position == profileBackgroundSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ProfileBackgroundColor), CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor(), false);
                            } else if (position == profileEmojiSwitchRow) {
                                switchCell.setTextAndCheck(getString(R.string.CP_ProfileBackgroundEmoji), CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji(), true);
                            }
                            break;
                        }
                        case VIEW_TYPE_TEXT_DETAIL: {
                            TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                            final TLRPC.User me = getUserConfig().getCurrentUser();

                            if (position == idDcPreviewRow) {
                                StringBuilder sb = new StringBuilder();
                                if (me.photo != null && me.photo.dc_id > 0) {
                                    sb = new StringBuilder();
                                    sb.append("DC: ");
                                    sb.append(me.photo.dc_id);
                                    sb.append(", ");
                                    sb.append(CGResourcesHelper.INSTANCE.getDCName(me.photo.dc_id));
                                    sb.append(", ");
                                    sb.append(CGResourcesHelper.INSTANCE.getDCGeo(me.photo.dc_id));
                                } else {
                                    sb.append("DC: ");
                                    sb.append(getString(R.string.NumberUnknown));
                                }
                                DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US) {{ setGroupingSeparator(' '); }});
                                detailCell.setTextAndValue("ID: " + df.format(me.id), sb, false);

                                Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_calendar2);
                                detailCell.setImage(drawable);
                                if (drawable != null && colorBar != null && colorBar.getActionBarButtonColor() != 0) {
                                    final int buttonColor = processColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, getResourceProvider()));
                                    drawable.setColorFilter(new PorterDuffColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY));
                                }
                                detailCell.setImageClickListener(v -> Extra.INSTANCE.getRegistrationDate(MessagesAndProfilesPreferencesEntry.this, getParentActivity(), getUserConfig().getClientUserId(), 0));
                            } else if (position == birthdayPreviewRow) {
                                TLRPC.UserFull meFull = getMessagesController().getUserFull(me.id);
                                if (meFull != null && meFull.birthday != null) {
                                    String date = UserInfoActivity.birthdayString(meFull.birthday);
                                    detailCell.setTextAndValue(date, getString(R.string.ProfileBirthday), false);
                                } else {
                                    final int age = Period.between(LocalDate.of(2022, 1, 15), LocalDate.now()).getYears();
                                    String date = LocaleController.formatPluralString("ProfileBirthdayValueYear", age, birthdayString());
                                    detailCell.setTextAndValue(date, getString(R.string.ProfileBirthday), false);
                                }
                                detailCell.textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, getResourceProvider()));

                                Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.input_calendar_add_solar);
                                detailCell.setImage(drawable);
                                if (drawable != null && colorBar != null && colorBar.getActionBarButtonColor() != 0) {
                                    final int buttonColor = processColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, getResourceProvider()));
                                    drawable.setColorFilter(new PorterDuffColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY));
                                }
                                detailCell.setImageClickListener(v -> Extra.INSTANCE.addBirthdayToCalendar(getParentActivity(), getUserConfig().getCurrentUser().id));
                            } else if (position == businessHoursPreviewRow) {
                                detailCell.textView.setTextColor(Theme.getColor(Theme.key_avatar_nameInMessageGreen, getResourceProvider()));
                                detailCell.setTextAndValue(getString(R.string.BusinessHoursProfileNowOpen), getString(R.string.BusinessHoursProfile), false);
                            } else if (position == businessLocationPreviewRow) {
                                detailCell.textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, getResourceProvider()));
                                detailCell.setTextAndValue("Cherrygram, Abu-Dhabi", getString(R.string.BusinessProfileLocation), false);
                            }
                            break;
                        }
                        case VIEW_TYPE_CHANNEL:
                            if (position == channelPreviewRow) {
                                ((ProfileChannelCell) holder.itemView).setCherry();
                            }
                            break;
                    }
                }

                @Override
                public int getItemCount() {
                    return rowCount;
                }

                @Override
                public int getItemViewType(int position) {
                    if (position == previewRow) {
                        return VIEW_TYPE_MESSAGE;
                    }
                    if (position == infoHeaderRow || position == headerRow) {
                        return VIEW_TYPE_HEADER;
                    }
                    if (position == timeWithSecondsSwitchRow || position == premiumStatusSwitchRow || position == replyBackgroundSwitchRow || position == replyColorSwitchRow || position == replyEmojiSwitchRow
                            || position == channelPreviewSwitchRow || position == showDcIdSwitchRow || position == birthdayPreviewSwitchRow || position == businessPreviewSwitchRow || position == profileBackgroundSwitchRow || position == profileEmojiSwitchRow) {
                        return VIEW_TYPE_SWITCH;
                    }
                    if (position == idDcPreviewRow || position == birthdayPreviewRow
                            || position == businessHoursPreviewRow || position == businessLocationPreviewRow) {
                        return VIEW_TYPE_TEXT_DETAIL;
                    }
                    if (position == channelPreviewRow) {
                        return VIEW_TYPE_CHANNEL;
                    }
                    return VIEW_TYPE_SHADOW;
                }
            });
            listView.setOnItemClickListener((view, position) -> {
                final TLRPC.User me = getUserConfig().getCurrentUser();

                if (position == timeWithSecondsSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setShowSeconds(!CherrygramAppearanceConfig.INSTANCE.getShowSeconds());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowSeconds());
                    }
                    LocaleController.getInstance().recreateFormatters();
                    parentLayout.rebuildAllFragmentViews(true, true);
                } else if (position == premiumStatusSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setDisablePremiumStatuses(!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses());
                    }

                    if (!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses()) {
                        profilePage.profilePreview.titleView.setRightDrawable(profilePage.profilePreview.statusEmoji);
                        profilePage.profilePreview.statusEmoji.play();
                        profilePage.profilePreview.subtitleView.setText(getString(R.string.Online));
                    } else {
                        profilePage.profilePreview.titleView.setRightDrawable(0);
                        String tgPremium = CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses() ? " | TG Premium" : "";
                        profilePage.profilePreview.subtitleView.setText(getString(R.string.Online) + tgPremium);
                    }

                    updateMessages();
                    if (type == PAGE_PROFILE) {
                        messagePage.updateMessages();
                        messagePage.updateRows();
                    } else {
                        profilePage.updateRows();
                    }
                } else if (position == replyBackgroundSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setReplyBackground(!CherrygramAppearanceConfig.INSTANCE.getReplyBackground());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyBackground());
                    }

                    updateMessages();
                } else if (position == replyColorSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setReplyCustomColors(!CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors());
                    }

                    if (me.premium && UserObject.getColorId(me) != -1) {
                        selectedColor = CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors() ? UserObject.getColorId(me) : -1;
                    } else {
                        selectedColor = CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors() ? Constants.REPLY_BACKGROUND_COLOR_ID : -1;
                    }

                    updateMessages();
                } else if (position == replyEmojiSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setReplyBackgroundEmoji(!CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji());
                    }

                    if (me.premium && UserObject.getEmojiId(me) != 0) {
                        selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji() ? UserObject.getEmojiId(me) : 0;
                    } else {
                        selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji() ? Constants.CHERRY_EMOJI_ID : 0;
                    }

                    updateMessages();
                } else if (position == channelPreviewRow) {
                    Browser.openUrl(getParentActivity(), Constants.CG_CHANNEL_URL);
                } else if (position == channelPreviewSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setProfileChannelPreview(!CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview());
                    }

                    listAdapter.notifyItemChanged(channelPreviewSwitchRow);
                    listAdapter.notifyItemChanged(channelPreviewRow);
                    listAdapter.notifyItemChanged(profilePreviewDivisorRow);
                    listAdapter.notifyItemChanged(idDcPreviewRow);
                    listAdapter.notifyItemChanged(birthdayPreviewRow);
                    listAdapter.notifyItemChanged(businessHoursPreviewRow);
                    listAdapter.notifyItemChanged(businessLocationPreviewRow);
                    profilePage.updateRows();
                    parentLayout.rebuildAllFragmentViews(false, false);
                } else if (position == showDcIdSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setShowIDDC(!CherrygramAppearanceConfig.INSTANCE.getShowIDDC());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowIDDC());
                    }

                    listAdapter.notifyItemChanged(channelPreviewRow);
                    listAdapter.notifyItemChanged(showDcIdSwitchRow);
                    listAdapter.notifyItemChanged(profilePreviewDivisorRow);
                    listAdapter.notifyItemChanged(idDcPreviewRow);
                    listAdapter.notifyItemChanged(birthdayPreviewRow);
                    listAdapter.notifyItemChanged(businessHoursPreviewRow);
                    listAdapter.notifyItemChanged(businessLocationPreviewRow);
                    profilePage.updateRows();
                    parentLayout.rebuildAllFragmentViews(false, false);
                } else if (position == birthdayPreviewSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setProfileBirthDatePreview(!CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview());
                    }

                    listAdapter.notifyItemChanged(channelPreviewRow);
                    listAdapter.notifyItemChanged(birthdayPreviewSwitchRow);
                    listAdapter.notifyItemChanged(profilePreviewDivisorRow);
                    listAdapter.notifyItemChanged(idDcPreviewRow);
                    listAdapter.notifyItemChanged(birthdayPreviewRow);
                    listAdapter.notifyItemChanged(businessHoursPreviewRow);
                    listAdapter.notifyItemChanged(businessLocationPreviewRow);
                    profilePage.updateRows();
                    parentLayout.rebuildAllFragmentViews(false, false);
                } else if (position == businessPreviewSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setProfileBusinessPreview(!CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview());
                    }

                    listAdapter.notifyItemChanged(channelPreviewRow);
                    listAdapter.notifyItemChanged(businessPreviewSwitchRow);
                    listAdapter.notifyItemChanged(profilePreviewDivisorRow);
                    listAdapter.notifyItemChanged(idDcPreviewRow);
                    listAdapter.notifyItemChanged(birthdayPreviewRow);
                    listAdapter.notifyItemChanged(businessHoursPreviewRow);
                    listAdapter.notifyItemChanged(businessLocationPreviewRow);
                    profilePage.updateRows();
                    parentLayout.rebuildAllFragmentViews(false, false);
                } else if (position == profileBackgroundSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setProfileBackgroundColor(!CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor());
                    }

                    if (me.premium && UserObject.getProfileColorId(me) != -1) {
                        selectedColor = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor() ? UserObject.getProfileColorId(me) : -1;
                    } else {
                        selectedColor = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor() ? Constants.PROFILE_BACKGROUND_COLOR_ID_RED : -1;
                    }

                    updateMessages();
                    if (type == PAGE_PROFILE) {
                        messagePage.updateMessages();
                    }
                    if (type == PAGE_PROFILE && colorBar != null) {
                        colorBar.setColor(currentAccount, selectedColor, true);
                    }
                    if (profilePreview != null) {
                        profilePreview.setColor(selectedColor, true);
                    }
                    if (profilePage != null && profilePage.profilePreview != null && messagePage != null) {
                        profilePage.profilePreview.overrideAvatarColor(messagePage.selectedColor);
                    }
                } else if (position == profileEmojiSwitchRow) {
                    CherrygramAppearanceConfig.INSTANCE.setProfileBackgroundEmoji(!CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji());
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji());
                    }

                    if (me.premium && UserObject.getProfileEmojiId(me) != 0) {
                        selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji() ? UserObject.getProfileEmojiId(me) : 0;
                    } else {
                        selectedEmoji = CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji() ? Constants.CHERRY_EMOJI_ID : 0;
                    }

                    updateMessages();
                    if (type == PAGE_PROFILE) {
                        messagePage.updateMessages();
                    }
                    if (profilePreview != null) {
                        profilePreview.setEmoji(selectedEmoji, true);
                    }
                }
            });
            addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setDurations(350);
            itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            itemAnimator.setDelayAnimations(false);
            itemAnimator.setSupportsChangeAnimations(false);
            listView.setItemAnimator(itemAnimator);

            if (type == PAGE_PROFILE) {
                profilePreview = new ProfilePreview(getContext(), currentAccount, resourceProvider);
                profilePreview.setColor(selectedColor, false);
                profilePreview.setEmoji(selectedEmoji, false);
                addView(profilePreview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));
            }

            updateColors();
            updateRows();

            setWillNotDraw(false);
        }

        private int actionBarHeight;

        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            super.dispatchDraw(canvas);
            if (getParentLayout() != null) {
                getParentLayout().drawHeaderShadow(canvas, actionBarHeight);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (type == PAGE_MESSAGE) {
                actionBarHeight = ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight;
                ((MarginLayoutParams) listView.getLayoutParams()).topMargin = actionBarHeight;
            } else {
                actionBarHeight = dp(230) + AndroidUtilities.statusBarHeight;
                ((MarginLayoutParams) listView.getLayoutParams()).topMargin = actionBarHeight;
                profilePreview.getLayoutParams().height = actionBarHeight;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        private void updateRows() {
            rowCount = 0;
            if (type == PAGE_MESSAGE) {
                previewRow = rowCount++;
                messagePreviewDivisorRow = rowCount++;
                headerRow = rowCount++;
                timeWithSecondsSwitchRow = rowCount++;

                premiumStatusSwitchRow = rowCount++;
                if (listAdapter != null) {
                    listAdapter.notifyItemRemoved(premiumStatusSwitchRow);
                    listAdapter.notifyItemInserted(premiumStatusSwitchRow);
                }
                replyBackgroundSwitchRow = rowCount++;
                replyColorSwitchRow = rowCount++;
                replyEmojiSwitchRow = rowCount++;
            }
            if (type == PAGE_PROFILE) {
                // Channel preview
                int prevChannelPreviewRow = channelPreviewRow;
                channelPreviewRow = -1;
                if (CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview()) {
                    channelPreviewRow = rowCount++;
                    profilePreviewDivisorRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevChannelPreviewRow == -1 && channelPreviewRow != -1) {
                        listAdapter.notifyItemInserted(channelPreviewRow);
                        listAdapter.notifyItemInserted(profilePreviewDivisorRow);
                    } else if (prevChannelPreviewRow != -1 && channelPreviewRow == -1) {
                        listAdapter.notifyItemRemoved(prevChannelPreviewRow);
                        listAdapter.notifyItemRemoved(profilePreviewDivisorRow);
                    }
                }
                // Channel preview

                int prevInfoHeaderRow = infoHeaderRow;
                infoHeaderRow = -1;
                if (!CherrygramAppearanceConfig.INSTANCE.getShowIDDC()
                        || CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview()
                        || CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview()
                ) {
                    infoHeaderRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevInfoHeaderRow == -1 && infoHeaderRow != -1) {
                        listAdapter.notifyItemInserted(infoHeaderRow);
                    } else if (prevInfoHeaderRow != -1 && infoHeaderRow == -1) {
                        listAdapter.notifyItemRemoved(prevInfoHeaderRow);
                    }
                }

                // DC ID
                int prevIdDcPreviewRow = idDcPreviewRow;
                idDcPreviewRow = -1;
                if (CherrygramAppearanceConfig.INSTANCE.getShowIDDC()) {
                    idDcPreviewRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevIdDcPreviewRow == -1 && idDcPreviewRow != -1) {
                        listAdapter.notifyItemInserted(idDcPreviewRow);
                    } else if (prevIdDcPreviewRow != -1 && idDcPreviewRow == -1) {
                        listAdapter.notifyItemRemoved(prevIdDcPreviewRow);
                    }
                }
                // DC ID

                // Birth date preview
                int prevBirthdayPreviewRow = birthdayPreviewRow;
                birthdayPreviewRow = -1;
                if (CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview()) {
                    birthdayPreviewRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevBirthdayPreviewRow == -1 && birthdayPreviewRow != -1) {
                        listAdapter.notifyItemInserted(birthdayPreviewRow);
                    } else if (prevBirthdayPreviewRow != -1 && birthdayPreviewRow == -1) {
                        listAdapter.notifyItemRemoved(prevBirthdayPreviewRow);
                    }
                }
                // Birth date preview

                // Business preview
                int prevBusinessHoursPreviewRow = businessHoursPreviewRow;
                businessHoursPreviewRow = -1;
                if (CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview()) {
                    businessHoursPreviewRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevBusinessHoursPreviewRow == -1 && businessHoursPreviewRow != -1) {
                        listAdapter.notifyItemInserted(businessHoursPreviewRow);
                    } else if (prevBusinessHoursPreviewRow != -1 && businessHoursPreviewRow == -1) {
                        listAdapter.notifyItemRemoved(prevBusinessHoursPreviewRow);
                    }
                }

                int prevBusinessLocationPreviewRow = businessLocationPreviewRow;
                businessLocationPreviewRow = -1;
                if (CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview()) {
                    businessLocationPreviewRow = rowCount++;
                }
                if (listAdapter != null) {
                    if (prevBusinessLocationPreviewRow == -1 && businessLocationPreviewRow != -1) {
                        listAdapter.notifyItemInserted(businessLocationPreviewRow);
                    } else if (prevBusinessLocationPreviewRow != -1 && businessLocationPreviewRow == -1) {
                        listAdapter.notifyItemRemoved(prevBusinessLocationPreviewRow);
                    }
                }
                // Business preview

                profilePreviewDivisorRow = rowCount++;
                headerRow = rowCount++;
                channelPreviewSwitchRow = rowCount++;
                showDcIdSwitchRow = rowCount++;
                birthdayPreviewSwitchRow = rowCount++;
                businessPreviewSwitchRow = rowCount++;
                premiumStatusSwitchRow = rowCount++;
                if (listAdapter != null) {
                    listAdapter.notifyItemRemoved(premiumStatusSwitchRow);
                    listAdapter.notifyItemInserted(premiumStatusSwitchRow);
                }
                profileBackgroundSwitchRow = rowCount++;
                profileEmojiSwitchRow = rowCount++;
            }
        }

        private void updateMessages() {
            if (messagesCellPreview != null) {
                ChatMessageCell[] cells = messagesCellPreview.getCells();
                for (ChatMessageCell cell : cells) {
                    if (cell != null) {
                        MessageObject msg = cell.getMessageObject();
                        if (msg != null) {
                            msg.overrideLinkColor = selectedColor;
                            msg.overrideLinkEmoji = selectedEmoji;
                            cell.setAvatar(msg);

                            if (cell.currentNameStatusDrawable == null) {
                                cell.currentNameStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, dp(26));
                            }
                            if (!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses()) {
                                Long emojiStatusId = UserObject.getEmojiStatusDocumentId(getUserConfig().getCurrentUser().emoji_status);
                                if (emojiStatusId != null) {
                                    cell.currentNameStatusDrawable.set(emojiStatusId, false);
                                    cell.currentNameStatusDrawable.play();
                                } else {
                                    cell.currentNameStatusDrawable.set(PremiumGradient.getInstance().premiumStarDrawableMini, false);
                                }
                            } else {
                                cell.currentNameStatusDrawable.set((Drawable) null, false);
                            }
                            cell.invalidate();
                        }
                    }
                }
            }
        }

        public void updateColors() {
            listView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
            if (type == PAGE_PROFILE && colorBar != null) {
                colorBar.setColor(currentAccount, selectedColor, true);
            }
            if (messagesCellPreview != null) {
                messagesCellPreview.invalidate();
            }
            if (profilePreview != null) {
                profilePreview.setColor(selectedColor, false);
                AndroidUtilities.forEachViews(listView, view -> {
                    if (view instanceof ProfileChannelCell) {
                        ((ProfileChannelCell) view).updateColors();
                    }
                });
            }
        }

    }

    private Theme.ResourcesProvider parentResourcesProvider;
    private final SparseIntArray currentColors = new SparseIntArray();
    private final Theme.MessageDrawable msgInDrawable, msgInDrawableSelected;

    public MessagesAndProfilesPreferencesEntry() {
        super();

        resourceProvider = new Theme.ResourcesProvider() {
            @Override
            public int getColor(int key) {
                int index = currentColors.indexOfKey(key);
                if (index >= 0) {
                    return currentColors.valueAt(index);
                }
                if (parentResourcesProvider != null) {
                    return parentResourcesProvider.getColor(key);
                }
                return Theme.getColor(key);
            }

            @Override
            public Drawable getDrawable(String drawableKey) {
                if (drawableKey.equals(Theme.key_drawable_msgIn)) {
                    return msgInDrawable;
                }
                if (drawableKey.equals(Theme.key_drawable_msgInSelected)) {
                    return msgInDrawableSelected;
                }
                if (parentResourcesProvider != null) {
                    return parentResourcesProvider.getDrawable(drawableKey);
                }
                return Theme.getThemeDrawable(drawableKey);
            }

            @Override
            public Paint getPaint(String paintKey) {
                return Theme.ResourcesProvider.super.getPaint(paintKey);
            }
        };
        msgInDrawable = new Theme.MessageDrawable(Theme.MessageDrawable.TYPE_TEXT, false, false, resourceProvider);
        msgInDrawableSelected = new Theme.MessageDrawable(Theme.MessageDrawable.TYPE_TEXT, false, true, resourceProvider);
    }

    @Override
    public void setResourceProvider(Theme.ResourcesProvider resourceProvider) {
        parentResourcesProvider = resourceProvider;
    }

    private boolean startAtProfile;
    public MessagesAndProfilesPreferencesEntry startOnProfile() {
        this.startAtProfile = true;
        return this;
    }

    @Override
    public boolean onFragmentCreate() {
        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int tag) {
                return dp(62);
            }

            @Override
            public boolean clipWithGradient(int tag) {
                return true;
            }
        });
        getMediaDataController().loadReplyIcons();
        if (getMessagesController().peerColors == null && BuildVars.DEBUG_PRIVATE_VERSION) {
            getMessagesController().loadAppConfig(true);
        }
        return super.onFragmentCreate();
    }

    private ViewPagerFixed viewPager;
    private ImageView backButton;
    private FrameLayout actionBarContainer;
    private FilledTabsView tabsView;

    @Override
    public View createView(Context context) {
        messagePage = new Page(context, PAGE_MESSAGE);
        profilePage = new Page(context, PAGE_PROFILE);

        actionBar.setCastShadows(false);
        actionBar.setVisibility(View.GONE);
        actionBar.setAllowOverlayTitle(false);

        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (actionBarContainer != null) {
                    actionBarContainer.getLayoutParams().height = ActionBar.getCurrentActionBarHeight();
                    ((MarginLayoutParams) actionBarContainer.getLayoutParams()).topMargin = AndroidUtilities.statusBarHeight;
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        frameLayout.setFitsSystemWindows(true);

        colorBar = new ColoredActionBar(context, resourceProvider) {
            @Override
            protected void onUpdateColor() {
                updateLightStatusBar();
                updateActionBarButtonsColor();
                if (tabsView != null) {
                    tabsView.setBackgroundColor(getTabsViewBackgroundColor());
                }
            }

            private int lastBtnColor = 0;
            public void updateActionBarButtonsColor() {
                final int btnColor = getActionBarButtonColor();
                if (lastBtnColor != btnColor) {
                    if (backButton != null) {
                        lastBtnColor = btnColor;
                        backButton.setColorFilter(new PorterDuffColorFilter(btnColor, PorterDuff.Mode.SRC_IN));
                    }
                }
            }
        };
        if (profilePage != null) {
            colorBar.setColor(currentAccount, profilePage.selectedColor, false);
        }
        frameLayout.addView(colorBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        viewPager = new ViewPagerFixed(context) {
            @Override
            public void onTabAnimationUpdate(boolean manual) {
                tabsView.setSelected(viewPager.getPositionAnimated());
                colorBar.setProgressToGradient(viewPager.getPositionAnimated());
            }
        };
        viewPager.setAdapter(new ViewPagerFixed.Adapter() {
            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            public View createView(int viewType) {
                if (viewType == PAGE_MESSAGE) return messagePage;
                if (viewType == PAGE_PROFILE) return profilePage;
                return null;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public void bindView(View view, int position, int viewType) {

            }
        });
        frameLayout.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        actionBarContainer = new FrameLayout(context);
        frameLayout.addView(actionBarContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        tabsView = new FilledTabsView(context);
        tabsView.setTabs(
                getString(R.string.Message),
                getString(R.string.UserColorTabProfile)
        );
        tabsView.onTabSelected(tab -> {
            if (viewPager != null) {
                viewPager.scrollToPosition(tab);
            }
        });
        actionBarContainer.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.CENTER));

        if (startAtProfile) {
            viewPager.setPosition(1);
            if (tabsView != null) {
                tabsView.setSelected(1);
            }
            if (colorBar != null) {
                colorBar.setProgressToGradient(1f);
                updateLightStatusBar();
            }
        }

        backButton = new ImageView(context);
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_actionBarWhiteSelector), Theme.RIPPLE_MASK_CIRCLE_20DP));
        backButton.setImageResource(R.drawable.ic_ab_back);
        backButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        backButton.setOnClickListener(v -> {
            if (onBackPressed(true)) {
                finishFragment();
            }
        });
        actionBarContainer.addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        colorBar.updateColors();

        fragmentView = contentView = frameLayout;

        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("msgs_and_profiles_prefs");

        return contentView;
    }

    @Override
    public void onFragmentClosed() {
        super.onFragmentClosed();
        Bulletin.removeDelegate(this);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return SimpleThemeDescription.createThemeDescriptions(this::updateColors,
                Theme.key_windowBackgroundWhite,
                Theme.key_windowBackgroundWhiteBlackText,
                Theme.key_windowBackgroundWhiteGrayText2,
                Theme.key_listSelector,
                Theme.key_windowBackgroundGray,
                Theme.key_windowBackgroundWhiteGrayText4,
                Theme.key_text_RedRegular,
                Theme.key_windowBackgroundChecked,
                Theme.key_windowBackgroundCheckText,
                Theme.key_switchTrackBlue,
                Theme.key_switchTrackBlueChecked,
                Theme.key_switchTrackBlueThumb,
                Theme.key_switchTrackBlueThumbChecked
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateColors() {
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        messagePage.updateColors();
        profilePage.updateColors();
        if (colorBar != null) {
            colorBar.updateColors();
        }
        setNavigationBarColor(getNavigationBarColor());
    }

    private static class ColoredActionBar extends View {

        private int defaultColor;
        private final Theme.ResourcesProvider resourcesProvider;

        public ColoredActionBar(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            defaultColor = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
            setColor(-1, -1, false);
        }

        public void setColor(int currentAccount, int colorId, boolean animated) {
            MessagesController.PeerColor peerColor = null;
            if (colorId >= 0 && currentAccount >= 0) {
                MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            }
            setColor(peerColor, animated);
        }

        public void setColor(MessagesController.PeerColor peerColor, boolean animated) {
            isDefault = false;
            if (peerColor == null) {
                isDefault = true;
                color1 = color2 = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
            } else {
                final boolean isDark = resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark();
                color1 = peerColor.getBgColor1(isDark);
                color2 = peerColor.getBgColor2(isDark);
            }
            if (!animated) {
                color1Animated.set(color1, true);
                color2Animated.set(color2, true);
            }
            invalidate();
        }

        private float progressToGradient = 0;
        public void setProgressToGradient(float progress) {
            if (Math.abs(progressToGradient - progress) > 0.001f) {
                progressToGradient = progress;
                onUpdateColor();
                invalidate();
            }
        }

        public boolean isDefault;
        public int color1, color2;
        private final AnimatedColor color1Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final AnimatedColor color2Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

        private int backgroundGradientColor1, backgroundGradientColor2, backgroundGradientWidth, backgroundGradientHeight;
        private RadialGradient backgroundGradient;
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        protected void onUpdateColor() {

        }

        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            final int color1 = color1Animated.set(this.color1);
            final int color2 = color2Animated.set(this.color2);
            if (backgroundGradient == null || backgroundGradientColor1 != color1 || backgroundGradientColor2 != color2 || backgroundGradientWidth != getWidth() || backgroundGradientHeight != getHeight()) {
                backgroundGradientWidth = getWidth();
                backgroundGradientHeight = getHeight();
                backgroundGradient = new RadialGradient(
                    backgroundGradientWidth / 2f, backgroundGradientHeight * 0.40f,
                    distance(0, 0, backgroundGradientWidth, backgroundGradientHeight) * 0.75f,
                    new int[] { backgroundGradientColor2 = color2, backgroundGradientColor1 = color1 },
                    new float[] { 0, 1 },
                    Shader.TileMode.CLAMP
                );
                backgroundPaint.setShader(backgroundGradient);
                onUpdateColor();
            }
            if (progressToGradient < 1) {
                canvas.drawColor(defaultColor);
            }
            if (progressToGradient > 0) {
                backgroundPaint.setAlpha((int) (0xFF * progressToGradient));
                canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
            }
        }

        protected boolean ignoreMeasure;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, ignoreMeasure ? heightMeasureSpec : MeasureSpec.makeMeasureSpec(AndroidUtilities.statusBarHeight + dp(230), MeasureSpec.EXACTLY));
        }

        public void updateColors() {
            defaultColor = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
            onUpdateColor();
            invalidate();
        }

        public int getColor() {
            return ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefault, resourcesProvider), ColorUtils.blendARGB(color1Animated.get(), color2Animated.get(), .75f), progressToGradient);
        }

        public int getActionBarButtonColor() {
            return ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultIcon, resourcesProvider), isDefault ? Theme.getColor(Theme.key_actionBarDefaultIcon, resourcesProvider) : Color.WHITE, progressToGradient);
        }

        public int getTabsViewBackgroundColor() {
            return (
                ColorUtils.blendARGB(
                    AndroidUtilities.computePerceivedBrightness(Theme.getColor(Theme.key_actionBarDefault, resourcesProvider)) > .721f ?
                        Theme.getColor(Theme.key_actionBarDefaultIcon, resourcesProvider) :
                        Theme.adaptHSV(Theme.getColor(Theme.key_actionBarDefault, resourcesProvider), +.08f, -.08f),
                    AndroidUtilities.computePerceivedBrightness(ColorUtils.blendARGB(color1Animated.get(), color2Animated.get(), .75f)) > .721f ?
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlueIcon, resourcesProvider) :
                        Theme.adaptHSV(ColorUtils.blendARGB(color1Animated.get(), color2Animated.get(), .75f), +.08f, -.08f),
                    progressToGradient
                )
            );
        }
    }

    private class ProfilePreview extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;

        protected final ImageReceiver imageReceiver = new ImageReceiver(this);
        protected final AvatarDrawable avatarDrawable = new AvatarDrawable();
        protected final SimpleTextView titleView, subtitleView;

        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable statusEmoji;
        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);
        private final StoriesUtilities.StoryGradientTools storyGradient = new StoriesUtilities.StoryGradientTools(this, false);

        public ProfilePreview(Context context, int currentAccount, Theme.ResourcesProvider resourcesProvider) {
            super(context);

            this.resourcesProvider = resourcesProvider;

            titleView = new SimpleTextView(context) {
                @Override
                protected void onAttachedToWindow() {
                    super.onAttachedToWindow();
                    statusEmoji.attach();
                }

                @Override
                protected void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    statusEmoji.detach();
                }
            };
            statusEmoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(titleView, dp(24));
            titleView.setLeftDrawableOutside(true);
            titleView.setRightDrawableOutside(true);
            if (!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses()) titleView.setRightDrawable(statusEmoji);
            titleView.setRightDrawableOnClick(v -> statusEmoji.play());
            titleView.setTextColor(0xFFFFFFFF);
            titleView.setTextSize(20);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setWidthWrapContent(true);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 16, 0, 16, 40.33f));

            subtitleView = new SimpleTextView(context);
            subtitleView.setTextSize(14);
            subtitleView.setTextColor(0x80FFFFFF);
            subtitleView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 16, 0, 16, 20.66f));

            imageReceiver.setRoundRadius(dp(96));
            CharSequence title;
            TLRPC.User user = getUserConfig().getCurrentUser();
            title = UserObject.getUserName(user);

            avatarDrawable.setInfo(currentAccount, user);
            imageReceiver.setForUserOrChat(user, avatarDrawable);
            try {
                title = Emoji.replaceEmoji(title, null, false);
            } catch (Exception ignore) {
            }

            titleView.setText(title);
            String tgPremium = CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses() ? " | TG Premium" : "";
            subtitleView.setText(getString(R.string.Online) + tgPremium);

            setWillNotDraw(false);
        }

        public void overrideAvatarColor(int colorId) {
            final int color1, color2;
            if (colorId >= 14) {
                MessagesController.PeerColors peerColors = getMessagesController().peerColors;
                MessagesController.PeerColor peerColor = peerColors != null ? peerColors.getColor(colorId) : null;
                if (peerColor != null) {
                    final int peerColorValue = peerColor.getColor1();
                    color1 = getThemedColor(Theme.keys_avatar_background[AvatarDrawable.getPeerColorIndex(peerColorValue)]);
                    color2 = getThemedColor(Theme.keys_avatar_background2[AvatarDrawable.getPeerColorIndex(peerColorValue)]);
                } else {
                    color1 = getThemedColor(Theme.keys_avatar_background[AvatarDrawable.getColorIndex(colorId)]);
                    color2 = getThemedColor(Theme.keys_avatar_background2[AvatarDrawable.getColorIndex(colorId)]);
                }
            } else {
                color1 = getThemedColor(Theme.keys_avatar_background[AvatarDrawable.getColorIndex(colorId)]);
                color2 = getThemedColor(Theme.keys_avatar_background2[AvatarDrawable.getColorIndex(colorId)]);
            }
            avatarDrawable.setColor(color1, color2);
            invalidate();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            emoji.attach();
            imageReceiver.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            emoji.detach();
            imageReceiver.onDetachedFromWindow();
        }

        private int getThemedColor(int key) {
            return Theme.getColor(key, resourcesProvider);
        }

        private MessagesController.PeerColor peerColor;
        public void setColor(int colorId, boolean animated) {
            MessagesController.PeerColors peerColors = getMessagesController().profilePeerColors;
            MessagesController.PeerColor peerColor = peerColors == null ? null : peerColors.getColor(colorId);
            setColor(peerColor, animated);
        }

        public void setColor(MessagesController.PeerColor peerColor, boolean animated) {
            this.peerColor = peerColor;
            final boolean isDark = resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark();
            if (peerColor != null) {
                if (peerColor.patternColor != 0) {
                    emoji.setColor(peerColor.patternColor);
                } else {
                    emoji.setColor(adaptProfileEmojiColor(peerColor.getBgColor1(isDark)));
                }
                statusEmoji.setColor(ColorUtils.blendARGB(peerColor.getStoryColor1(Theme.isCurrentThemeDark()), 0xFFFFFFFF, 0.25f));
                final int accentColor = ColorUtils.blendARGB(peerColor.getStoryColor1(isDark), peerColor.getStoryColor2(isDark), .5f);
                if (!Theme.hasHue(getThemedColor(Theme.key_actionBarDefault))) {
                    subtitleView.setTextColor(accentColor);
                } else {
                    subtitleView.setTextColor(Theme.changeColorAccent(getThemedColor(Theme.key_actionBarDefault), accentColor, getThemedColor(Theme.key_avatar_subtitleInProfileBlue), isDark, accentColor));
                }
                titleView.setTextColor(Color.WHITE);
            } else {
                if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                    emoji.setColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueText));
                } else if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                    emoji.setColor(Theme.multAlpha(getThemedColor(Theme.key_actionBarDefaultTitle), .5f));
                } else {
                    emoji.setColor(adaptProfileEmojiColor(getThemedColor(Theme.key_actionBarDefault)));
                }
                statusEmoji.setColor(Theme.getColor(Theme.key_profile_verifiedBackground, resourcesProvider));
                subtitleView.setTextColor(getThemedColor(Theme.key_actionBarDefaultSubtitle));
                titleView.setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
            }

            storyGradient.setColor(peerColor, animated);
            invalidate();
        }

        public void setEmoji(long docId, boolean animated) {
            if (docId == 0) {
                emoji.set((Drawable) null, animated);
            } else {
                emoji.set(docId, animated);
            }
            final boolean isDark = resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark();
            if (peerColor != null) {
                if (peerColor.patternColor != 0) {
                    emoji.setColor(peerColor.patternColor);
                } else {
                    emoji.setColor(adaptProfileEmojiColor(peerColor.getBgColor1(isDark)));
                }
            } else if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                emoji.setColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueText));
            } else if (AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                emoji.setColor(Theme.multAlpha(Theme.getColor(Theme.key_actionBarDefaultTitle), .5f));
            } else {
                emoji.setColor(adaptProfileEmojiColor(Theme.getColor(Theme.key_actionBarDefault)));
            }
            if (peerColor != null) {
                statusEmoji.setColor(ColorUtils.blendARGB(peerColor.getColor(1, resourcesProvider), peerColor.hasColor6(isDark) ? peerColor.getColor(4, resourcesProvider) : peerColor.getColor(2, resourcesProvider), .5f));
            } else {
                statusEmoji.setColor(Theme.getColor(Theme.key_profile_verifiedBackground, resourcesProvider));
            }
            invalidate();
        }

        private final RectF rectF = new RectF();
        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            rectF.set(
                (getWidth() - dp(86)) / 2f,
                getHeight() - dp(82 + 86),
                (getWidth() + dp(86)) / 2f,
                getHeight() - dp(82)
            );
            imageReceiver.setRoundRadius(dp(54));
            imageReceiver.setImageCoords(rectF);
            imageReceiver.draw(canvas);

            final float r = rectF.width() / 2f + dp(4);
            final float rr = dp(58);
            canvas.drawRoundRect(
                rectF.centerX() - r,
                rectF.centerY() - r,
                rectF.centerX() + r,
                rectF.centerY() + r,
                rr, rr,
                storyGradient.getPaint(rectF)
            );

            StarGiftPatterns.drawProfileAnimatedPattern(
                canvas,
                emoji,
                getWidth(),
                getHeight(),
                1.0f,
                rectF,
                1.0f
            );

            super.dispatchDraw(canvas);
        }
    }

    public int adaptProfileEmojiColor(int color) {
        final boolean isDark = AndroidUtilities.computePerceivedBrightness(color) < .2f;
        return Theme.adaptHSV(color, +.5f, isDark ? +.28f : -.28f);
    }

    private String birthdayString() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 15);
        return LocaleController.getInstance().getFormatterBoostExpired().format(calendar.getTimeInMillis());
    }

    public int processColor(int color) {
        return color;
    }

    @Override
    public boolean isLightStatusBar() {
        if (colorBar == null) {
            return super.isLightStatusBar();
        }
        return ColorUtils.calculateLuminance(colorBar.getColor()) > 0.7f;
    }

    public void updateLightStatusBar() {
        if (getParentActivity() == null) return;
        AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), isLightStatusBar());
    }

    @Override
    public boolean isActionBarCrossfadeEnabled() {
        return false;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return false; // Breaks status bar
    }

}