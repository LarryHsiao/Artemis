package com.silverhetch.artemis;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.silverhetch.artemis.media.*;
import com.silverhetch.aura.AuraActivity;
import com.silverhetch.clotho.utility.comparator.StringComparator;

import java.util.Comparator;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.N;
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
        final List<Media> media = new QueriedVideoList(new AllVideo(this)).value();
        if (SDK_INT >= N) {
            media.sort(new Comparator<Media>() {
                private final Comparator<String> comparator = new StringComparator();

                @Override
                public int compare(Media o1, Media o2) {
                    return comparator.compare(o2.title(), o1.title());
                }
            });
        }
        adapter.load(media);
    }
}
