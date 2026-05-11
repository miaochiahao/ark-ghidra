package com.arkghidra.plugin;

import com.arkghidra.format.AbcClass;

/**
 * Listener notified when the user single-clicks a class node in the ABC structure tree.
 */
public interface ClassNavigationListener {

    /**
     * Called when the user selects a class node.
     *
     * @param abcClass the selected class
     */
    void onClassSelected(AbcClass abcClass);
}
