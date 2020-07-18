package com.silverhetch.artemis.regex

import org.junit.Assert
import org.junit.Test

/**
 * Test for regex.
 */
class UriRegexMatchTest{

    /**
     * Test uri Regex matches to last part of name.
     */
    @Test
    fun simple() {
        Assert.assertEquals(
            "File name.mp4",
            "(?!(.*\\/(?=.+\$))).*".toRegex().find(
                "raw:/storage/emulated/0/Download/File name.mp4"
            )?.value
        )
    }
}