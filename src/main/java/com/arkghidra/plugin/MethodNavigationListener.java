package com.arkghidra.plugin;

import com.arkghidra.format.AbcMethod;

/**
 * Listener notified when the user double-clicks a method node in the ABC structure tree.
 */
public interface MethodNavigationListener {

    /**
     * Called when the user requests navigation to a method.
     *
     * @param method the method to navigate to
     */
    void onMethodSelected(AbcMethod method);
}
