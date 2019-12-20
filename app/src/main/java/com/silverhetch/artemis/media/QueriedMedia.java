package com.silverhetch.artemis.media;

import android.content.ContentUris;
import android.database.Cursor;
import com.silverhetch.clotho.Source;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Source to build media from cursor.
 */
public class QueriedMedia implements Source<Media> {
    private final Source<Cursor> origin;
    private final boolean autoClose;

    public QueriedMedia(Source<Cursor> origin) {
        this(origin, true);
    }

    public QueriedMedia(Source<Cursor> origin, boolean autoClose) {
        this.origin = origin;
        this.autoClose = autoClose;
    }

    @Override
    public Media value() {
        final Cursor cursor = origin.value();
        try {
            return new ConstMedia(
                    cursor.getString(cursor.getColumnIndex(TITLE)),
                    ContentUris.withAppendedId(
                            EXTERNAL_CONTENT_URI,
                            cursor.getInt(cursor.getColumnIndex(_ID))
                    ).toString()
            );
        } finally {
            if (autoClose) {
                cursor.close();
            }
        }
    }
}