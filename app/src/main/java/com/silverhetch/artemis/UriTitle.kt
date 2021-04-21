package com.silverhetch.artemis

import android.net.Uri
import com.larryhsiao.clotho.Source

/**
 * Source to build title from uri.
 */
class UriTitle(private val uri: Uri) : Source<String> {
    override fun value(): String {
        return try {
            if (uri.toString().startsWith("content")) {
                "(?!(.*\\/(?=.+\$))).*".toRegex()
                    .find(uri.lastPathSegment ?: "")?.value ?: ""
            } else {
                uri.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
