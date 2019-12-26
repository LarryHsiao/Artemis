package com.silverhetch.artemis.media;

import android.content.ContentUris;
import android.database.Cursor;

import com.silverhetch.clotho.Source;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.RELATIVE_PATH;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Source to build media from cursor.
 */
public class QueriedAudio implements Source<Media> {
    private final Source<Cursor> origin;
    private final boolean autoClose;

    public QueriedAudio(Source<Cursor> origin) {
        this(origin, true);
    }

    public QueriedAudio(Source<Cursor> origin, boolean autoClose) {
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
                    ).toString(),
                    cursor.getString(cursor.getColumnIndex(RELATIVE_PATH))
            );
        } finally {
            if (autoClose) {
                cursor.close();
            }
        }
    }
}
