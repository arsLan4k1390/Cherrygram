/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package uz.unnarsx.cherrygram.core.ui;

import androidx.recyclerview.widget.DiffUtil;

import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;

import java.util.ArrayList;

public abstract class MD3AdapterWithDiffUtils extends MD3ListAdapter {

    DiffUtilsCallback callback = new DiffUtilsCallback();

    public void setItems(ArrayList<? extends AdapterWithDiffUtils.Item> oldItems, ArrayList<? extends AdapterWithDiffUtils.Item> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }
        callback.setItems(oldItems, newItems);
        DiffUtil.calculateDiff(callback).dispatchUpdatesTo(this);
    }

    private static class DiffUtilsCallback extends DiffUtil.Callback {

        ArrayList<? extends AdapterWithDiffUtils.Item> oldItems;
        ArrayList<? extends AdapterWithDiffUtils.Item> newItems;

        public void setItems(ArrayList<? extends AdapterWithDiffUtils.Item> oldItems, ArrayList<? extends AdapterWithDiffUtils.Item> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition).compare(newItems.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition).compareContents(newItems.get(newItemPosition));
        }
    }

}

