package com.silverhetch.artemis.devices.browsing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Json of browsing item.
 */
public class JsonItem implements BrowsingItem {
    private final JSONObject obj;

    public JsonItem(JSONObject obj) {
        this.obj = obj;
    }

    @Override
    public String name() {
        try {
            return obj.getString("name");
        } catch (Exception e) {
            return "Known";
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            return obj.getBoolean("isDirectory");
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    public String uri() {
        try {
            return obj.getString("uri");
        } catch (JSONException e) {
            throw new RuntimeException("Missing uri, item: " + obj.toString());
        }
    }
}
