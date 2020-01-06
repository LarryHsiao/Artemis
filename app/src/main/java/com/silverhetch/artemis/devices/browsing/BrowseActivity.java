package com.silverhetch.artemis.devices.browsing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverhetch.artemis.ArtemisApp;
import com.silverhetch.artemis.PlayerActivity;
import com.silverhetch.artemis.R;
import com.silverhetch.aura.AuraActivity;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Activity to browsing device's content.
 */
public class BrowseActivity extends AuraActivity {
    private static final String ARG_HOST_NAME = "ARG_HOST_NAME";
    private final List<String> path = new ArrayList<>();
    private ArrayAdapter<BrowsingItem> adapter;

    public static Intent newIntent(Context context, String hostName) {
        final Intent intent = new Intent(context, BrowseActivity.class);
        intent.putExtra(ARG_HOST_NAME, hostName);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<BrowsingItem>(this, android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                final BrowsingItem item = getItem(position);
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setText(item.name());
                if (item.isDirectory()) {
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                            view.getContext().getResources().getDrawable(R.drawable.ic_folder),
                            null, null, null
                    );
                } else {
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
                            view.getContext().getResources().getDrawable(R.drawable.ic_file),
                            null, null, null
                    );
                }
                return view;
            }
        };
        final ListView list = new ListView(this);
        list.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        list.setAdapter(adapter);
        setContentView(list);
        list.setOnItemClickListener((parent, view, position, id) -> {
            final BrowsingItem item = adapter.getItem(position);
            if (item == null) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Not exist")
                        .show();
                return;
            }
            if (item.isDirectory()) {
                openDirectory(item);
            } else {
                openItem(item);
            }
        });

        loadPath();
    }

    private void openItem(BrowsingItem item) {
        final String hostName = getIntent().getStringExtra(ARG_HOST_NAME);
        final String mimeType = URLConnection.guessContentTypeFromName(item.name());
        if (mimeType == null) {
            new AlertDialog.Builder(this)
                    .setMessage("Invalid file")
                    .show();
            return;
        }
        if (mimeType.startsWith("image")) {
            new StfalconImageViewer.Builder<>(
                    this,
                    new BrowsingItem[]{item},
                    (imageView, image) -> Picasso.get().load(
                            pathUrl() + "/" + image.name()
                    ).into(imageView)
            ).show();
        }

        if (mimeType.startsWith("video") || mimeType.startsWith("audio")) {
            final Intent intent = new Intent(this, PlayerActivity.class);
            intent.setData(Uri.parse("http://" + hostName + "/" + item.name()));
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (path.size() > 0) {
            path.remove(path.size() - 1);
            loadPath();
        } else {
            super.onBackPressed();
        }
    }

    private void openDirectory(BrowsingItem item) {
        path.add(item.name());
        loadPath();
    }

    private String pathUrl() {
        final String hostName = getIntent().getStringExtra(ARG_HOST_NAME);
        final StringBuilder builder = new StringBuilder();
        builder.append("http://").append(hostName);
        for (String part : path) {
            builder.append('/');
            builder.append(part);
        }
        return builder.toString();
    }

    private void loadPath() {
        ((ArtemisApp) getApplicationContext()).httpClient.newCall(new Request.Builder()
                .url(pathUrl())
                .build()
        ).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                final StringWriter error = new StringWriter();
                e.printStackTrace(new PrintWriter(error));
                new AlertDialog.Builder(BrowseActivity.this)
                        .setTitle(R.string.error)
                        .setPositiveButton(R.string.app_confirm, (dialog, which) -> {
                        })
                        .setMessage(error.toString())
                        .show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    final List<BrowsingItem> value = new JsonItems(new String(response.body().bytes())).value();
                    Collections.sort(value, new Comparator<BrowsingItem>() {
                        final StringComparator comparator = new StringComparator();

                        @Override
                        public int compare(BrowsingItem o1, BrowsingItem o2) {
                            if (o1.isDirectory() && !o2.isDirectory()) {
                                return -1;
                            } else if (!o1.isDirectory() && o2.isDirectory()) {
                                return 1;
                            } else {
                                return comparator.compare(o2.name(), o1.name());
                            }
                        }
                    });
                    runOnUiThread(() -> {
                        adapter.clear();
                        adapter.addAll(value);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}
