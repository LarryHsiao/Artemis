package com.silverhetch.artemis.media;

import android.database.Cursor;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.source.ConstSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source to build media list from cursor.
 */
public class QueriedVideoList implements Source<List<Media>> {
    private final Source<Cursor> origin;

    public QueriedVideoList(Source<Cursor> origin) {
        this.origin = origin;
    }

    @Override
    public List<Media> value() {
        try (final Cursor cursor = origin.value()) {
            final List<Media> media = new ArrayList<>();
            if (cursor == null) {
                return Collections.emptyList();
            }
            while (cursor.moveToNext()) {
                media.add(new QueriedVideo(
                        new ConstSource<>(cursor), false
                ).value());
            }
            return media;
        }
    }
}
