package com.hmdm.launcher.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.Application;
import com.hmdm.launcher.util.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppShortcutManager {

    private static AppShortcutManager instance;

    public static AppShortcutManager getInstance() {
        if (instance == null) {
            instance = new AppShortcutManager();
        }
        return instance;
    }

    public int getInstalledAppCount(Context context, boolean bottom) {
        return getInstalledApps(context, bottom).size();
    }

    public List<AppInfo> getInstalledApps(Context context, boolean bottom) {
        Map<String, Application> requiredPackages = new HashMap();
        Map<String, Application> requiredLinks = new HashMap();
        getConfiguredApps(context, bottom, requiredPackages, requiredLinks);

        List<AppInfo> appInfos = new ArrayList<>();
        List<ApplicationInfo> packs = context.getPackageManager().getInstalledApplications(0);
        if (packs == null) {
            return new ArrayList<AppInfo>();
        }
        // First we display app icons
        PackageManager pm = context.getPackageManager();
        for(int i = 0; i < packs.size(); i++) {
            ApplicationInfo p = packs.get(i);
            if (pm.getLaunchIntentForPackage(p.packageName) != null &&
                    requiredPackages.containsKey( p.packageName) ) {
                Application app = requiredPackages.get(p.packageName);
                AppInfo newInfo = new AppInfo();
                newInfo.type = AppInfo.TYPE_APP;
                newInfo.keyCode = app.getKeyCode();
                String fallbackName = p.loadLabel(pm).toString();
                applyLauncherLabel(newInfo, app.getIconText() != null ? app.getIconText() : fallbackName, fallbackName);
                newInfo.packageName = p.packageName;
                newInfo.iconUrl = app.getIcon();
                newInfo.screenOrder = app.getScreenOrder();
                newInfo.longTap = app.isLongTap() ? 1 : 0;

                // Duplicate shortcuts for apps with multiple intent activities
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(p.packageName);
                List<ResolveInfo> shortcuts = pm.queryIntentActivities(intent, 0);
                if (shortcuts.size() > 1) {
                    for (int j = 0; j < shortcuts.size(); j++) {
                        AppInfo appInfo = new AppInfo(newInfo);
                        appInfo.multiIcon = true;
                        appInfo.iconIndex = j;
                        if (appInfo.folderName == null) {
                            appInfo.name = shortcuts.get(j).loadLabel(pm).toString();
                        }
                        // Icon is loaded in BaseAppListAdapter because here's just a custom URL
                        appInfos.add(appInfo);
                    }
                } else {
                    appInfos.add(newInfo);
                }
            }
        }

        // Then we display weblinks
        for (Map.Entry<String, Application> entry : requiredLinks.entrySet()) {
            AppInfo newInfo = new AppInfo();
            newInfo.type = entry.getValue().getType().equals(Application.TYPE_INTENT) ? AppInfo.TYPE_INTENT : AppInfo.TYPE_WEB;
            newInfo.keyCode = entry.getValue().getKeyCode();
            applyLauncherLabel(newInfo, entry.getValue().getIconText(), getLinkFallbackName(entry));
            newInfo.url = entry.getValue().getUrl();
            newInfo.iconUrl = entry.getValue().getIcon();
            newInfo.screenOrder = entry.getValue().getScreenOrder();
            newInfo.useKiosk = entry.getValue().isUseKiosk() ? 1 : 0;
            newInfo.intent = entry.getValue().getIntent();
            appInfos.add(newInfo);
        }

        // Apply manually set order
        Collections.sort(appInfos, new AppInfosComparator());

        return buildLauncherItems(appInfos);
    }

    private void applyLauncherLabel(AppInfo appInfo, CharSequence label, String fallbackName) {
        ParsedLauncherLabel parsedLabel = FolderLabelParser.parse(label, fallbackName);
        appInfo.name = parsedLabel.getVisibleLabel();
        if (parsedLabel.isFolder()) {
            appInfo.folderName = parsedLabel.getFolderName();
        }
    }

    private String getLinkFallbackName(Map.Entry<String, Application> entry) {
        if (entry.getValue().getName() != null) {
            return entry.getValue().getName();
        }
        return entry.getKey();
    }

    private List<AppInfo> buildLauncherItems(List<AppInfo> appInfos) {
        List<AppInfo> launcherItems = new ArrayList<>();
        Map<String, AppInfo> folders = new LinkedHashMap<>();

        for (AppInfo appInfo : appInfos) {
            if (appInfo.folderName == null) {
                launcherItems.add(appInfo);
                continue;
            }

            AppInfo folder = folders.get(appInfo.folderName);
            if (folder == null) {
                folder = new AppInfo();
                folder.type = AppInfo.TYPE_FOLDER;
                folder.name = appInfo.folderName;
                folder.folderName = appInfo.folderName;
                folder.screenOrder = appInfo.screenOrder;
                folder.folderItems = new ArrayList<>();
                folders.put(appInfo.folderName, folder);
                launcherItems.add(folder);
            }
            folder.folderItems.add(appInfo);
        }

        return launcherItems;
    }

    private void getConfiguredApps(Context context, boolean bottom, Map<String, Application> requiredPackages, Map<String, Application> requiredLinks) {
        SettingsHelper config = SettingsHelper.getInstance( context );
        if ( config.getConfig() != null ) {
            List< Application > applications = SettingsHelper.getInstance( context ).getConfig().getApplications();
            for ( Application application : applications ) {
                if (application.isShowIcon() && !application.isRemove() && (bottom == application.isBottom())) {
                    if (application.getType() == null || application.getType().equals(Application.TYPE_APP)) {
                        requiredPackages.put(application.getPkg(), application);
                    } else if (application.getType().equals(Application.TYPE_WEB)) {
                        requiredLinks.put(application.getUrl(), application);
                    } else if (application.getType().equals(Application.TYPE_INTENT)) {
                        requiredLinks.put(application.getIntent(), application);
                    }
                }
            }
        }
    }

    public class AppInfosComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo o1, AppInfo o2) {
            if (o1.screenOrder == null) {
                if (o2.screenOrder == null) {
                    return 0;
                }
                return 1;
            }
            if (o2.screenOrder == null) {
                return -1;
            }
            return Integer.compare(o1.screenOrder, o2.screenOrder);
        }
    }

}
