package com.labiapari.platyplus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iago on 10/04/18.
 */

public class SelectionRecorder<T> {

    private static final SelectionRecorder mSelectionRecorder = new SelectionRecorder();

    private boolean mIsSelectionOpen = false;

    private List<T> mSelectedItems;

    public int anchorViewId;

    private SelectionRecorder() {
        mSelectedItems = new ArrayList<>();
    }

    public static SelectionRecorder getInstance() {
        return mSelectionRecorder;
    }

    public void openSelection() {
        mIsSelectionOpen = true;
    }

    public boolean isSelectionOpen() {
        return mIsSelectionOpen;
    }

    public void addtem(T item) {
        mSelectedItems.add(item);
    }

    public void addBulk(List<T> data) {
        mSelectedItems.addAll(data);
    }

    public void remove(T item, boolean closeSelection) {
        mSelectedItems.remove(item);
        mIsSelectionOpen = !mSelectedItems.isEmpty() || !closeSelection;
    }

    public void clear(boolean closeSelection) {
        mIsSelectionOpen = !closeSelection;
        mSelectedItems.clear();
    }

    public void select(T item, SmartAdapter.OnSelectionChangeListener listener) {
        if (!mIsSelectionOpen)
            if (listener != null)
                listener.onStartSelection();

        if (mSelectedItems.contains(item)) {
            mSelectedItems.remove(item);
        } else {
            mSelectedItems.add(item);
        }

        if (mSelectedItems.size() > 0) {
            mIsSelectionOpen = true;
        } else {
            mIsSelectionOpen = false;
            if (listener != null)
                listener.onCancelSelection();
        }
    }

    public boolean has(T data) {
        return mSelectedItems.contains(data);
    }
}
