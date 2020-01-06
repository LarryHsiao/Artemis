package com.silverhetch.artemis.devices.browsing;

import com.silverhetch.clotho.Source;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Browsing items from json array.
 */
public class JsonItems implements Source<List<BrowsingItem>> {
    private final String jsonArrayStr;

    public JsonItems(String arrayStr) {
        this.jsonArrayStr = arrayStr;
    }

    @Override
    public List<BrowsingItem> value() {
        final List<BrowsingItem> result = new ArrayList<>();
        try {
            final JSONArray arr = new JSONArray(jsonArrayStr);
            for (int i = 0; i < arr.length(); i++) {
                result.add(new JsonItem(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {

        }
        return result;
    }
}
