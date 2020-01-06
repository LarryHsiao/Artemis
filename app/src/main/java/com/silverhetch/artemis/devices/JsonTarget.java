package com.silverhetch.artemis.devices;

import org.json.JSONObject;

/**
 * Json parsing object of {@link Target}
 */
public class JsonTarget implements Target {
    private final String hostName;
    private final JSONObject json;

    public JsonTarget(String hostName, JSONObject json) {
        this.hostName = hostName;
        this.json = json;
    }

    @Override
    public String name() {
        try {
            return json.getString("name");
        } catch (Exception e) {
            return "Unknown device";
        }
    }

    @Override
    public String hostName() {
        try {
            return hostName + ":" + json.getInt("port");
        } catch (Exception e) {
            return hostName + ":" + 24001; // Just in case we fucked up with dynamic port parameter.
        }
    }
}
