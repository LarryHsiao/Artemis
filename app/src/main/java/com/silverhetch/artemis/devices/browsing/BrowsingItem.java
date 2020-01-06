package com.silverhetch.artemis.devices.browsing;

/**
 * Item of remote files.
 */
public interface BrowsingItem {
    /**
     * Name of this file
     */
    String name();

    /**
     * Is this file a directory
     */
    boolean isDirectory();

    /**
     * Uri of this item.
     */
    String uri();
}
