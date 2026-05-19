package com.hmdm.launcher.ui;

public class ParsedLauncherLabel {
    private final boolean folder;
    private final String folderName;
    private final String visibleLabel;

    public ParsedLauncherLabel(boolean folder, String folderName, String visibleLabel) {
        this.folder = folder;
        this.folderName = folderName;
        this.visibleLabel = visibleLabel;
    }

    public boolean isFolder() {
        return folder;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getVisibleLabel() {
        return visibleLabel;
    }
}
