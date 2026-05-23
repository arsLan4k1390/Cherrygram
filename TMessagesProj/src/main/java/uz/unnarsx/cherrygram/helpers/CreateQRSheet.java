/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Components.BottomSheetWithRecyclerListView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper;
import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class CreateQRSheet extends BottomSheetWithRecyclerListView {

    private PollEditTextCell outlineEditText;

    private ImageView qrImageView;
    private View qrSkeletonView;
    private Runnable updateQrRunnable;

    private boolean canDismiss = false;

    private Bitmap currentQr;
    private int qrVersion = 0;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.CG_CreateQR);
    }

    private UniversalAdapter adapter;
    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        adapter = new UniversalAdapter(listView, getContext(), currentAccount, 0, true, this::fillItems, resourcesProvider);
        adapter.setApplyBackground(false);
        return adapter;
    }

    public CreateQRSheet(Context context, BaseFragment fragment, Theme.ResourcesProvider resourcesProvider) {
        super(context, fragment, true, false, false, false, ActionBarType.SLIDING, resourcesProvider);

        fixNavigationBar();
        setCanDismissWithTouchOutside(false);

        ImageView closeView = new ImageView(context);
        closeView.setScaleType(ImageView.ScaleType.CENTER);
        closeView.setImageResource(R.drawable.ic_close_white);
        closeView.setColorFilter(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        closeView.setBackground(Theme.createSelectorDrawable(Theme.multAlpha(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), .10f)));
        actionBar.addView(closeView, LayoutHelper.createFrame(54, 54, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, 8, 0));
        ScaleStateListAnimator.apply(closeView, .1f, 1.5f);
        closeView.setOnClickListener(v -> {
            canDismiss = true;
            this.dismiss();
        });

        ignoreTouchActionBar = false;
        headerMoveTop = dp(12);
        topPadding = 0.35f;

        setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        recyclerListView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, dp(20));
        recyclerListView.setClipToPadding(false);
        recyclerListView.setSections();
        recyclerListView.setOnItemClickListener((view, position) -> {
            final UItem item = adapter.getItem(position - 1);
            if (item == null) return;
        });

        takeTranslationIntoAccount = true;
        final DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }
        };
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        recyclerListView.setItemAnimator(itemAnimator);

        adapter.update(false);
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asShadow(null));
        adapter.itemsOffset = 1;
        adapter.whiteSectionStart();

        LinearLayout contentLayout = new LinearLayout(getContext());
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        qrImageView = new ImageView(getContext());
        ScaleStateListAnimator.apply(qrImageView, 0.03f, 1.2f);
        qrImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        qrImageView.setOutlineProvider(new QRCodeSheet.RoundedQrOutlineProvider());
        qrImageView.setClipToOutline(true);

        qrSkeletonView = new View(getContext());
        qrSkeletonView.setBackground(createSkeletonDrawable());

        qrImageView.setAlpha(0f);
        qrImageView.setScaleX(0.95f);
        qrImageView.setScaleY(0.95f);

        qrSkeletonView.setAlpha(1f);
        qrSkeletonView.setScaleX(1f);
        qrSkeletonView.setScaleY(1f);

        FrameLayout qrContainer = new FrameLayout(getContext());
        qrContainer.addView(qrImageView, LayoutHelper.createFrame(230, 230, Gravity.CENTER));
        qrContainer.addView(qrSkeletonView, LayoutHelper.createFrame(230, 230, Gravity.CENTER));

        contentLayout.addView(
                qrContainer,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 18, 20, 18, 15)
        );

        items.add(SettingsHelper.asCustomWithBackground(contentLayout));
        items.add(UItem.asShadow(null));
        adapter.whiteSectionEnd();
        items.add(UItem.asShadow(getString(R.string.CG_QR_Hint)));

        outlineEditText = new PollEditTextCell(getContext(), false, PollEditTextCell.TYPE_DEFAULT, null, resourcesProvider);
        /*outlineEditText.getTextView().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        outlineEditText.getTextView().setImeOptions(EditorInfo.IME_ACTION_DONE);
        outlineEditText.getTextView().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                doOnDone();
            }
            return false;
        });*/
        outlineEditText.getTextView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                scheduleQrUpdate();
            }
        });
        outlineEditText.getTextView().setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated));
        outlineEditText.getTextView().setSingleLine(false);
        outlineEditText.getTextView().setHint(getString(R.string.TextPlaceholder));
        outlineEditText.setTextRight(95);

        ImageView clearImageView = new ImageView(getContext());
        clearImageView.setImageResource(R.drawable.menu_delete_old);
        clearImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider), PorterDuff.Mode.SRC_IN));
        outlineEditText.addView(clearImageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 0, 0, 20, 0));
        ScaleStateListAnimator.apply(clearImageView);
        clearImageView.setOnClickListener(v -> outlineEditText.getTextView().setText(""));

        items.add(SettingsHelper.asCustomWithBackground(outlineEditText));

        items.add(UItem.asShadow(null));
    }

    private void updateQr(int version) {
        if (qrImageView == null || outlineEditText == null) return;

        String text = outlineEditText.getTextView().getText().toString();

        if (TextUtils.isEmpty(text)) {
            animateQR(true);
            return;
        }

        AndroidUtilities.runOnUIThread(() -> {
            if (version != qrVersion) return;
            currentQr = QrHelper.createQR(text);

            if (version != qrVersion) return;
            qrImageView.setImageBitmap(currentQr);
            animateQR(false);
        }, 80);
    }

    private void scheduleQrUpdate() {
        if (updateQrRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(updateQrRunnable);
        }

        final int version = ++qrVersion;

        updateQrRunnable = () -> updateQr(version);
        AndroidUtilities.runOnUIThread(updateQrRunnable, 150);
    }

    private void animateQR(boolean toSkeleton) {
        qrImageView.animate().cancel();
        qrSkeletonView.animate().cancel();

        if (toSkeleton) {
            qrImageView.setImageBitmap(null);
            qrImageView.setOnClickListener(null);
            qrImageView.setOnLongClickListener(null);
        } else {
            qrImageView.setOnClickListener(view -> {
                if (currentQr != null) {
                    copyQR(currentQr, false, getBaseFragment().getParentActivity());
                }
            });
            qrImageView.setOnLongClickListener(view -> {
                if (currentQr != null) {
                    copyQR(currentQr, true, getBaseFragment().getParentActivity());
                }
                return true;
            });
        }

        qrImageView.animate()
                .alpha(toSkeleton ? 0f : 1f)
                .scaleX(toSkeleton ? 0.95f : 1f)
                .scaleY(toSkeleton ? 0.95f : 1f)
                .setDuration(toSkeleton ? 180 : 220)
                .start();

        qrSkeletonView.animate()
                .alpha(toSkeleton ? 1f : 0f)
                .scaleX(toSkeleton ? 1f : 1.05f)
                .scaleY(toSkeleton ? 1f : 1.05f)
                .setDuration(toSkeleton ? 220 : 180)
                .start();
    }

    private Drawable createSkeletonDrawable() {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(16));
        d.setColor(Theme.multAlpha(
                Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                0.15f
        ));
        return d;
    }

    public void copyQR(Bitmap qrBitmap, boolean save, Activity activity) {
        try {
            File dir = activity.getExternalFilesDir(null);
            if (dir == null) return;

            File qrFile = new File(dir, "qr_code.png");

            try (FileOutputStream outputStream = new FileOutputStream(qrFile)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            }

            if (save) {
                MediaController.saveFile(qrFile.toString(), activity, 0, null, "image/png", uri -> {
                    BulletinFactory.of(getContainer(), resourcesProvider)
                            .createDownloadBulletin(BulletinFactory.FileType.PHOTO, resourcesProvider)
                            .setDuration(Bulletin.DURATION_SHORT)
                            .show();
                });
            } else {
                ChatsHelper.addFileToClipboard(qrFile, () -> BulletinFactory.of(getContainer(), resourcesProvider)
                        .createSuccessBulletin(getString(R.string.CG_PhotoCopied), resourcesProvider)
                        .setDuration(Bulletin.DURATION_SHORT)
                        .show());
            }
        } catch (IOException e) {
            CherrygramLogger.e(e);
        }
    }

    @Override
    public void onOpenAnimationEnd() {
        super.onOpenAnimationEnd();
        if (outlineEditText != null && outlineEditText.getEditText() != null) {
            outlineEditText.getTextView().postDelayed(() -> {
                outlineEditText.getTextView().requestFocus();
                AndroidUtilities.showKeyboard(outlineEditText.getTextView());
            }, 10);
        }
    }

    @Override
    public void onBackPressed() {
        canDismiss = true;
        if (canDismiss) {
            if (attachedFragment == null) {
                super.onBackPressed();
            } else {
                dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        if (canDismiss) {
            if (attachedFragment == null) {
                super.dismiss();
            } else {
                dismiss();
            }
        }
    }

}