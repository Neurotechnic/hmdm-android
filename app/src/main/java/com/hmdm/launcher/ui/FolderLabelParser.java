package com.hmdm.launcher.ui;

public class FolderLabelParser {
    public static ParsedLauncherLabel parse(CharSequence label, String fallbackLabel) {
        String originalLabel = label != null ? label.toString() : "";
        String trimmedLabel = originalLabel.trim();

        if (!trimmedLabel.startsWith("[")) {
            return new ParsedLauncherLabel(false, null, originalLabel);
        }

        int closingBracket = trimmedLabel.indexOf(']');
        if (closingBracket < 0) {
            return new ParsedLauncherLabel(false, null, originalLabel);
        }

        String folderName = trimmedLabel.substring(1, closingBracket).trim();
        if (folderName.length() == 0) {
            return new ParsedLauncherLabel(false, null, originalLabel);
        }

        String visibleLabel = trimmedLabel.substring(closingBracket + 1).trim();
        if (visibleLabel.length() == 0) {
            visibleLabel = fallbackLabel != null ? fallbackLabel : originalLabel;
        }

        return new ParsedLauncherLabel(true, folderName, visibleLabel);
    }
}
