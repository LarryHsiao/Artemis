package com.silverhetch.artemis.media;

import android.content.Context;
import android.database.Cursor;
import com.silverhetch.clotho.Source;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Source to find all audio files on the Phone
 */
public class AllAudio implements Source<Cursor> {
    private final Context context;

    public AllAudio(Context context) {
        this.context = context;
    }

    @Override
    public Cursor value() {
        return context.getContentResolver().query(
                EXTERNAL_CONTENT_URI,
                new String[]{_ID, TITLE},
                null,
                null,
                null
        );
    }
}
