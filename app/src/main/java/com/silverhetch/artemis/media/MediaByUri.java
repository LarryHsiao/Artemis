package com.silverhetch.artemis.media;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import com.silverhetch.clotho.Source;

/**
 * Source to build {@link Media} by uri
 */
public class MediaByUri implements Source<Cursor> {
    private final Context context;
    private final Uri uri;

    public MediaByUri(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public Cursor value() {
        final Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToNext()) {
            return cursor;
        } else {
            throw new Resources.NotFoundException("Media not found: " + uri);
        }
    }
}
