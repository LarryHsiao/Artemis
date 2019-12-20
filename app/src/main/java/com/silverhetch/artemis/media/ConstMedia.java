package com.silverhetch.artemis.media;

/**
 * Constant implementation of {@link Media}
 */
public class ConstMedia implements Media {
    private final String title;
    private final String uri;

    public ConstMedia(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String uri() {
        return uri;
    }
}
