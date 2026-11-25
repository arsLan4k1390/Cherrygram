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

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyNewSpan;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.EdgeToEdgeSupportMode;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.ThemePreviewMessagesCell;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilledTabsView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileActionsView;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.SimpleThemeDescription;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.Stories.StoriesUtilities;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.UserInfoActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import uz.unnarsx.cherrygram.Extra;
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MessagesAndProfilesPreferencesEntry extends BaseCGPreferencesEntry {

    private static final int PAGE_MESSAGE = 0;
    private static final int PAGE_PROFILE = 1;

    // Message page
    private final int showForwardDateSwitchRow = 1;
    private final int pencilIconSwitchRow = 2;
    private final int timeWithSecondsSwitchRow = 3;

    private final int premiumStatusSwitchRow = 4;
    private final int replyBackgroundSwitchRow = 5;
    private final int replyColorSwitchRow = 6;
    private final int replyEmojiSwitchRow = 7;

    // Profile page
    private final int channelPreviewRow = 8;
    private final int roundActionsSwitchRow = 9;

    private final int profilePreviewsSectionRow = 10;
    private boolean expandedProfilePreviewsSection = false;
    private final int hidePhoneSwitchRow = 11;
    private final int channelPreviewSwitchRow = 12;
    private final int showDcIdSwitchRow = 13;
    private final int birthdayPreviewSwitchRow = 14;
    private final int businessPreviewSwitchRow = 15;

    private final int premiumSectionRow = 16;
    private boolean expandedPremiumSection = false;
    private final int profileBackgroundSwitchRow = 17;
    private final int newProfileDesignSwitchRow = 18;
    private final int profileEmojiSwitchRow = 19;

    private static final Set<String> PREMIUM_SECTION_SLUG = Set.of(
            "disablePremiumStatuses",
            "profileBackground",
            "newProfileDesign",
            "profileEmoji"
    );

    private static final Set<String> PROFILE_PREVIEWS_SECTION_SLUG = Set.of(
            "hidePhoneNumber",
            "channelPreview",
            "showIdDc",
            "birthdayPreview",
            "businessPreview"
    );

    private ColoredActionBar colorBar;

    private float actionsRowHeight = ProfileActionsView.getRowHeight();
    private ValueAnimator actionsRowAnimator;
    private ViewPagerFixed viewPager;
    private ImageView backButton;
    private FilledTabsView tabsView;

    public Page messagePage;
    public Page profilePage;

    private boolean startAtProfile;
    public MessagesAndProfilesPreferencesEntry startOnProfile() {
        this.startAtProfile = true;
        return this;
    }

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("msgs_and_profiles_prefs");
        return getString(R.string.CP_ProfileReplyBackground);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Messages_And_Profiles;
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

    @Override
    public void onFragmentClosed() {
        super.onFragmentClosed();
        Bulletin.removeDelegate(this);
    }

    @Override
    public View createView(Context context) {
        actionBar.setCastShadows(false);
        actionBar.setVisibility(View.GONE);
        actionBar.setAllowOverlayTitle(false);

        contentView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (actionBarContainer != null) {
                    actionBarContainer.getLayoutParams().height = ActionBar.getCurrentActionBarHeight();
                    ((MarginLayoutParams) actionBarContainer.getLayoutParams()).topMargin = AndroidUtilities.statusBarHeight;
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };

        colorBar = new ColoredActionBar(context, getResourceProvider()) {
            @Override
            protected int getHeaderHeight() {
                return getProfileHeaderHeight();
            }

            @Override
            protected void onUpdateColor() {
                updateLightStatusBar();
                updateActionBarButtonsColor();
                updateTabColors();
                if (profilePage != null) {
                    profilePage.invalidate();
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
        colorBar.newMode = false;
        contentView.addView(colorBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        messagePage = new Page(context, PAGE_MESSAGE);
        profilePage = new Page(context, PAGE_PROFILE);
        colorBar.setColor(profilePage.getSelectedProfilePeerColor(), false);

        viewPager = new ViewPagerFixed(context) {
            @Override
            public void onTabAnimationUpdate(boolean manual) {
                final float position = viewPager.getPositionAnimated();
                tabsView.setSelected(position);
                colorBar.setProgressToGradient(getTabProgressFromPageProgress(position));
                updateTabColors();
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
        contentView.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        actionBarContainer = new FrameLayout(context);
        contentView.addView(actionBarContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

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
        updateTabColors();
        actionBarContainer.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.CENTER));

        if (startAtProfile) {
            viewPager.setPosition(PAGE_PROFILE);
            tabsView.setSelected(PAGE_PROFILE);
            colorBar.setProgressToGradient(1f);
            updateLightStatusBar();
        }

        backButton = new ImageView(context);
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_actionBarWhiteSelector), Theme.RIPPLE_MASK_CIRCLE_20DP));
        BackDrawable backDrawable = new BackDrawable(false);
        backDrawable.setShowStick(!CherrygramAppearanceConfig.INSTANCE.getCenterTitle());
        backButton.setImageDrawable(backDrawable);
        backButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        backButton.setOnClickListener(v -> {
            if (onBackPressed(true)) {
                finishFragment();
            }
        });
        actionBarContainer.addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        colorBar.updateColors();

        fragmentView = contentView;
        ViewCompat.setOnApplyWindowInsetsListener(fragmentView, this::onInsetsInternal);

        return contentView;
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == showForwardDateSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setMsgForwardDate(!CherrygramAppearanceConfig.INSTANCE.getMsgForwardDate());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getMsgForwardDate());

            messagePage.messagesCellPreview = messagePage.createMessagesPreview(getContext());
            messagePage.listView.adapter.update(false);
        } else if (item.id == pencilIconSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setShowPencilIcon(!CherrygramAppearanceConfig.INSTANCE.getShowPencilIcon());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getShowPencilIcon());

            messagePage.messagesCellPreview = messagePage.createMessagesPreview(getContext());
            messagePage.listView.adapter.update(false);
        } else if (item.id == timeWithSecondsSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setShowSeconds(!CherrygramAppearanceConfig.INSTANCE.getShowSeconds());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getShowSeconds());

            LocaleController.getInstance().recreateFormatters();
            messagePage.messagesCellPreview = messagePage.createMessagesPreview(getContext());
            messagePage.listView.adapter.update(false);
        } else if (item.id == premiumStatusSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setDisablePremiumStatuses(!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses());

            profilePage.profilePreview.setPremiumStatusVisible(!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses());
            messagePage.updateMessages();
            updateRows(true);
        } else if (item.id == replyBackgroundSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setReplyBackground(!CherrygramAppearanceConfig.INSTANCE.getReplyBackground());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getReplyBackground());

            messagePage.updateMessages();
        } else if (item.id == replyColorSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setReplyCustomColors(!CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors());

            messagePage.setupValues();
            messagePage.updateMessages();
        } else if (item.id == replyEmojiSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setReplyBackgroundEmoji(!CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji());

            messagePage.setupValues();
            messagePage.updateMessages();
        } else if (item.id == channelPreviewRow) {
            Browser.openUrl(getParentActivity(), Constants.CG_CHANNEL_URL);
        } else if (item.id == roundActionsSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setRoundProfileActions(!CherrygramAppearanceConfig.INSTANCE.getRoundProfileActions());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getRoundProfileActions());

            profilePage.updateActionsStyle();
        } else if (item.id == profilePreviewsSectionRow) {
            expandedProfilePreviewsSection = !expandedProfilePreviewsSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == hidePhoneSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileHidePhoneNumber(!CherrygramAppearanceConfig.INSTANCE.getProfileHidePhoneNumber());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileHidePhoneNumber());

            updateRows(true);
        } else if (item.id == channelPreviewSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileChannelPreview(!CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview());

            updateRows(true);
        } else if (item.id == showDcIdSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setShowIDDC(!CherrygramAppearanceConfig.INSTANCE.getShowIDDC());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getShowIDDC());

            updateRows(true);
        } else if (item.id == birthdayPreviewSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileBirthDatePreview(!CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview());

            updateRows(true);
        } else if (item.id == businessPreviewSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileBusinessPreview(!CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview());

            updateRows(true);
        } else if (item.id == premiumSectionRow) {
            expandedPremiumSection = !expandedPremiumSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == profileBackgroundSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileBackgroundColor(!CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor());

            profilePage.setupValues();
            messagePage.updateMessages();
            colorBar.setColor(profilePage.getSelectedProfilePeerColor(), true);
            profilePage.updateProfilePreviewColor(true);
            profilePage.profilePreview.overrideAvatarColor(messagePage.selectedColor);

            if (CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign()) {
                profilePage.updatePalette();
                messagePage.listView.adapter.update(true);
            } else {
                updateRows(true);
            }
        } else if (item.id == newProfileDesignSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setNewProfileDesign(!CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign());

            if (CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign()) {
                CherrygramAppearanceConfig.INSTANCE.setRoundProfileActions(true);
            }

            colorBar.setColor(profilePage.getSelectedProfilePeerColor(), true);
            profilePage.updateProfilePreviewColor(true);
            profilePage.updatePalette();
        } else if (item.id == profileEmojiSwitchRow) {
            CherrygramAppearanceConfig.INSTANCE.setProfileBackgroundEmoji(!CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji());

            profilePage.setupValues();
            messagePage.updateMessages();
            profilePage.updateProfilePreviewEmoji(true);
        }
    }

    @Override
    protected void updateRows(boolean animated) {
        if (messagePage != null) messagePage.listView.adapter.update(animated);
        if (profilePage != null) profilePage.listView.adapter.update(animated);
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        if (PROFILE_PREVIEWS_SECTION_SLUG.contains(slug) && !expandedProfilePreviewsSection) {
            expandedProfilePreviewsSection = true;
            updateRows(false);
        }
        if (PREMIUM_SECTION_SLUG.contains(slug) && !expandedPremiumSection) {
            expandedPremiumSection = true;
            updateRows(false);
        }
        for (Page page : new Page[] { messagePage, profilePage }) {
            if (page == null) continue;
            final int position = page.listView.findPositionByItemSlug(slug);
            if (position == -1) continue;

            if (viewPager.getCurrentPosition() != page.type) {
                viewPager.scrollToPosition(page.type);
            }
            page.listView.highlightRow(() -> {
                ((LinearLayoutManager) page.listView.getLayoutManager()).scrollToPositionWithOffset(position, AndroidUtilities.displaySize.y / 2);
                return position;
            });
            return;
        }
        if (unknown != null) unknown.run();
    }

    private class Page extends FrameLayout {

        private final int type;

        private UniversalRecyclerView listView;
        private ProfilePreview profilePreview;

        private int selectedColor;
        private long selectedEmoji;
        private TLRPC.TL_emojiStatusCollectible selectedEmojiCollectible;
        private TLRPC.TL_peerColorCollectible selectedPeerCollectible;

        private ThemePreviewMessagesCell messagesCellPreview;
        private ProfileChannelCell channelCell;
        private TextDetailCell phoneCell, idDcCell, birthdayCell, businessHoursCell, businessLocationCell;

        private int actionBarHeight;
        private float headerCollapse;

        private Theme.ResourcesProvider pageResourcesProvider;

        public Page(Context context, int type) {
            super(context);
            this.type = type;
            setupValues();

            pageResourcesProvider = createPageResourcesProvider();
            if (type == PAGE_MESSAGE) {
                messagesCellPreview = createMessagesPreview(context);
            } else {
                createProfileCells(context);
            }

            listView = createListView(context);
            addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            if (type == PAGE_PROFILE) {
                profilePreview = new ProfilePreview(context, currentAccount, getResourceProvider());
                if (selectedEmojiCollectible != null) {
                    profilePreview.setStatusEmoji(selectedEmojiCollectible.document_id, true, true);
                }
                updateProfilePreviewEmoji(false);
                updateProfilePreviewColor(false);
                addView(profilePreview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));
            }

            updateColors();
            setWillNotDraw(false);
        }

        private Theme.ResourcesProvider createPageResourcesProvider() {
            final MessagesController.PeerColor tintColor = CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign() ? getSelectedProfilePeerColor() : null;
            if (tintColor == null) {
                return getResourceProvider();
            }
            final ProfileActivity.PeerColorResourcesProvider peerResourcesProvider = new ProfileActivity.PeerColorResourcesProvider(null);
            peerResourcesProvider.setParent(getResourceProvider());
            peerResourcesProvider.setPeerColor(tintColor);
            final ProfileActivity.PeerColorResourcesProvider lightThemeResourcesProvider = new ProfileActivity.PeerColorResourcesProvider(peerResourcesProvider);
            return new Theme.ResourcesProvider() {
                @Override
                public int getColor(int key) {
                    if (key == Theme.key_alwaysGrayDarkTheme) {
                        return 0;
                    }
                    if (isSwitchColorKey(key)) {
                        return lightThemeResourcesProvider.getColor(key);
                    }
                    return peerResourcesProvider.getColor(key);
                }

                @Override
                public Drawable getDrawable(String drawableKey) {
                    return peerResourcesProvider.getDrawable(drawableKey);
                }

                @Override
                public Paint getPaint(String paintKey) {
                    return peerResourcesProvider.getPaint(paintKey);
                }

                @Override
                public boolean hasGradientService() {
                    return peerResourcesProvider.hasGradientService();
                }

                @Override
                public void applyServiceShaderMatrix(int w, int h, float translationX, float translationY) {
                    peerResourcesProvider.applyServiceShaderMatrix(w, h, translationX, translationY);
                }

                @Override
                public boolean isDark() {
                    return peerResourcesProvider.isDark();
                }
            };
        }

        private boolean isSwitchColorKey(int key) {
            return key == Theme.key_switchTrack || key == Theme.key_switchTrackChecked
                    || key == Theme.key_switch2Track || key == Theme.key_switch2TrackChecked
                    || key == Theme.key_switchTrackBlue || key == Theme.key_switchTrackBlueChecked
                    || key == Theme.key_switchTrackBlueThumb || key == Theme.key_switchTrackBlueThumbChecked
                    || key == Theme.key_switchTrackBlueSelector || key == Theme.key_switchTrackBlueSelectorChecked
                    || key == Theme.key_alwaysGray || key == Theme.key_alwaysWhite;
        }

        private boolean isPeerTinted() {
            return pageResourcesProvider != getResourceProvider();
        }

        private void createProfileCells(Context context) {
            channelCell = new ProfileChannelCell(MessagesAndProfilesPreferencesEntry.this, false, true, pageResourcesProvider);
            phoneCell = createDetailCell(context);
            idDcCell = createDetailCell(context);
            birthdayCell = createDetailCell(context);
            businessHoursCell = createDetailCell(context);
            businessLocationCell = createDetailCell(context);
        }

        private UniversalRecyclerView createListView(Context context) {
            final UniversalRecyclerView listView;
            if (!isPeerTinted()) {
                listView = new UniversalRecyclerView(
                        MessagesAndProfilesPreferencesEntry.this,
                        this::fillPageItems,
                        MessagesAndProfilesPreferencesEntry.this::onClick,
                        (item, view, position, x, y) -> handleItemLongClick(this.listView, item, view, position, x, y)
                );
            } else {
                listView = new UniversalRecyclerView(
                        context, currentAccount, getClassGuid(),
                        this::fillPageItems,
                        MessagesAndProfilesPreferencesEntry.this::onClick,
                        (item, view, position, x, y) -> handleItemLongClick(this.listView, item, view, position, x, y),
                        pageResourcesProvider
                );
            }
            listView.adapter.setApplyBackground(false);
            listView.setClipToPadding(false);
            listView.setSections();
            if (type == PAGE_PROFILE) {
                listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        updateHeaderCollapse();
                    }
                });
                listView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> updateHeaderCollapse());
            }
            return listView;
        }

        public void updatePalette() {
            if (type != PAGE_PROFILE) {
                return;
            }
            final Bitmap snapshot = takeSnapshot();

            final UniversalRecyclerView oldListView = listView;
            final Parcelable scrollState = oldListView.getLayoutManager() != null ? oldListView.getLayoutManager().onSaveInstanceState() : null;

            pageResourcesProvider = createPageResourcesProvider();
            createProfileCells(getContext());
            listView = createListView(getContext());
            listView.setPadding(oldListView.getPaddingLeft(), oldListView.getPaddingTop(), oldListView.getPaddingRight(), oldListView.getPaddingBottom());
            if (scrollState != null && listView.getLayoutManager() != null) {
                listView.getLayoutManager().onRestoreInstanceState(scrollState);
            }
            final int index = indexOfChild(oldListView);
            removeView(oldListView);
            addView(listView, index, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            updateColors();
            invalidate();

            fadeOutSnapshot(snapshot);
        }

        public void updateActionsStyle() {
            if (type != PAGE_PROFILE || profilePreview == null) {
                return;
            }
            profilePreview.updateActionsStyle();

            if (actionsRowAnimator != null) {
                actionsRowAnimator.cancel();
            }
            actionsRowAnimator = ValueAnimator.ofFloat(actionsRowHeight, ProfileActionsView.getRowHeight());
            actionsRowAnimator.addUpdateListener(a -> {
                actionsRowHeight = (float) a.getAnimatedValue();
                requestLayout();
                profilePreview.requestLayout();
                profilePreview.invalidate();
                if (colorBar != null) {
                    colorBar.requestLayout();
                }
            });
            actionsRowAnimator.setDuration(320);
            actionsRowAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            actionsRowAnimator.start();
        }

        private Bitmap takeSnapshot() {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return null;
            }
            final Bitmap snapshot = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            draw(new Canvas(snapshot));
            return snapshot;
        }

        private void fadeOutSnapshot(Bitmap snapshot) {
            if (snapshot == null) {
                return;
            }
            final ImageView snapshotView = new ImageView(getContext());
            snapshotView.setImageBitmap(snapshot);
            addView(snapshotView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            snapshotView.animate()
                    .alpha(0f)
                    .setDuration(320)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .withEndAction(() -> {
                        removeView(snapshotView);
                        snapshot.recycle();
                    })
                    .start();
        }

        private final Paint defaultBackgroundPaint = new Paint();

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (type == PAGE_PROFILE && isPeerTinted() && colorBar != null && colorBar.backgroundGradient != null && viewPager != null) {
                final float dx = colorBar.getLeft() - viewPager.getLeft() - getLeft();
                final float dy = colorBar.getY() - viewPager.getTop() - getTop();
                canvas.save();
                canvas.translate(dx, dy);
                if (colorBar.progressToGradient < 1 && !colorBar.newMode) {
                    defaultBackgroundPaint.setColor(colorBar.defaultColor);
                    canvas.drawRect(-dx, -dy, -dx + getWidth(), -dy + getHeight(), defaultBackgroundPaint);
                }
                canvas.drawRect(-dx, -dy, -dx + getWidth(), -dy + getHeight(), colorBar.backgroundPaint);
                canvas.restore();
            }
        }

        private ThemePreviewMessagesCell createMessagesPreview(Context context) {
            final TLRPC.User user = getUserConfig().getCurrentUser();
            ThemePreviewMessagesCell cell = new ThemePreviewMessagesCell(
                    context, parentLayout, user.premium && UserObject.getEmojiId(user) != 0
                            ? ThemePreviewMessagesCell.TYPE_PEER_COLOR : ThemePreviewMessagesCell.TYPE_PEER_COLOR_CHERRY
            );
            cell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            cell.fragment = MessagesAndProfilesPreferencesEntry.this;
            for (ChatMessageCell messageCell : cell.getCells()) {
                final MessageObject msg = messageCell.getMessageObject();
                if (msg == null) continue;
                msg.messageOwner.flags |= TLRPC.MESSAGE_FLAG_EDITED;
                msg.messageOwner.edit_date = msg.messageOwner.date;

                msg.messageOwner.flags |= TLRPC.MESSAGE_FLAG_FWD;
                msg.messageOwner.fwd_from = new TLRPC.TL_messageFwdHeader();
                msg.messageOwner.fwd_from.flags |= 1;
                msg.messageOwner.fwd_from.from_id = new TLRPC.TL_peerUser();
                msg.messageOwner.fwd_from.from_id.user_id = getUserConfig().getClientUserId();
                msg.messageOwner.fwd_from.date = msg.messageOwner.date - 60 * 60 * 24;
                msg.forceUpdate = true;
                messageCell.setMessageObject(msg, null, false, false, false);
            }
            return cell;
        }

        private TextDetailCell createDetailCell(Context context) {
            TextDetailCell cell = new TextDetailCell(context, pageResourcesProvider, true, true);
            cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite, pageResourcesProvider));
            cell.setContentDescriptionValueFirst(true);
            return cell;
        }

        private void setupValues() {
            final TLRPC.User user = getUserConfig().getCurrentUser();

            if (type == PAGE_PROFILE) {
                selectedEmojiCollectible = user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible
                        ? (TLRPC.TL_emojiStatusCollectible) user.emoji_status : null;
                selectedPeerCollectible = user != null && user.color instanceof TLRPC.TL_peerColorCollectible
                        ? (TLRPC.TL_peerColorCollectible) user.color : null;

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
                selectedEmojiCollectible = null;
                selectedPeerCollectible = null;

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
            if (selectedEmojiCollectible != null || selectedPeerCollectible != null) {
                selectedColor = -1;
                selectedEmoji = 0;
            }
        }

        private void fillPageItems(ArrayList<UItem> items, UniversalAdapter adapter) {
            if (type == PAGE_MESSAGE) {
                fillMessageItems(items);
            } else {
                fillProfileItems(items);
            }
        }

        private void fillMessageItems(ArrayList<UItem> items) {
            items.add(SettingsHelper.asCustomWithBackground(messagesCellPreview));
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(getString(R.string.CP_CustomizeMessage)));
            items.add(SettingsHelper.asSwitchCG(showForwardDateSwitchRow, getString(R.string.CP_ForwardMsgDate))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getMsgForwardDate())
                    .slug("showForwardDate")
            );
            items.add(SettingsHelper.asSwitchCG(pencilIconSwitchRow, getString(R.string.AP_ShowPencilIcon))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getShowPencilIcon())
                    .slug("showPencilIcon")
            );
            items.add(SettingsHelper.asSwitchCG(timeWithSecondsSwitchRow, getString(R.string.CP_ShowSeconds), getString(R.string.CP_ShowSeconds_Desc))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getShowSeconds())
                    .slug("showSeconds")
            );
            items.add(UItem.asShadow(null));

            items.add(SettingsHelper.asSwitchCG(premiumStatusSwitchRow, getString(R.string.CP_DisablePremiumStatuses), getString(R.string.CP_DisablePremiumStatuses_Desc))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses())
                    .slug("disablePremiumStatuses")
            );
            items.add(SettingsHelper.asSwitchCG(replyBackgroundSwitchRow, getString(R.string.CP_ReplyBackground))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyBackground())
                    .slug("replyBackground")
            );
            items.add(SettingsHelper.asSwitchCG(replyColorSwitchRow, getString(R.string.CP_ReplyCustomColors))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyCustomColors())
                    .slug("replyColors")
            );
            items.add(SettingsHelper.asSwitchCG(replyEmojiSwitchRow, getString(R.string.CP_ReplyBackgroundEmoji))
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getReplyBackgroundEmoji())
                    .slug("replyEmoji")
            );
            items.add(UItem.asShadow(null));
        }

        private void fillProfileItems(ArrayList<UItem> items) {
            final TLRPC.User me = getUserConfig().getCurrentUser();

            items.add(UItem.asShadow(null));
            if (CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview()) {
                channelCell.setCherry();
                items.add(UItem.asCustom(channelPreviewRow, channelCell));
                items.add(UItem.asShadow(null));
            }

            if (!CherrygramAppearanceConfig.INSTANCE.getShowIDDC()
                    || CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview()
                    || CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview()
            ) {
                items.add(UItem.asHeader(getString(R.string.Info)));
            }

            if (me != null && !TextUtils.isEmpty(me.phone)) {
                final String formattedPhone = PhoneFormat.getInstance().format("+ " + me.phone);
                final boolean isFragmentPhoneNumber = me.phone.matches("888\\d{8}");
                final CharSequence text = CherrygramAppearanceConfig.INSTANCE.getProfileHidePhoneNumber() && !isFragmentPhoneNumber
                        ? getChatsPasswordHelper().replaceStringToSpoilers(formattedPhone, true)
                        : formattedPhone;
                phoneCell.setTextAndValue(
                        text,
                        isFragmentPhoneNumber ? getString(R.string.AnonymousNumber) : getString(R.string.PhoneMobile) + Extra.INSTANCE.getCountryByPhone(MessagesAndProfilesPreferencesEntry.this, me),
                        false
                );
            }
            items.add(SettingsHelper.asCustomWithBackground(phoneCell));

            if (CherrygramAppearanceConfig.INSTANCE.getShowIDDC()) {
                StringBuilder sb = new StringBuilder();
                sb.append("DC: ");
                if (me.photo != null && me.photo.dc_id > 0) {
                    sb.append(me.photo.dc_id);
                    sb.append(", ");
                    sb.append(CGResourcesHelper.INSTANCE.getDCName(me.photo.dc_id));
                    sb.append(", ");
                    sb.append(CGResourcesHelper.INSTANCE.getDCGeo(me.photo.dc_id));
                } else {
                    sb.append(getString(R.string.NumberUnknown));
                }
                DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US) {{ setGroupingSeparator(' '); }});
                idDcCell.setTextAndValue("ID: " + df.format(me.id), sb, false);

                setTintedImage(idDcCell, R.drawable.msg_calendar2);
                idDcCell.setImageClickListener(v -> showCreationDateHint(
                        idDcCell.getImageView(),
                        Extra.INSTANCE.getRegistrationDate(MessagesAndProfilesPreferencesEntry.this, getUserConfig().getClientUserId(), 0)
                ));
                items.add(SettingsHelper.asCustomWithBackground(idDcCell));
            }

            if (CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview()) {
                final TLRPC.UserFull meFull = getMessagesController().getUserFull(me.id);
                final String date;
                if (meFull != null && meFull.birthday != null) {
                    date = UserInfoActivity.birthdayString(meFull.birthday);
                } else {
                    final int age = Period.between(LocalDate.of(2022, 1, 15), LocalDate.now()).getYears();
                    date = LocaleController.formatPluralString("ProfileBirthdayValueYear", age, birthdayString());
                }
                birthdayCell.setTextAndValue(date, getString(R.string.ProfileBirthday), false);
                birthdayCell.textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, pageResourcesProvider));

                setTintedImage(birthdayCell, R.drawable.input_calendar_add_solar);
                birthdayCell.setImageClickListener(v -> Extra.INSTANCE.addBirthdayToCalendar(getParentActivity(), getUserConfig().getCurrentUser().id));
                items.add(SettingsHelper.asCustomWithBackground(birthdayCell));
            }

            if (CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview()) {
                businessHoursCell.textView.setTextColor(Theme.getColor(Theme.key_avatar_nameInMessageGreen, pageResourcesProvider));
                businessHoursCell.setTextAndValue(getString(R.string.BusinessHoursProfileNowOpen), getString(R.string.BusinessHoursProfile), false);
                items.add(SettingsHelper.asCustomWithBackground(businessHoursCell));

                businessLocationCell.textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, pageResourcesProvider));
                businessLocationCell.setTextAndValue("Cherrygram, Abu-Dhabi", getString(R.string.BusinessProfileLocation), false);
                items.add(SettingsHelper.asCustomWithBackground(businessLocationCell));
            }
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(getString(R.string.CP_CustomizeProfile)));
            items.add(
                    SettingsHelper.asSwitchCG(
                            roundActionsSwitchRow,
                            applyNewSpan(getString(R.string.CP_RoundProfileActions)),
                            getString(R.string.CP_RoundProfileActions_Desc)
                    )
                    .setChecked(CherrygramAppearanceConfig.INSTANCE.getRoundProfileActions())
                    .slug("roundProfileActions")
            );
            items.add(UItem.asShadow(null));
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            profilePreviewsSectionRow,
                            R.drawable.msg_info,
                            getString(R.string.Info),
                            " "
                    )
                    .setCollapsed(!expandedProfilePreviewsSection)
                    .setClickCallback(v -> {
                        expandedProfilePreviewsSection = !expandedProfilePreviewsSection;
                        updateRows(true);
                    })
                    .hideCheckbox(true)
                    .slug("profilePreviews")
            );
            if (expandedProfilePreviewsSection) {
                items.add(SettingsHelper.asSwitchCG(hidePhoneSwitchRow, getString(R.string.CP_ProfileHidePhoneNumbers))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileHidePhoneNumber())
                        .slug("hidePhoneNumber")
                );
                items.add(SettingsHelper.asSwitchCG(channelPreviewSwitchRow, getString(R.string.CP_ProfileChannelPreview))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileChannelPreview())
                        .slug("channelPreview")
                );
                items.add(SettingsHelper.asSwitchCG(showDcIdSwitchRow, getString(R.string.AP_ShowID))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getShowIDDC())
                        .slug("showIdDc")
                );
                items.add(SettingsHelper.asSwitchCG(birthdayPreviewSwitchRow, getString(R.string.CP_ProfileBirthDatePreview))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBirthDatePreview())
                        .slug("birthdayPreview")
                );
                items.add(SettingsHelper.asSwitchCG(businessPreviewSwitchRow, getString(R.string.CP_ProfileBusinessPreview))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBusinessPreview())
                        .slug("businessPreview")
                );
            }
            items.add(UItem.asShadow(null));

            items.add(
                    SettingsHelper.asExpandableSwitch(
                            premiumSectionRow,
                            R.drawable.menu_feature_premium,
                            getString(R.string.TelegramPremium),
                            " "
                    )
                    .setCollapsed(!expandedPremiumSection)
                    .setClickCallback(v -> {
                        expandedPremiumSection = !expandedPremiumSection;
                        updateRows(true);
                    })
                    .hideCheckbox(true)
                    .slug("premiumSection")
            );
            if (expandedPremiumSection) {
                items.add(SettingsHelper.asSwitchCG(premiumStatusSwitchRow, getString(R.string.CP_DisablePremiumStatuses), getString(R.string.CP_DisablePremiumStatuses_Desc))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses())
                        .slug("disablePremiumStatuses")
                );
                items.add(SettingsHelper.asSwitchCG(profileBackgroundSwitchRow, getString(R.string.CP_ProfileBackgroundColor))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor())
                        .slug("profileBackground")
                );
                if (CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor()) {
                    items.add(
                            SettingsHelper.asSwitchCG(
                                    newProfileDesignSwitchRow,
                                    applyNewSpan(getString(R.string.CG_NewProfileDesign)),
                                    getString(R.string.CG_NewProfileDesign_Desc)
                            )
                            .setChecked(CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign())
                            .slug("newProfileDesign")
                    );
                }
                items.add(SettingsHelper.asSwitchCG(profileEmojiSwitchRow, getString(R.string.CP_ProfileBackgroundEmoji))
                        .setChecked(CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji())
                        .slug("profileEmoji")
                );
            }
            items.add(UItem.asShadow(null));
        }

        private void setTintedImage(TextDetailCell cell, int resId) {
            Drawable drawable = ContextCompat.getDrawable(cell.getContext(), resId);
            cell.setImage(drawable);
            if (drawable != null && colorBar != null && colorBar.getActionBarButtonColor() != 0) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(pageResourcesProvider != getResourceProvider() ? Theme.key_actionBarDefaultIcon : Theme.key_windowBackgroundWhiteBlueHeader, pageResourcesProvider), PorterDuff.Mode.MULTIPLY));
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (type == PAGE_MESSAGE) {
                actionBarHeight = ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight;
                ((MarginLayoutParams) listView.getLayoutParams()).topMargin = actionBarHeight;
            } else {
                actionBarHeight = getProfileHeaderHeight() + AndroidUtilities.statusBarHeight;
                profilePreview.getLayoutParams().height = actionBarHeight;
                if (listView.getPaddingTop() != actionBarHeight) {
                    listView.setPadding(listView.getPaddingLeft(), actionBarHeight, listView.getPaddingRight(), listView.getPaddingBottom());
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected boolean drawChild(@NonNull Canvas canvas, View child, long drawingTime) {
            if (child == listView && type == PAGE_PROFILE) {
                canvas.save();
                canvas.clipRect(0, actionBarHeight - headerCollapse, getWidth(), getHeight());
                final boolean result = super.drawChild(canvas, child, drawingTime);
                canvas.restore();
                return result;
            }
            return super.drawChild(canvas, child, drawingTime);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (type == PAGE_PROFILE && ev.getAction() == MotionEvent.ACTION_DOWN
                    && headerCollapse > 0 && headerCollapse >= getHeaderCollapseRange()
                    && ev.getY() < actionBarHeight - headerCollapse) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }

        private int getHeaderCollapseRange() {
            return actionBarHeight - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight;
        }

        private void updateHeaderCollapse() {
            final int range = getHeaderCollapseRange();
            if (range <= 0 || profilePreview == null) return;

            final View first = listView.layoutManager.findViewByPosition(0);
            final int offset = first != null ? listView.getPaddingTop() - listView.layoutManager.getDecoratedTop(first) : range;
            final float collapse = MathUtils.clamp(offset, 0, range);
            if (collapse == headerCollapse) return;

            headerCollapse = collapse;
            profilePreview.setTranslationY(-collapse);
            profilePreview.setAlpha(1f - collapse / range);
            if (colorBar != null) {
                colorBar.setTranslationY(-collapse);
            }
            invalidate();
        }

        private MessagesController.PeerColor getSelectedProfilePeerColor() {
            if (type != PAGE_PROFILE || !CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor()) {
                return null;
            }
            if (selectedPeerCollectible != null) {
                return MessagesController.PeerColor.fromPeerCollectible(selectedPeerCollectible);
            }
            if (selectedEmojiCollectible != null) {
                return MessagesController.PeerColor.fromCollectible(selectedEmojiCollectible);
            }
            if (selectedColor >= 0) {
                MessagesController.PeerColors peerColors = MessagesController.getInstance(currentAccount).profilePeerColors;
                return peerColors == null ? null : peerColors.getColor(selectedColor);
            }
            return null;
        }

        private void updateProfilePreviewColor(boolean animated) {
            if (profilePreview == null) {
                return;
            }
            profilePreview.setColor(getSelectedProfilePeerColor(), animated);
        }

        private void updateProfilePreviewEmoji(boolean animated) {
            if (profilePreview == null) {
                return;
            }
            if (selectedEmojiCollectible != null) {
                profilePreview.setEmoji(
                        CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji() ? selectedEmojiCollectible.pattern_document_id : 0,
                        true,
                        animated
                );
            } else {
                profilePreview.setEmoji(selectedEmoji, false, animated);
            }
        }

        private void updateMessages() {
            if (messagesCellPreview == null) {
                return;
            }
            final TLRPC.TL_peerColorCollectible previewPeerCollectible = profilePage != null && CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor()
                    ? profilePage.selectedPeerCollectible : null;

            for (ChatMessageCell cell : messagesCellPreview.getCells()) {
                if (cell == null) continue;
                MessageObject msg = cell.getMessageObject();
                if (msg == null) continue;

                msg.overrideLinkEmoji = selectedEmoji;
                msg.overrideLinkPeerColor = previewPeerCollectible;
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

        private void updateColors() {
            listView.setBackgroundColor(isPeerTinted() ? 0 : Theme.getColor(Theme.key_windowBackgroundGray, pageResourcesProvider));
            if (messagesCellPreview != null) {
                messagesCellPreview.invalidate();
            }
            if (type == PAGE_PROFILE) {
                if (colorBar != null) {
                    colorBar.setColor(getSelectedProfilePeerColor(), true);
                }
                updateProfilePreviewColor(false);
                channelCell.updateColors();
            }
        }

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

    private void updateColors() {
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        messagePage.updateColors();
        profilePage.updateColors();
        updateRows(true);
        if (colorBar != null) {
            colorBar.updateColors();
        }
        updateTabColors();
        setNavigationBarColor(getNavigationBarColor());
    }

    private void updateTabColors() {
        if (colorBar == null || tabsView == null) {
            return;
        }
        final int profileBackgroundColor = colorBar.getTabsViewBackgroundColor();
        final float progressP = viewPager == null ? 0f : viewPager.getPositionAnimated();
        final float progress = 1 - getTabProgressFromPageProgress(progressP);
        tabsView.setColors(
                ColorUtils.blendARGB(profileBackgroundColor, getThemedColor(Theme.key_windowBackgroundWhite), progress),
                ColorUtils.blendARGB(Color.WHITE, getThemedColor(Theme.key_windowBackgroundGray), progress),
                ColorUtils.blendARGB(Color.WHITE, getThemedColor(Theme.key_windowBackgroundWhiteGrayText2), progress),
                ColorUtils.blendARGB(profileBackgroundColor, getThemedColor(Theme.key_windowBackgroundWhiteBlackText), progress)
        );
    }

    private float getTabProgressFromPageProgress(float progress) {
        return MathUtils.clamp((progress - 0.333333f) / 0.333333f, 0 ,1);
    }

    private static class ColoredActionBar extends View {

        private int defaultColor;
        private final Theme.ResourcesProvider resourcesProvider;

        public ColoredActionBar(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            defaultColor = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
            setColor(null, false);
        }

        public void setColor(MessagesController.PeerColor peerColor, boolean animated) {
            if (peerColor == null) {
                isDefault = true;
                color1 = color2 = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
            } else {
                isDefault = false;
                final boolean isDark = isPeerColorDark(resourcesProvider);
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
            if (progressToGradient < 1 && !newMode) {
                canvas.drawColor(defaultColor);
            }
            backgroundPaint.setAlpha(newMode ? 255 : (int) (0xFF * progressToGradient));
            canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        }

        protected boolean newMode;
        protected boolean ignoreMeasure;

        protected int getHeaderHeight() {
            return dp(230);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, ignoreMeasure ? heightMeasureSpec : MeasureSpec.makeMeasureSpec(AndroidUtilities.statusBarHeight + getHeaderHeight(), MeasureSpec.EXACTLY));
        }

        public void updateColors() {
            defaultColor = Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider);
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

        private boolean isEmojiCollectible;
        private final AnimatedFloat emojiCollectible = new AnimatedFloat(this, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

        private ProfileActionsView actionsView;

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
            TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
            CharSequence title = UserObject.getUserName(user);

            avatarDrawable.setInfo(currentAccount, user);
            imageReceiver.setForUserOrChat(user, avatarDrawable);

            final long emojiStatusId = user != null ? DialogObject.getEmojiStatusDocumentId(user.emoji_status) : 0;
            try {
                title = Emoji.replaceEmoji(title, null, false);
            } catch (Exception ignore) {
            }

            titleView.setText(title);
            statusEmoji.set(emojiStatusId, false);
            if (!CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses()) titleView.setRightDrawable(statusEmoji);

            String tgPremium = CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses() ? " | TG Premium" : "";
            subtitleView.setText(getString(R.string.Online) + tgPremium);

            actionsView = createActionsView(context);
            addView(actionsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

            setWillNotDraw(false);
        }

        private ProfileActionsView createActionsView(Context context) {
            final ProfileActionsView view = new ProfileActionsView(context, ProfileActionsView.getRowHeight());
            view.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            view.mode = ProfileActionsView.MODE_MY_PROFILE;
            view.beginApplyingActions();
            view.addCameraAction();
            view.addQRButton();
            view.addEditInfo();
            view.addSettings();
            view.commitActions();
            view.setParentExpanded(0f);
            view.updatePosition(0, ProfileActionsView.getRowHeight());
            return view;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            updateActionsColor();
        }

        private int getActionsOffset() {
            return (int) actionsRowHeight - dp(10);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            ((LayoutParams) titleView.getLayoutParams()).bottomMargin = dp(40.33f) + getActionsOffset();
            ((LayoutParams) subtitleView.getLayoutParams()).bottomMargin = dp(20.66f) + getActionsOffset();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void updateActionsStyle() {
            final ProfileActionsView oldView = actionsView;
            oldView.animate().cancel();
            oldView.animate()
                    .alpha(0f)
                    .setDuration(320)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .withEndAction(() -> removeView(oldView))
                    .start();

            actionsView = createActionsView(getContext());
            actionsView.setAlpha(0f);
            addView(actionsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
            updateActionsColor();
            actionsView.animate()
                    .alpha(1f)
                    .setDuration(320)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .start();
        }

        private void updateActionsColor() {
            final boolean isDark = isPeerColorDark(resourcesProvider);
            int btnColor;
            if (peerColor != null) {
                if (peerColor.patternColor != 0) {
                    btnColor = Theme.multAlpha(peerColor.patternColor, .45f);
                } else {
                    btnColor = Theme.multAlpha(adaptProfileEmojiColor(peerColor.getBgColor1(isDark)), .15f);
                }
                if (CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign()) {
                    final float amount = ProfileActivity.PeerColorResourcesProvider.getCardDarkenAmount();
                    btnColor = ColorUtils.compositeColors(Color.argb(Math.round(255 * amount), 0, 0, 0), btnColor);
                }
            } else {
                final int actionBarColor = getThemedColor(Theme.key_actionBarDefault);
                final float brightness = AndroidUtilities.computePerceivedBrightness(actionBarColor);
                if (brightness > .8f) {
                    btnColor = Color.WHITE;
                } else if (brightness < .2f) {
                    btnColor = Theme.multAlpha(Theme.adaptHSV(actionBarColor, +0.02f, +0.25f), .35f);
                } else {
                    btnColor = Theme.multAlpha(adaptProfileEmojiColor(actionBarColor), .15f);
                }
            }
            actionsView.setActionsColor(btnColor, peerColor != null);
            actionsView.invalidate();
        }

        public void setPremiumStatusVisible(boolean visible) {
            titleView.animate().cancel();
            subtitleView.animate().cancel();
            final float titleX = titleView.getX();
            final float subtitleX = subtitleView.getX();

            if (visible) {
                titleView.setRightDrawable(statusEmoji);
                statusEmoji.play();
                subtitleView.setText(getString(R.string.Online));
            } else {
                titleView.setRightDrawable(0);
                subtitleView.setText(getString(R.string.Online) + " | TG Premium");
            }

            addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    removeOnLayoutChangeListener(this);
                    animateFromX(titleView, titleX);
                    animateFromX(subtitleView, subtitleX);
                }
            });
            titleView.requestLayout();
            subtitleView.requestLayout();
        }

        private void animateFromX(View view, float oldX) {
            view.setTranslationX(oldX - view.getLeft());
            view.animate()
                    .translationX(0)
                    .setDuration(320)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .start();
        }

        public void overrideAvatarColor(int colorId) {
            final int color1, color2;
            if (colorId >= 14) {
                MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
                MessagesController.PeerColors peerColors = messagesController != null ? messagesController.peerColors : null;
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

        public void setColor(MessagesController.PeerColor peerColor, boolean animated) {
            this.peerColor = peerColor;
            final boolean isDark = isPeerColorDark(resourcesProvider);
            if (peerColor != null) {
                if (peerColor.patternColor != 0) {
                    emoji.setColor(peerColor.patternColor);
                } else {
                    emoji.setColor(adaptProfileEmojiColor(peerColor.getBgColor1(isDark)));
                }
                statusEmoji.setColor(ColorUtils.blendARGB(peerColor.getStoryColor1(isDark), 0xFFFFFFFF, 0.25f));
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
            updateActionsColor();
            invalidate();
        }

        public void setEmoji(long docId, boolean isCollectible, boolean animated) {
            if (docId == 0) {
                emoji.set((Drawable) null, animated);
            } else {
                emoji.set(docId, animated);
            }
            final boolean isDark = isPeerColorDark(resourcesProvider);
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
            isEmojiCollectible = isCollectible;
            if (!animated) {
                emojiCollectible.force(isEmojiCollectible);
            }
            invalidate();
        }

        public void setStatusEmoji(long docId, boolean isCollectible, boolean animated) {
            statusEmoji.set(docId, animated);
            statusEmoji.setParticles(isCollectible, animated);
            final boolean isDark = isPeerColorDark(resourcesProvider);
            if (peerColor != null) {
                statusEmoji.setColor(ColorUtils.blendARGB(peerColor.getColor2(isDark), peerColor.hasColor6(isDark) ? peerColor.getColor5(isDark) : peerColor.getColor3(isDark), .5f));
            } else {
                statusEmoji.setColor(Theme.getColor(Theme.key_profile_verifiedBackground, resourcesProvider));
            }
        }

        private final RectF rectF = new RectF();
        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            rectF.set(
                (getWidth() - dp(86)) / 2f,
                getHeight() - getActionsOffset() - dp(82 + 86),
                (getWidth() + dp(86)) / 2f,
                getHeight() - getActionsOffset() - dp(82)
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

            super.dispatchDraw(canvas);
        }
    }

    private int getProfileHeaderHeight() {
        return dp(220) + (int) actionsRowHeight;
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

    @Override
    public EdgeToEdgeSupportMode getEdgeToEdgeSupportMode() {
        return EdgeToEdgeSupportMode.FULL;
    }

    @Override
    public boolean drawEdgeNavigationBar() {
        return true;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        if (messagePage == null || profilePage == null) return;
        messagePage.listView.setPadding(left, messagePage.listView.getPaddingTop(), right, bottom);
        profilePage.listView.setPadding(left, profilePage.listView.getPaddingTop(), right, bottom);
    }

    public HintView2 creationDateHint;
    public void showCreationDateHint(View view, CharSequence text) {
        if (TextUtils.isEmpty(text) || getContext() == null || view == null || contentView == null) {
            return;
        }
        final HintView2 oldHint = creationDateHint;
        if (oldHint != null) {
            oldHint.setOnHiddenListener(() -> {
                if (oldHint.getParent() instanceof ViewGroup) {
                    ((ViewGroup) oldHint.getParent()).removeView(oldHint);
                }
            });
            oldHint.hide();
            creationDateHint = null;
        }
        final HintView2 hint = new HintView2(getContext(), HintView2.DIRECTION_TOP)
                .setMultilineText(true)
                .setDuration(5000L)
                .setBgColor(Theme.getColor(Theme.key_undo_background, profilePage.pageResourcesProvider))
                .setTextColor(Theme.getColor(Theme.key_undo_infoColor, profilePage.pageResourcesProvider))
                .setRounding(12f);
        creationDateHint = hint;
        hint.setText(text);
        hint.setMaxWidthPx(HintView2.cutInFancyHalf(hint.getText(), hint.getTextPaint()));
        contentView.addView(hint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 120, Gravity.TOP | Gravity.LEFT, 16, 0, 16, 0));
        contentView.post(() -> {
            float x = 0, y = 0;
            View v = view;
            while (v != null && v != contentView) {
                x += v.getX();
                y += v.getY();
                if (!(v.getParent() instanceof View)) {
                    break;
                }
                v = (View) v.getParent();
            }
            hint.setTranslationY(y + view.getHeight());
            hint.setJointPx(0f, -dp(16) + x + view.getWidth() / 2f);
            hint.show();
        });
    }

    private static boolean isPeerColorDark(Theme.ResourcesProvider resourcesProvider) {
        return CherrygramAppearanceConfig.INSTANCE.getNewProfileDesign() || (resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark());
    }

}