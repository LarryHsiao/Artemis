package com.silverhetch.artemis.player;

import android.graphics.Point;
import android.view.SurfaceHolder;
import androidx.lifecycle.LiveData;
import com.silverhetch.aura.media.AuraMediaPlayer;
import com.silverhetch.aura.media.PlaybackControl;
import com.silverhetch.aura.media.State;
import org.jetbrains.annotations.NotNull;

public class PlayerWrapper implements AuraMediaPlayer {
    private final AuraMediaPlayer player;

    public PlayerWrapper(AuraMediaPlayer player) {
        this.player = player;
    }

    @Override
    public void attachDisplay(@NotNull SurfaceHolder surfaceHolder) {
        player.attachDisplay(surfaceHolder);
    }

    @Override
    public void detachDisplay() {
        player.detachDisplay();
    }

    @Override
    public void load(@NotNull String s) {
        player.load(s);
    }

    @NotNull
    @Override
    public PlaybackControl playback() {
        return player.playback();
    }

    @Override
    public void release() {
        player.release();
    }

    @NotNull
    @Override
    public LiveData<State> state() {
        return player.state();
    }

    @NotNull
    @Override
    public LiveData<Point> videoSize() {
        return player.videoSize();
    }
}
