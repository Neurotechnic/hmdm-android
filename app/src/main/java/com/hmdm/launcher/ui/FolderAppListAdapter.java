package com.hmdm.launcher.ui;

import android.app.Activity;

import com.hmdm.launcher.util.AppInfo;

import java.util.List;

public class FolderAppListAdapter extends BaseAppListAdapter {
    public FolderAppListAdapter(Activity parentActivity, List<AppInfo> items, OnAppChooseListener appChooseListener) {
        super(parentActivity, appChooseListener, null);
        this.items = items;
        initShortcuts();
    }

    @Override
    public void updateShortcuts(Activity parentActivity) {
        initShortcuts();
    }
}
