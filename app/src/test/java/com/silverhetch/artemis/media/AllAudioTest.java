package com.silverhetch.artemis.media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertTrue;


/**
 * Test for {@link AllAudio}.
 */
@RunWith(RobolectricTestRunner.class)
public class AllAudioTest {
    /**
     * Check the input valid.
     */
    @Test
    public void simple() {
        for (Media media : new QueriedAudioList(new AllAudio(RuntimeEnvironment.application)).value()) {
            System.out.println(media.title());
        }
        assertTrue(true); // If it can run through normally.
    }
}