/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.updater;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.helpers.ui.FontHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.preferences.AboutPreferencesEntry;

public class UpdaterBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private final int currentVersionRow = 1;
    private final int buildTypeRow = 2;

    private Runnable stickerUpdateRunnable;
    private static final String STICKER_PACK_NAME = "HotCherry";
    private static final int[] RANDOM_UPDATE_STICKERS = {5, 7, 8, 15, 16, 23, 24, 25, 26, 31, 33};
    private static final int NO_UPDATE_STICKER_NUM = 7;

    private static final int HEADER_ICON_SIZE_DP = 20;
    private static final int HEADER_TEXT_LEFT_PADDING_DP = HEADER_ICON_SIZE_DP + 2;

    private static final String CHANGELOG_BLOCK_SEPARATOR = "\n\n";

    private BaseFragment fragment;
    private Theme.ResourcesProvider resourcesProvider;
    private ButtonWithCounterView checkUpdatesButton;
    private ButtonWithCounterView downloadButton;

    private final TLRPC.TL_help_appUpdate update;

    private final boolean available;
    private boolean isForce = false;

    public UpdaterBottomSheet(BaseFragment fragment, Context context, Theme.ResourcesProvider resourcesProvider, boolean available, TLRPC.TL_help_appUpdate update) {
        super(context, false, resourcesProvider);

        this.available = available;
        this.update = update;

        setOpenNoDelay(true);
        fixNavigationBar();

        if (fragment == null) fragment = LaunchActivity.getSafeLastFragment();

        UniversalRecyclerView listView = new UniversalRecyclerView(fragment, this::fillItems, this::onClick, null) {
            @Override
            protected void onMeasure(int widthSpec, int heightSpec) {
                int maxHeight = (int) (AndroidUtilities.displaySize.y * 0.65F);
                int height = MeasureSpec.getSize(heightSpec);
                if (height > maxHeight) {
                    heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                }
                super.onMeasure(widthSpec, heightSpec);
            }
        };
        listView.setSections();
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider));

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 0);

        if (available) {
            container.addView(addUpdateAvailableHeader(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            container.addView(addDefaultHeder(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        container.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f, Gravity.CENTER, 0, 0, 0, available ? -dp(2) : 0));

        container.addView(createBottomButtonsView(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        setCustomView(container);
    }

    public static void showAlert(BaseFragment fragment, boolean available, TLRPC.TL_help_appUpdate update) {
        UpdaterBottomSheet alert = new UpdaterBottomSheet(fragment, fragment.getContext(), fragment.getResourceProvider(), available, update);
        alert.setFragmentParams(fragment);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
    }

    public void setFragmentParams(BaseFragment fragment) {
        this.fragment = fragment;
        this.resourcesProvider = fragment.getResourceProvider();
    }

    // ========================================================================================
    // Lifecycle
    // ========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addNotificationObservers();
    }

    private void addNotificationObservers() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateLoading);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadFailed);
    }

    private void removeNotificationObservers() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateLoading);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadFailed);
    }

    @Override
    public void onBackPressed() {
        if (!isForce) {
            if (attachedFragment == null) {
                super.onBackPressed();
            } else {
                dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        if (!isForce) {
            if (stickerUpdateRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(stickerUpdateRunnable);
            }
            removeNotificationObservers();
            super.dismiss();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileLoadProgressChanged || id == NotificationCenter.appUpdateLoading) {
            updateFileProgress(args);
        } else if (id == NotificationCenter.fileLoaded || id == NotificationCenter.fileLoadFailed) {
            String path = (String) args[0];
            if (SharedConfig.isAppUpdateAvailable()) {
                String name = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (name.equals(path) && downloadButton != null) {
                    downloadButton.setClickable(true);
                    downloadButton.setText(getDownloadButtonText(), true);
                }
            }
        }
    }

    public void updateFileProgress(Object[] args) {
        if (downloadButton == null || args == null) return;
        if (SharedConfig.isAppUpdateAvailable()) {
            String location = (String) args[0];
            String fileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
            if (fileName != null && fileName.equals(location)) {
                Long loadedSize = (Long) args[1];
                Long totalSize = (Long) args[2];
                float loadProgress = loadedSize / (float) totalSize;
                downloadButton.setText(LocaleController.formatString(R.string.AppUpdateDownloading, (int) (loadProgress * 100)), true);
            }
        }
    }

    // ========================================================================================
    // List content
    // ========================================================================================

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (available) {
            addChangelogSection(items);
            applyForceUpdateFlagsIfNeeded();
        } else {
            addSettingsSection(items);
        }
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == currentVersionRow) {
            copyText(getString(R.string.UP_CurrentVersion) + ": " + CGResourcesHelper.getCherryVersion());
        } else if (item.id == buildTypeRow) {
            final String bType = CGResourcesHelper.getBuildType() + " | " + CGResourcesHelper.getAbiCode();
            copyText(getString(R.string.UP_BuildType) + ": " + bType);
        }
    }

    // ----------------------------------------------------------------------------------------
    // Header (icon + title + subtitle)
    // ----------------------------------------------------------------------------------------

    private FrameLayout createHeaderContainer() {
        FrameLayout header = new FrameLayout(getContext());
        header.setPadding(dp(12), 0, dp(12), 0);
        return header;
    }

    private View addUpdateAvailableHeader() {
        setCanDismissWithSwipe(false);
        setCanDismissWithTouchOutside(false);

        FrameLayout header = createHeaderContainer();
        header.setPadding(dp(12), 0, 0, available ? dp(8) : 0);
        header.addView(createUpdateStickerView(), LayoutHelper.createFrame(dp(HEADER_ICON_SIZE_DP), dp(HEADER_ICON_SIZE_DP), Gravity.LEFT | Gravity.CENTER_VERTICAL));

        SimpleTextView nameView = createHeaderTitleView(getString(R.string.UP_UpdateAvailable));
        header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(10), Gravity.LEFT, dp(HEADER_TEXT_LEFT_PADDING_DP), dp(3), 0, 0));

        String subtitle = getString(R.string.CG_AppName) + " " + update.build_flavor + " " + update.version + " | " + update.release_date;
        header.addView(createHeaderSubtitleView(subtitle), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(5), Gravity.LEFT, dp(HEADER_TEXT_LEFT_PADDING_DP), dp(13), 0, 0));

        return header;
    }

    private View addDefaultHeder() {
        FrameLayout header = createHeaderContainer();
        header.addView(createStaticStickerView(NO_UPDATE_STICKER_NUM), LayoutHelper.createFrame(dp(HEADER_ICON_SIZE_DP), dp(HEADER_ICON_SIZE_DP), Gravity.LEFT | Gravity.CENTER_VERTICAL));

        SimpleTextView titleView = createHeaderTitleView(getString(R.string.UP_Category_Updates));
        header.addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(10), Gravity.LEFT, dp(HEADER_TEXT_LEFT_PADDING_DP), dp(3), 0, 0));

        header.addView(createHeaderSubtitleView(AboutPreferencesEntry.getLastCheckUpdateTime()), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(5), Gravity.LEFT, dp(HEADER_TEXT_LEFT_PADDING_DP), dp(13), 0, 0));

        return header;
    }

    private View createBottomButtonsView() {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout buttonsView = new LinearLayout(getContext());
        buttonsView.setOrientation(LinearLayout.HORIZONTAL);
        buttonsView.setPadding(dp(4), available ? dp(12) : dp(8), dp(4), dp(12));
        buttonsView.setGravity(Gravity.CENTER_VERTICAL);

        if (available) {
            addDownloadButton(buttonsView);
        } else {
            addCheckUpdatesButton(buttonsView);
        }

        addSecondaryButtons(buttonsView);

        container.addView(buttonsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (available && !isForce) {
            container.addView(createScheduleLaterButtonWrapper(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        return container;
    }

    /** Sticker taken from the update payload if present, otherwise a random HotCherry sticker. */
    private View createUpdateStickerView() {
        if (update.sticker == null) {
            return createRandomStickerView();
        }

        BackupImageView imageView = new BackupImageView(getContext());
        SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(update.sticker.thumbs, Theme.key_windowBackgroundGray, 1.0f);
        if (svgThumb != null) {
            imageView.setImage(ImageLocation.getForDocument(update.sticker), "250_250", svgThumb, 0, "update");
        } else {
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(update.sticker.thumbs, 90);
            ImageLocation imageLocation = ImageLocation.getForDocument(thumb, update.sticker);
            imageView.setImage(ImageLocation.getForDocument(update.sticker), "250_250", imageLocation, null, 0, "update");
        }
        return imageView;
    }

    private StickerImageView createRandomStickerView() {
        int randomIndex = new Random().nextInt(RANDOM_UPDATE_STICKERS.length);
        StickerImageView imageView = createStaticStickerView(RANDOM_UPDATE_STICKERS[randomIndex]);

        Runnable changeStickerAction = () -> {
            int newIndex = new Random().nextInt(RANDOM_UPDATE_STICKERS.length);
            imageView.setStickerNum(RANDOM_UPDATE_STICKERS[newIndex]);
            if (imageView.getImageReceiver() != null) {
                imageView.getImageReceiver().setAutoRepeat(1);
            }
        };

        imageView.setOnClickListener(v -> {
            changeStickerAction.run();
            if (stickerUpdateRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(stickerUpdateRunnable);
                AndroidUtilities.runOnUIThread(stickerUpdateRunnable, 5000);
            }
        });

        stickerUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (imageView.isAttachedToWindow()) {
                    changeStickerAction.run();
                    AndroidUtilities.runOnUIThread(this, 5000);
                }
            }
        };
        AndroidUtilities.runOnUIThread(stickerUpdateRunnable, 5000);

        return imageView;
    }

    private StickerImageView createStaticStickerView(int stickerNum) {
        StickerImageView imageView = new StickerImageView(getContext(), currentAccount);
        imageView.setStickerPackName(STICKER_PACK_NAME);
        imageView.setStickerNum(stickerNum);
        imageView.getImageReceiver().setAutoRepeat(1);
        return imageView;
    }

    private SimpleTextView createHeaderTitleView(String text) {
        SimpleTextView titleView = new SimpleTextView(getContext());
        titleView.setTextSize(20);
        titleView.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD));
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        titleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        titleView.setText(text);
        return titleView;
    }

    private AnimatedTextView createHeaderSubtitleView(CharSequence text) {
        AnimatedTextView subtitleView = new AnimatedTextView(getContext(), true, true, false);
        subtitleView.setAnimationProperties(0.7f, 0, 450, CubicBezierInterpolator.EASE_OUT_QUINT);
        subtitleView.setIgnoreRTL(!LocaleController.isRTL);
        subtitleView.adaptWidth = false;
        subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        subtitleView.setTextSize(dp(13));
        subtitleView.setTypeface(AndroidUtilities.bold());
        subtitleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        subtitleView.setText(text);
        return subtitleView;
    }

    // ----------------------------------------------------------------------------------------
    // "Update available" content: changelog + download button
    // ----------------------------------------------------------------------------------------

    private void addChangelogSection(ArrayList<UItem> items) {
//        items.add(UItem.asShadow(null));
        List<ChangelogPart> parts = splitChangelogParts();
        for (int i = 0; i < parts.size(); i++) {
            ChangelogPart part = parts.get(i);

            // Если у части есть заголовок, создаем его с правильными headerEntities
            if (!TextUtils.isEmpty(part.headerText)) {
                SpannableStringBuilder whatsNew = new SpannableStringBuilder(part.headerText);
                MessageObject.addEntitiesToText(whatsNew, part.headerEntities, false, true, false, false);
                MessageObject.replaceAnimatedEmoji(whatsNew, part.headerEntities, Theme.chat_namePaint.getFontMetricsInt());

                TextCell headerCell = new TextCell(getContext(), dp(5), false, false, resourcesProvider);
                headerCell.heightDp = dp(10);
                headerCell.textView.setTextSize(14);
                headerCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
                headerCell.setText(whatsNew, false);
                items.add(UItem.asFullyCustom(headerCell));
            }

            // Добавляем само тело (ScrollView)
            items.add(UItem.asCustom(createChangelogScrollView(part)));

            // Разделитель между блоками или шедов после последнего
            if (i < parts.size() - 1) {
                UItem space = UItem.asSpace(dp(10));
                space.transparent = true;
                items.add(space);
//                items.add(UItem.asShadow(null));
            }
        }
    }

    private ScrollView createChangelogScrollView(ChangelogPart part) {
        TextView changelogTextView = createChangelogTextView(part);

        ScrollView changelogScrollView = new ScrollView(getContext());
        changelogScrollView.setVerticalScrollBarEnabled(false);

        changelogTextView.measure(
                View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x - AndroidUtilities.dp(46), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
        );
        int textHeight = changelogTextView.getMeasuredHeight();
        int maxHeight = (int) (AndroidUtilities.displaySize.y * 0.65F);

        LinearLayout.LayoutParams scrollParams = LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                0,
                23, 0, 23, 8
        );
        scrollParams.height = Math.min(textHeight, maxHeight);
        changelogScrollView.setLayoutParams(scrollParams);
        changelogScrollView.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        changelogScrollView.setPadding(35, 23, 23, 23);
        return changelogScrollView;
    }

    private TextView createChangelogTextView(ChangelogPart part) {
        TextView changelogTextView = new TextViewEffects(getContext());
        changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        changelogTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        changelogTextView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink, resourcesProvider));
        changelogTextView.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        if (TextUtils.isEmpty(part.text)) {
            changelogTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AppUpdateChangelogEmpty)));
        } else {
            SpannableStringBuilder builder = new SpannableStringBuilder(part.text);
            MessageObject.addEntitiesToText(builder, part.entities, false, true, false, false);
            MessageObject.replaceAnimatedEmoji(builder, part.entities, changelogTextView.getPaint().getFontMetricsInt());
            changelogTextView.setText(builder);
        }
        return changelogTextView;
    }

    /**
     * Splits {@link #update}'s changelog text into language blocks on {@link #CHANGELOG_BLOCK_SEPARATOR},
     * carrying over only the entities that fall fully inside each block and rebasing their offsets.
     * An entity that straddles the separator (starts in one block, ends in the next) is dropped rather
     * than guessed at — that shouldn't happen for a hand-written RU/EN changelog, but a stray blank line
     * inside a formatted span could theoretically produce one.
     */
    private List<ChangelogPart> splitChangelogParts() {
        String fullText = update.text;
        if (TextUtils.isEmpty(fullText)) {
            return Collections.singletonList(new ChangelogPart(null, null, "", null));
        }

        List<ChangelogPart> parts = new ArrayList<>();
        int lastIndex = 0;
        int separatorLength = CHANGELOG_BLOCK_SEPARATOR.length();

        while (true) {
            int nextSeparator = fullText.indexOf(CHANGELOG_BLOCK_SEPARATOR, lastIndex);
            String blockText;
            int blockEndIndex;

            if (nextSeparator < 0) {
                blockText = fullText.substring(lastIndex);
                blockEndIndex = fullText.length();
            } else {
                blockText = fullText.substring(lastIndex, nextSeparator);
                blockEndIndex = nextSeparator;
            }

            String headerText = null;
            String bodyText = blockText;
            int bodyAbsoluteStart = lastIndex;

            // Ищем перенос строки для отделения заголовка
            int lineBreakIndex = blockText.indexOf("\n");
            if (lineBreakIndex > 0) {
                String potentialHeader = blockText.substring(0, lineBreakIndex).trim();
                if (
                        potentialHeader.toLowerCase().contains("what's new")
                        || potentialHeader.toLowerCase().contains("changelog")
                        || potentialHeader.toLowerCase().contains("что нового")
                        || potentialHeader.toLowerCase().contains("список изменений")
                ) {
                    headerText = potentialHeader;
                    // Пропускаем \n (и возможный \r перед ним)
                    int bodyStartIndex = lineBreakIndex + 1;
                    bodyText = blockText.substring(bodyStartIndex);
                    bodyAbsoluteStart = lastIndex + bodyStartIndex;
                }
            }

            // Собираем энтити раздельно для заголовка и для тела
            ArrayList<TLRPC.MessageEntity> headerEntities = new ArrayList<>();
            ArrayList<TLRPC.MessageEntity> bodyEntities = new ArrayList<>();

            if (update.entities != null) {
                for (TLRPC.MessageEntity entity : update.entities) {
                    // Энтити заголовка (если заголовок вообще есть в этом блоке)
                    if (headerText != null && entity.offset >= lastIndex && (entity.offset + entity.length) < bodyAbsoluteStart) {
                        headerEntities.add(cloneEntityWithOffset(entity, entity.offset - lastIndex));
                    }
                    // Энтити тела
                    else if (entity.offset >= bodyAbsoluteStart && (entity.offset + entity.length) <= blockEndIndex) {
                        bodyEntities.add(cloneEntityWithOffset(entity, entity.offset - bodyAbsoluteStart));
                    }
                }
            }

            parts.add(new ChangelogPart(headerText, headerEntities, bodyText, bodyEntities));

            if (nextSeparator < 0) {
                break;
            }
            lastIndex = nextSeparator + separatorLength;
        }

        return parts;
    }

    /** Deep-clones a {@link TLRPC.MessageEntity} via a TL serialize/deserialize round trip and rebases its offset. */
    private TLRPC.MessageEntity cloneEntityWithOffset(TLRPC.MessageEntity original, int newOffset) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(original.getObjectSize());
            original.serializeToStream(buffer);
            buffer.position(0);
            // serializeToStream() writes the constructor id as the first int; "constructor" itself is a
            // static field on each concrete subclass (TL_messageEntityBold, ...), not on the base
            // MessageEntity type, so we read it back from the buffer rather than off `original`.
            int constructor = buffer.readInt32(true);
            TLRPC.MessageEntity clone = TLRPC.MessageEntity.TLdeserialize(buffer, constructor, true);
            buffer.reuse();
            clone.offset = newOffset;
            return clone;
        } catch (Exception e) {
            CherrygramLogger.e(e);
            return null;
        }
    }

    /**
     * One renderable changelog block: its own text plus the entities rebased to that text.
     */
    private record ChangelogPart(
            CharSequence headerText,
            ArrayList<TLRPC.MessageEntity> headerEntities,
            String text,
            ArrayList<TLRPC.MessageEntity> entities
    ) {}

    private void addDownloadButton(LinearLayout buttonsView) {
        downloadButton = new ButtonWithCounterView(getContext(), resourcesProvider).setRound();
        downloadButton.setFilled(true);
        downloadButton.setText(getDownloadButtonText(), true);
        downloadButton.setOnClickListener(v -> {
            if (isUpdateAlreadyDownloaded()) {
                AndroidUtilities.openForView(SharedConfig.pendingAppUpdate.document, true, fragment.getParentActivity());
            } else {
                downloadButton.setClickable(false);
                FileLoader.getInstance(fragment.getCurrentAccount()).loadFile(update.document, "update", FileLoader.PRIORITY_NORMAL, 1);
                dismiss();
            }
        });

        buttonsView.addView(downloadButton, flexButtonLayoutParams());
    }

    private boolean isUpdateAlreadyDownloaded() {
        if (SharedConfig.pendingAppUpdate == null || SharedConfig.pendingAppUpdate.document == null) {
            return false;
        }
        File path = FileLoader.getInstance(currentAccount).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
        return path != null && path.exists() && fragment != null && fragment.getParentActivity() != null;
    }

    private void applyForceUpdateFlagsIfNeeded() {
        if (update.can_not_skip) {
            setCancelable(false);
            isForce = true;
            CherrygramCoreConfig.INSTANCE.setAutoOTA(true);
        }
        CherrygramCoreConfig.INSTANCE.setForceFound(update.can_not_skip);
    }

    private StringBuilder getDownloadButtonText() {
        StringBuilder sb = new StringBuilder();

        File path = FileLoader.getInstance(currentAccount).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
        if (path.exists()) {
            sb.append(getString(R.string.AppUpdateNow));
        } else {
            sb.append(getString(R.string.AppUpdateDownloadNow));
            sb.append(" (");
            sb.append(getUpdateSizeString());
            sb.append(")");
        }
        return sb;
    }

    private String getUpdateSizeString() {
        if (SharedConfig.pendingAppUpdate != null && SharedConfig.pendingAppUpdate.document != null) {
            String size = AndroidUtilities.formatFileSize(SharedConfig.pendingAppUpdate.document.size, true, false);
            if (!TextUtils.isEmpty(size) && !size.equals("0")) {
                return size;
            }
        }
        return "";
    }

    private FrameLayout createScheduleLaterButtonWrapper() {
        ButtonWithCounterView scheduleButton = new ButtonWithCounterView(getContext(), resourcesProvider);
        scheduleButton.setFilled(false);
        scheduleButton.setText(getString(R.string.AppUpdateRemindMeLater), false);
        scheduleButton.setOnClickListener(v -> {
            dismiss();

            SharedConfig.lastUpdateCheckTime = System.currentTimeMillis();
            SharedConfig.pendingAppUpdate = null;
            SharedConfig.saveConfig();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
        });

        FrameLayout wrapper = new FrameLayout(getContext());
        wrapper.setPadding(dp(16), dp(8), dp(16), dp(8));
        wrapper.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 25, Gravity.CENTER));
        return wrapper;
    }

    // ----------------------------------------------------------------------------------------
    // "No update" content: build info + settings + check button
    // ----------------------------------------------------------------------------------------

    private void addSettingsSection(ArrayList<UItem> items) {
        String buildType = CGResourcesHelper.getBuildType() + " | " + CGResourcesHelper.getAbiCode();

        items.add(UItem.asShadow(null));
        items.add(UItem.asButton(currentVersionRow, R.drawable.msg_info, getString(R.string.UP_CurrentVersion), CGResourcesHelper.getCherryVersion()));
        items.add(UItem.asButton(buildTypeRow, R.drawable.msg_customize, getString(R.string.UP_BuildType), buildType));
        items.add(UItem.asShadow(null));

        items.add(UItem.asCustom(createInstallBetasCell()));
        items.add(UItem.asCustom(createCheckOnLaunchCell()));
        items.add(UItem.asShadow(null));
    }

    private TextCell createInstallBetasCell() {
        TextCell installBetas = new TextCell(getContext(), 23, false, true, resourcesProvider);
        installBetas.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
        installBetas.setTextAndCheckAndIcon(getString(R.string.UP_InstallBetas), CherrygramCoreConfig.INSTANCE.getInstallBetas(), R.drawable.test_tube_solar, false);
        installBetas.setOnClickListener(v -> {
            CherrygramCoreConfig.INSTANCE.setInstallBetas(!CherrygramCoreConfig.INSTANCE.getInstallBetas());
            installBetas.setChecked(!installBetas.isChecked());
            checkUpdatesButton.callOnClick();
        });
        return installBetas;
    }

    private TextCell createCheckOnLaunchCell() {
        TextCell checkOnLaunch = new TextCell(getContext(), 23, false, true, resourcesProvider);
        checkOnLaunch.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
        checkOnLaunch.setTextAndCheckAndIcon(getString(R.string.UP_Auto_OTA), CherrygramCoreConfig.INSTANCE.getAutoOTA(), R.drawable.msg_retry, false);
        checkOnLaunch.setOnClickListener(v -> {
            CherrygramCoreConfig.INSTANCE.setAutoOTA(!CherrygramCoreConfig.INSTANCE.getAutoOTA());
            checkOnLaunch.setChecked(!checkOnLaunch.isChecked());
        });
        return checkOnLaunch;
    }

    private void addCheckUpdatesButton(LinearLayout buttonsView) {
        checkUpdatesButton = new ButtonWithCounterView(getContext(), resourcesProvider).setRound();
        checkUpdatesButton.text.setAnimationProperties(.7f, 0, 500, CubicBezierInterpolator.EASE_OUT_QUINT);
        checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
        checkUpdatesButton.setOnClickListener(v -> onCheckUpdatesClick());

        buttonsView.addView(checkUpdatesButton, flexButtonLayoutParams());
    }

    private void onCheckUpdatesClick() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append("+ ");
        sb.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.msg_retry_solar))), 0, 1, 0);
        checkUpdatesButton.setText(sb, true);
        SharedConfig.lastUpdateCheckTime = System.currentTimeMillis();

                if (fragment.getParentActivity() instanceof LaunchActivity launchActivity) {
                    launchActivity.checkAppUpdate(true, new Browser.Progress() {
                        @Override
                        public void end() {
                            checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
                            if (SharedConfig.isAppUpdateAvailable()) {
                                dismiss();
                            } else {
                                BulletinFactory.of(getContainer(), resourcesProvider).createErrorBulletin(getString(R.string.YourVersionIsLatest)).show();
                            }
                        }
                    });
                }
            }

    // ----------------------------------------------------------------------------------------
    // Secondary (round icon) buttons: translate + apk source
    // ----------------------------------------------------------------------------------------

    private void addSecondaryButtons(LinearLayout buttonsView) {
        LinearLayout.LayoutParams secondaryButtonParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        secondaryButtonParams.rightMargin = dp(8);

        if (available) {
            buttonsView.addView(createTranslateButton(), secondaryButtonParams);
        }

        ButtonWithCounterView apkButton = createApkButton();
        buttonsView.addView(apkButton, secondaryButtonParams);
    }

    private ButtonWithCounterView createTranslateButton() {
        ButtonWithCounterView translateButton = new ButtonWithCounterView(getContext(), resourcesProvider).setRound();
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append("+");
        sb.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), R.drawable.msg_translate_filled_solar)), 0, 1, 0);
        translateButton.setText(sb, false);
        translateButton.setOnClickListener(v -> onTranslateClick());
        return translateButton;
    }

    private void onTranslateClick() {
        String fromLang = "en";
        String toLang = TranslateAlert2.getToLanguage();

        TranslateAlert2.showAlert(getContext(), fragment, currentAccount, fromLang, toLang, update.text, update.entities, false, span -> {
            if (span != null) {
                Browser.openUrl(getContext(), span.getURL());
                return true;
            }
            return false;
        }, () -> {
            if (isForce) return;
            AndroidUtilities.runOnUIThread(() -> UpdaterBottomSheet.showAlert(fragment, true, SharedConfig.pendingAppUpdate), 200);
        });
    }

    private ButtonWithCounterView createApkButton() {
        ButtonWithCounterView apkButton = new ButtonWithCounterView(getContext(), resourcesProvider).setRound();
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append("+");
        sb.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), isForce ? R.drawable.github_cat_filled : R.drawable.msg_folders_channels_solar)), 0, 1, 0);
        apkButton.setText(sb, false);
        apkButton.setOnClickListener(v -> {
            if (isForce) {
                openGithubReleases();
            } else {
                openApkChannel();
            }
        });
        return apkButton;
    }

    private void openApkChannel() {
        dismiss();

        String username;
        if (CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
            username = Constants.CG_BETA_APKS_CHANNEL_USERNAME;
        } else {
            username = Constants.CG_APKS_CHANNEL_USERNAME;
        }
        fragment.getMessagesController().openByUserName(username, fragment, 1);
    }

    private void openGithubReleases() {
        String githubLink;
        if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
            githubLink = Constants.CG_GITHUB_URL + "Beta-APKs/releases/latest";
        } else {
            githubLink = Constants.CG_GITHUB_URL + "/releases/latest";
        }
        Browser.openInExternalBrowser(fragment.getContext(), githubLink, true);
    }

    // ----------------------------------------------------------------------------------------
    // Shared helpers
    // ----------------------------------------------------------------------------------------

    /** Layout params for the main flexible (weight = 1) button in the buttons row: download / check-for-updates. */
    private LinearLayout.LayoutParams flexButtonLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.rightMargin = dp(8);
        params.leftMargin = dp(8);
        return params;
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), resourcesProvider).createCopyBulletin(getString(R.string.TextCopied)).show();
    }

}