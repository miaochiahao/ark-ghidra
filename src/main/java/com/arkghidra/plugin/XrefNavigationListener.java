package com.arkghidra.plugin;

/**
 * Callback interface for navigating to a cross-reference entry in the output panel.
 */
public interface XrefNavigationListener {

    /**
     * Called when the user selects an xref entry.
     *
     * @param charOffset the character offset in the decompiled code to navigate to
     */
    void onXrefSelected(int charOffset);
}
