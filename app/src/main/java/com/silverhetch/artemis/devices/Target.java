package com.silverhetch.artemis.devices;

/**
 * The target device of Auxo.
 */
public interface Target {
    /**
     * Name of this device target.
     */
    String name();

    /**
     * Host name
     */
    String hostName();
}
