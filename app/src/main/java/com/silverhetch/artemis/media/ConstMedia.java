package com.silverhetch.artemis.media;

/**
 * Constant implementation of {@link Media}
 */
public class ConstMedia implements Media {
    private final String title;
    private final String uri;
    private final String relativePath;

    public ConstMedia(String title, String uri, String relativePath) {
        this.title = title;
        this.uri = uri;
        this.relativePath = relativePath;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String relativePath() {
        return relativePath;
    }
}
