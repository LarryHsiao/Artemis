package com.silverhetch.artemis

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.silverhetch.clotho.Source

/**
 * Source to build media title from given content:// uri.
 */
class MediaUriTitle(
    private val context: Context,
    private val uri: Uri
) : Source<String> {
    override fun value(): String = context.contentResolver.query(
        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
    )?.use { query ->
        query.moveToFirst()
        return query.getString(query.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)) ?: ""
    } ?: ""
}