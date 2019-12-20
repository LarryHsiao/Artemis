package com.silverhetch.artemis.media;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.silverhetch.clotho.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Source to find all audio files on the Phone
 */
public class AllAudio implements Source<List<Media>> {
    private final Context context;

    public AllAudio(Context context) {
        this.context = context;
    }

    @Override
    public List<Media> value() {
        try (final Cursor cursor = context.getContentResolver().query(
                EXTERNAL_CONTENT_URI, new String[]{_ID, TITLE}, null, null, null
        )) {
            final List<Media> media = new ArrayList<>();
            if (cursor == null) {
                return Collections.emptyList();
            }
            while (cursor.moveToNext()) {
                media.add(new ConstMedia(
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        ContentUris.withAppendedId(
                                EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(_ID))
                        ).toString()
                ));
            }
            return media;
        }
    }
}
