/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs.backup;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.StickerEmptyView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.StringTokenizer;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;

public class BackupFileImportActivity extends BaseFragment {

    private static final int sort_button = 1;
    private static final int REQUEST_CODE_SYSTEM_PICKER = 1390;
    private static final int MAX_SCAN_DEPTH = 8;

    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_FORWARD = 1;
    private static final int ANIMATION_BACKWARD = 2;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_FILE = 1;
    private static final int VIEW_TYPE_SHADOW = 2;
    private static final int VIEW_TYPE_BOTTOM = 3;

    private FrameLayout contentView;
    private RecyclerListView listView;
    private RecyclerListView backgroundListView;
    private LinearLayoutManager layoutManager;
    private LinearLayoutManager backgroundLayoutManager;
    private ListAdapter listAdapter;
    private ListAdapter backgroundListAdapter;
    private StickerEmptyView emptyView;
    private ActionBarMenuItem sortItem;

    private ValueAnimator listAnimation;
    private int currentAnimationType = ANIMATION_NONE;

    private final ArrayList<HistoryEntry> history = new ArrayList<>();
    private File currentDir;
    private boolean sortByName;
    private boolean receiverRegistered;

    private Utilities.Callback<File> onFileSelected;

    private static class ListItem {
        int icon;
        String title;
        String subtitle = "";
        File file;
    }

    private static class HistoryEntry {
        int scrollItem, scrollOffset;
        File dir;
        CharSequence title;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Runnable r = () -> {
                try {
                    if (currentDir == null) {
                        listRoots();
                    } else {
                        listFiles(currentDir);
                    }
                } catch (Exception e) {
                    CherrygramLogger.e(e);
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                AndroidUtilities.runOnUIThread(r, 1000);
            } else {
                r.run();
            }
        }
    };

    public void setOnFileSelected(Utilities.Callback<File> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }

    @Override
    public boolean onFragmentCreate() {
        sortByName = SharedConfig.sortFilesByName;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_NOFS);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ApplicationLoader.applicationContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ApplicationLoader.applicationContext.registerReceiver(receiver, filter);
            }
            receiverRegistered = true;
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        if (listAnimation != null) {
            listAnimation.cancel();
        }
        if (receiverRegistered) {
            try {
                ApplicationLoader.applicationContext.unregisterReceiver(receiver);
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
            receiverRegistered = false;
        }
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));
        actionBar.setTitleColor(getThemedColor(Theme.key_dialogTextBlack));
        actionBar.setItemsColor(getThemedColor(Theme.key_dialogTextBlack), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_dialogButtonSelector), false);
        BackDrawable backDrawable = new BackDrawable(false);
        backDrawable.setShowStick(!CherrygramAppearanceConfig.INSTANCE.getCenterTitle());
        actionBar.setBackButtonDrawable(backDrawable);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.SelectFile));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (!goBack()) {
                        finishFragment();
                    }
                } else if (id == sort_button) {
                    SharedConfig.toggleSortFilesByName();
                    sortByName = SharedConfig.sortFilesByName;
                    sortRecentItems();
                    sortFileItems();
                    listAdapter.notifyDataSetChanged();
                    sortItem.setIcon(sortByName ? R.drawable.msg_contacts_time : R.drawable.msg_contacts_name);
                }
            }
        });

        sortItem = actionBar.createMenu().addItem(sort_button, sortByName ? R.drawable.msg_contacts_time : R.drawable.msg_contacts_name);
        sortItem.setContentDescription(getString(R.string.AccDescrContactSorting));

        contentView = new FrameLayout(context);
        contentView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));
        fragmentView = contentView;

        emptyView = new StickerEmptyView(context, null, StickerEmptyView.STICKER_TYPE_SEARCH, getResourceProvider());
        emptyView.title.setText(getString(R.string.SearchEmptyViewTitle));
        emptyView.subtitle.setText(getString(R.string.SearchEmptyViewFilteredSubtitleFiles));
        emptyView.setVisibility(View.GONE);
        emptyView.setOnTouchListener((v, event) -> true);
        contentView.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        backgroundListView = new RecyclerListView(context, getResourceProvider()) {
            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return false;
            }
        };
        backgroundListView.setSections();
        backgroundListView.setSectionsType(RecyclerListView.SECTIONS_TYPE_DATE);
        backgroundListView.setVerticalScrollBarEnabled(false);
        backgroundListView.setClipToPadding(false);
        backgroundListView.setLayoutManager(backgroundLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        backgroundListView.setAdapter(backgroundListAdapter = new ListAdapter(context));
        backgroundListView.setVisibility(View.GONE);
        contentView.addView(backgroundListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context, getResourceProvider());
        listView.setSections();
        listView.setSectionsType(RecyclerListView.SECTIONS_TYPE_DATE);
        listView.setVerticalScrollBarEnabled(false);
        listView.setClipToPadding(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter = new ListAdapter(context));
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (currentAnimationType != ANIMATION_NONE) {
                return;
            }
            ListItem item = listAdapter.getItem(position);
            if (item == null) {
                return;
            }

            boolean isExternalStorageManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
            if (!BuildVars.NO_SCOPED_STORAGE && !isExternalStorageManager && (item.icon == R.drawable.files_storage || item.icon == R.drawable.files_internal)) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(intent, REQUEST_CODE_SYSTEM_PICKER);
                } catch (Exception e) {
                    CherrygramLogger.e(e);
                }
            } else if (item.file == null) {
                goBack();
            } else if (item.file.isDirectory()) {
                HistoryEntry he = new HistoryEntry();
                he.scrollItem = Math.max(0, layoutManager.findFirstVisibleItemPosition());
                View topView = layoutManager.findViewByPosition(he.scrollItem);
                if (topView != null) {
                    he.scrollOffset = topView.getTop();
                }
                he.dir = currentDir;
                he.title = actionBar.getTitle();

                prepareAnimation();
                history.add(he);
                if (!listFiles(item.file)) {
                    history.remove(he);
                    backgroundListView.setVisibility(View.GONE);
                    return;
                }
                actionBar.setTitle(item.title);
                layoutManager.scrollToPositionWithOffset(0, 0);
                runAnimation(ANIMATION_FORWARD);
            } else if (!item.file.canRead()) {
                showErrorBox(getString(R.string.AccessError));
            } else {
                selectFile(item.file);
            }
        });

        listRoots();
        loadRecentFiles();

        return fragmentView;
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_SYSTEM_PICKER || resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        File file = new File(ApplicationLoader.applicationContext.getCacheDir(), "import" + BackupHelper.BACKUP_EXTENSION);
        try (InputStream in = ApplicationLoader.applicationContext.getContentResolver().openInputStream(data.getData());
             OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            CherrygramLogger.e(e);
            showErrorBox(getString(R.string.AccessError));
            return;
        }
        selectFile(file);
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        if (!history.isEmpty()) {
            if (invoked) {
                goBack();
            }
            return false;
        }
        return super.onBackPressed(invoked);
    }

    private void selectFile(File file) {
        if (onFileSelected != null) {
            onFileSelected.run(file);
        }
        finishFragment();
    }

    private boolean goBack() {
        if (history.isEmpty()) {
            return false;
        }
        prepareAnimation();
        HistoryEntry he = history.remove(history.size() - 1);
        actionBar.setTitle(he.title);
        if (he.dir == null || !listFiles(he.dir)) {
            history.clear();
            actionBar.setTitle(getString(R.string.SelectFile));
            listRoots();
        }
        layoutManager.scrollToPositionWithOffset(he.scrollItem, he.scrollOffset);
        runAnimation(ANIMATION_BACKWARD);
        return true;
    }

    private void prepareAnimation() {
        backgroundListAdapter.items.clear();
        backgroundListAdapter.items.addAll(listAdapter.items);
        backgroundListAdapter.recentItems.clear();
        backgroundListAdapter.recentItems.addAll(listAdapter.recentItems);
        backgroundListAdapter.showRecent = listAdapter.showRecent;
        backgroundListAdapter.notifyDataSetChanged();
        backgroundListView.setVisibility(View.VISIBLE);

        int p = layoutManager.findFirstVisibleItemPosition();
        View child = p >= 0 ? layoutManager.findViewByPosition(p) : null;
        if (child != null) {
            backgroundLayoutManager.scrollToPositionWithOffset(p, child.getTop());
        }
    }

    private void runAnimation(int animationType) {
        if (listAnimation != null) {
            listAnimation.cancel();
        }
        currentAnimationType = animationType;
        final float xTranslate = dp(150);

        contentView.removeView(backgroundListView);
        int listViewIndex = contentView.indexOfChild(listView);
        backgroundListView.setAlpha(1f);
        backgroundListView.setScaleX(1f);
        backgroundListView.setScaleY(1f);
        backgroundListView.setTranslationX(0f);
        backgroundListView.setVisibility(View.VISIBLE);

        if (animationType == ANIMATION_FORWARD) {
            contentView.addView(backgroundListView, listViewIndex);
            listView.setTranslationX(xTranslate);
            listView.setAlpha(0f);
            listAnimation = ValueAnimator.ofFloat(1f, 0f);
        } else {
            contentView.addView(backgroundListView, listViewIndex + 1);
            listView.setAlpha(0f);
            listView.setScaleX(0.95f);
            listView.setScaleY(0.95f);
            listAnimation = ValueAnimator.ofFloat(0f, 1f);
        }

        listAnimation.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float scale = 0.95f + value * 0.05f;
            if (animationType == ANIMATION_FORWARD) {
                listView.setTranslationX(xTranslate * value);
                listView.setAlpha(1f - value);

                backgroundListView.setAlpha(value);
                backgroundListView.setScaleX(scale);
                backgroundListView.setScaleY(scale);
            } else {
                backgroundListView.setTranslationX(xTranslate * value);
                backgroundListView.setAlpha(Math.max(0f, 1f - value));

                listView.setAlpha(value);
                listView.setScaleX(scale);
                listView.setScaleY(scale);
            }
        });
        listAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                backgroundListView.setVisibility(View.GONE);
                currentAnimationType = ANIMATION_NONE;
                listView.setAlpha(1f);
                listView.setScaleX(1f);
                listView.setScaleY(1f);
                listView.setTranslationX(0f);
            }
        });
        listAnimation.setDuration(animationType == ANIMATION_FORWARD ? 220 : 200);
        listAnimation.setInterpolator(CubicBezierInterpolator.DEFAULT);
        listAnimation.start();
    }

    private void loadRecentFiles() {
        ArrayList<File> quickRoots = new ArrayList<>();
        quickRoots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        quickRoots.add(ApplicationLoader.applicationContext.getExternalFilesDir(null));
        quickRoots.add(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT));

        HashMap<String, File> quickFound = new HashMap<>();
        for (File root : quickRoots) {
            collectBackups(root, 0, 1, quickFound);
        }
        setRecentFiles(quickFound);

        ArrayList<File> roots = new ArrayList<>(quickRoots);
        for (ListItem item : listAdapter.items) {
            if (item.file != null) {
                roots.add(item.file);
            }
        }
        Utilities.globalQueue.postRunnable(() -> {
            HashMap<String, File> found = new HashMap<>();
            for (File root : roots) {
                collectBackups(root, 0, MAX_SCAN_DEPTH, found);
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (!isFinishing()) {
                    setRecentFiles(found);
                }
            });
        });
    }

    private void setRecentFiles(HashMap<String, File> found) {
        listAdapter.recentItems.clear();
        for (File file : found.values()) {
            listAdapter.recentItems.add(createFileItem(file, true));
        }
        sortRecentItems();
        if (currentDir == null && currentAnimationType == ANIMATION_NONE) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private static void collectBackups(File dir, int depth, int maxDepth, HashMap<String, File> found) {
        if (dir == null || depth > maxDepth) {
            return;
        }
        File[] files;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            return;
        }
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            if (name.startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                if (depth == 0 && "Android".equals(name)) {
                    continue;
                }
                collectBackups(file, depth + 1, maxDepth, found);
            } else if (isBackupFile(file)) {
                String key;
                try {
                    key = file.getCanonicalPath();
                } catch (Exception e) {
                    key = file.getAbsolutePath();
                }
                found.put(key, file);
            }
        }
    }

    private static boolean isBackupFile(File file) {
        return file.getName().toLowerCase(Locale.ROOT).endsWith(BackupHelper.BACKUP_EXTENSION) && file.length() > 0;
    }

    private static ListItem createFileItem(File file, boolean withFolder) {
        ListItem item = new ListItem();
        item.title = file.getName();
        item.subtitle = AndroidUtilities.formatFileSize(file.length());
        File parent = file.getParentFile();
        if (withFolder && parent != null) {
            item.subtitle += " • " + parent.getName();
        }
        item.file = file;
        return item;
    }

    private void sortRecentItems() {
        Collections.sort(listAdapter.recentItems, (o1, o2) -> {
            if (sortByName) {
                return o1.file.getName().compareToIgnoreCase(o2.file.getName());
            }
            return Long.compare(o2.file.lastModified(), o1.file.lastModified());
        });
    }

    private void sortFileItems() {
        if (currentDir == null) {
            return;
        }
        Collections.sort(listAdapter.items, (lhs, rhs) -> {
            if (lhs.file == null) {
                return -1;
            } else if (rhs.file == null) {
                return 1;
            }
            boolean isDir1 = lhs.file.isDirectory();
            boolean isDir2 = rhs.file.isDirectory();
            if (isDir1 != isDir2) {
                return isDir1 ? -1 : 1;
            } else if (isDir1 || sortByName) {
                return lhs.file.getName().compareToIgnoreCase(rhs.file.getName());
            }
            return Long.compare(rhs.file.lastModified(), lhs.file.lastModified());
        });
    }

    private void updateEmptyView() {
        emptyView.setVisibility(currentDir != null && listAdapter.items.size() <= 1 ? View.VISIBLE : View.GONE);
    }

    private boolean listFiles(File dir) {
        if (!dir.canRead()) {
            String path = dir.getAbsolutePath();
            String state = Environment.getExternalStorageState();
            if ((path.startsWith(Environment.getExternalStorageDirectory().toString()) || path.startsWith("/sdcard") || path.startsWith("/mnt/sdcard"))
                    && !Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                currentDir = dir;
                listAdapter.items.clear();
                listAdapter.showRecent = false;
                listAdapter.notifyDataSetChanged();
                updateEmptyView();
                return true;
            }
            showErrorBox(getString(R.string.AccessError));
            return false;
        }
        File[] files;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            showErrorBox(e.getLocalizedMessage());
            return false;
        }
        if (files == null) {
            showErrorBox(getString(R.string.UnknownError));
            return false;
        }
        currentDir = dir;
        listAdapter.items.clear();
        listAdapter.showRecent = false;

        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                ListItem item = new ListItem();
                item.title = file.getName();
                item.subtitle = getString(R.string.Folder);
                item.icon = R.drawable.files_folder;
                item.file = file;
                listAdapter.items.add(item);
            } else if (isBackupFile(file)) {
                listAdapter.items.add(createFileItem(file, false));
            }
        }

        ListItem up = new ListItem();
        up.title = "..";
        up.icon = R.drawable.files_folder;
        HistoryEntry last = history.isEmpty() ? null : history.get(history.size() - 1);
        up.subtitle = last == null || last.dir == null ? getString(R.string.Folder) : last.dir.toString();
        listAdapter.items.add(0, up);

        sortFileItems();
        listAdapter.notifyDataSetChanged();
        updateEmptyView();
        return true;
    }

    @SuppressLint("NewApi")
    private void listRoots() {
        currentDir = null;
        listAdapter.items.clear();
        listAdapter.showRecent = true;

        HashSet<String> paths = new HashSet<>();
        String defaultPath = Environment.getExternalStorageDirectory().getPath();
        String defaultPathState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(defaultPathState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(defaultPathState)) {
            ListItem ext = new ListItem();
            if (Environment.isExternalStorageRemovable()) {
                ext.title = getString(R.string.SdCard);
                ext.icon = R.drawable.files_internal;
                ext.subtitle = getString(R.string.ExternalFolderInfo);
            } else {
                ext.title = getString(R.string.InternalStorage);
                ext.icon = R.drawable.files_storage;
                ext.subtitle = getString(R.string.InternalFolderInfo);
            }
            ext.file = Environment.getExternalStorageDirectory();
            listAdapter.items.add(ext);
            paths.add(defaultPath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/mounts"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("/dev/block/vold") || !(line.contains("vfat") || line.contains("/mnt"))) {
                    continue;
                }
                if (line.contains("/mnt/secure") || line.contains("/mnt/asec") || line.contains("/mnt/obb") || line.contains("/dev/mapper") || line.contains("tmpfs")) {
                    continue;
                }
                StringTokenizer tokens = new StringTokenizer(line, " ");
                tokens.nextToken();
                String path = tokens.nextToken();
                if (paths.contains(path)) {
                    continue;
                }
                if (!new File(path).isDirectory()) {
                    int index = path.lastIndexOf('/');
                    if (index != -1) {
                        String newPath = "/storage/" + path.substring(index + 1);
                        if (new File(newPath).isDirectory()) {
                            path = newPath;
                        }
                    }
                }
                paths.add(path);

                ListItem item = new ListItem();
                item.title = getString(path.toLowerCase(Locale.ROOT).contains("sd") ? R.string.SdCard : R.string.ExternalStorage);
                item.subtitle = getString(R.string.ExternalFolderInfo);
                item.icon = R.drawable.files_internal;
                item.file = new File(path);
                listAdapter.items.add(item);
            }
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }

        try {
            File telegramPath = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "Telegram");
            if (telegramPath.exists()) {
                ListItem item = new ListItem();
                item.title = "Telegram";
                item.subtitle = getString(R.string.AppFolderInfo);
                item.icon = R.drawable.files_folder;
                item.file = telegramPath;
                listAdapter.items.add(item);
            }
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }

        listAdapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void showErrorBox(String error) {
        if (getParentActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getParentActivity(), getResourceProvider())
                .setTitle(getString(R.string.CG_AppName))
                .setMessage(error)
                .setPositiveButton(getString(R.string.OK), null)
                .show();
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context context;
        private final ArrayList<ListItem> items = new ArrayList<>();
        private final ArrayList<ListItem> recentItems = new ArrayList<>();
        private boolean showRecent;

        ListAdapter(Context context) {
            this.context = context;
        }

        private boolean hasRecent() {
            return showRecent && !recentItems.isEmpty();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_FILE;
        }

        @Override
        public int getItemCount() {
            return items.size() + (hasRecent() ? recentItems.size() + 2 : 0) + 1;
        }

        public ListItem getItem(int position) {
            if (position < items.size()) {
                return items.get(position);
            }
            int recentPosition = position - items.size() - 2;
            if (hasRecent() && recentPosition >= 0 && recentPosition < recentItems.size()) {
                return recentItems.get(recentPosition);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return VIEW_TYPE_BOTTOM;
            } else if (position == items.size()) {
                return VIEW_TYPE_SHADOW;
            } else if (position == items.size() + 1) {
                return VIEW_TYPE_HEADER;
            }
            return VIEW_TYPE_FILE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(context, getResourceProvider());
                    break;
                case VIEW_TYPE_FILE:
                    view = new SharedDocumentCell(context, SharedDocumentCell.VIEW_TYPE_PICKER, getResourceProvider());
                    break;
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case VIEW_TYPE_BOTTOM:
                default:
                    view = new View(context);
                    view.setTag(RecyclerListView.TAG_NOT_SECTION);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(getString(sortByName ? R.string.RecentFilesAZ : R.string.RecentFiles));
            } else if (holder.getItemViewType() == VIEW_TYPE_FILE) {
                ListItem item = getItem(position);
                if (item == null) {
                    return;
                }
                SharedDocumentCell cell = (SharedDocumentCell) holder.itemView;
                if (item.icon != 0) {
                    cell.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, null, null, item.icon, position != items.size() - 1);
                } else {
                    String ext = FileLoader.getFileExtension(item.file).toUpperCase(Locale.ROOT);
                    cell.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, ext.substring(0, Math.min(ext.length(), 4)), null, 0, false);
                }
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_dialogButtonSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_dialogScrollGlow));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"dateTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"thumbImageView"}, null, null, null, Theme.key_files_folderIcon));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{SharedDocumentCell.class}, new String[]{"thumbImageView"}, null, null, null, Theme.key_files_folderIconBackground));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"extTextView"}, null, null, null, Theme.key_files_iconText));

        return themeDescriptions;
    }

}