package com.silverhetch.artemis.media;

import android.content.Context;
import android.database.Cursor;

import com.silverhetch.clotho.Source;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.RELATIVE_PATH;
import static android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Source to find all audio files on the Phone with content provider.
 */
public class AllVideo implements Source<Cursor> {
    private final Context context;

    public AllVideo(Context context) {
        this.context = context;
    }

    @Override
    public Cursor value() {
        return context.getContentResolver().query(
                EXTERNAL_CONTENT_URI,
                new String[]{_ID, TITLE, RELATIVE_PATH},
                null,
                null,
                null
        );
    }
}
