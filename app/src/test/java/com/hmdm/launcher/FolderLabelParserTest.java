package com.hmdm.launcher;

import com.hmdm.launcher.ui.FolderLabelParser;
import com.hmdm.launcher.ui.ParsedLauncherLabel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FolderLabelParserTest {
    @Test
    public void parsesFolderPrefixWithSpace() {
        ParsedLauncherLabel label = FolderLabelParser.parse("[Office] Word", "Fallback");

        assertTrue(label.isFolder());
        assertEquals("Office", label.getFolderName());
        assertEquals("Word", label.getVisibleLabel());
    }

    @Test
    public void parsesFolderPrefixWithoutSpace() {
        ParsedLauncherLabel label = FolderLabelParser.parse("[Office]Word", "Fallback");

        assertTrue(label.isFolder());
        assertEquals("Office", label.getFolderName());
        assertEquals("Word", label.getVisibleLabel());
    }

    @Test
    public void trimsValidFolderSyntax() {
        ParsedLauncherLabel label = FolderLabelParser.parse(" [Office] Word ", "Fallback");

        assertTrue(label.isFolder());
        assertEquals("Office", label.getFolderName());
        assertEquals("Word", label.getVisibleLabel());
    }

    @Test
    public void ignoresBracketsInMiddleOfLabel() {
        ParsedLauncherLabel label = FolderLabelParser.parse("Office [Test] App", "Fallback");

        assertFalse(label.isFolder());
        assertEquals("Office [Test] App", label.getVisibleLabel());
    }

    @Test
    public void leavesNormalLabelsUnchanged() {
        ParsedLauncherLabel label = FolderLabelParser.parse(" Firefox ", "Fallback");

        assertFalse(label.isFolder());
        assertEquals(" Firefox ", label.getVisibleLabel());
    }

    @Test
    public void ignoresEmptyFolderName() {
        ParsedLauncherLabel label = FolderLabelParser.parse("[] App", "Fallback");

        assertFalse(label.isFolder());
        assertEquals("[] App", label.getVisibleLabel());
    }

    @Test
    public void usesFallbackWhenVisibleLabelIsEmpty() {
        ParsedLauncherLabel label = FolderLabelParser.parse("[Tools]", "Settings");

        assertTrue(label.isFolder());
        assertEquals("Tools", label.getFolderName());
        assertEquals("Settings", label.getVisibleLabel());
    }
}
