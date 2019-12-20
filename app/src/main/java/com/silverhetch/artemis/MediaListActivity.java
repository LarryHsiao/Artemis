package com.silverhetch.artemis;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.silverhetch.artemis.media.AllAudio;
import com.silverhetch.artemis.media.Media;
import com.silverhetch.aura.AuraActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Activity that shows the media list on the screen.
 */
public class MediaListActivity extends AuraActivity {
    private final MediaAdapter adapter = new MediaAdapter(new MediaAdapter.OnClickListener() {
        @Override
        public void onClicked(Media media) {
            final Intent intent = new Intent(MediaListActivity.this, MediaPlayerService.class);
            intent.setData(Uri.parse(media.uri()));
            startService(intent);
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final RecyclerView list = new RecyclerView(this);
        list.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        setContentView(list);

        requestPermissionsByObj(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onPermissionGranted() {
        super.onPermissionGranted();
        adapter.load(new AllAudio(this).value());
    }
}
