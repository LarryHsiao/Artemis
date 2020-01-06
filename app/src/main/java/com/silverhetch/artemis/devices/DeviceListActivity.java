package com.silverhetch.artemis.devices;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverhetch.artemis.devices.browsing.BrowseActivity;
import com.silverhetch.aura.AuraActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Activity to showing devices discovered by internet broadcasting.
 */
public class DeviceListActivity extends AuraActivity {
    private static final int PORT = 24000;
    private final HashMap<String, Target> devices = new HashMap<>();
    private boolean running = true;
    private ArrayAdapter<Target> adapter;
    private DatagramSocket socket;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<Target>(this, android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                final Target item = getItem(position);
                TextView textView = ((TextView) super.getView(position, convertView, parent));
                if (item == null) {
                    textView.setText("");
                } else {
                    textView.setText(item.hostName());
                }
                return textView;
            }
        };
        final ListView list = new ListView(this);
        list.setOnItemClickListener((parent, view, position, id) -> {
            final Target item = adapter.getItem(position);
            if (item == null) {
                return;
            }
            startActivity(BrowseActivity.newIntent(
                    this,
                    item.hostName()
            ));
        });
        list.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        list.setAdapter(adapter);
        setContentView(list);

        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        new Thread(() -> {
            try {
                while (running) {
                    final byte[] buffer = new byte[1024];
                    final DatagramPacket packet = new DatagramPacket(buffer, 1024);
                    socket.receive(packet);
                    final String msg = new String(buffer);
                    final String hostName = packet.getAddress().getHostName();
                    runOnUiThread(() -> {
                        try {
                            devices.put(
                                    hostName,
                                    new JsonTarget(hostName, new JSONObject(msg))
                            );
                            adapter.clear();
                            adapter.addAll(devices.values());
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
//                    targetData.put(
//                            packet.getAddress().toString(),
//                            new ConstTarget(
//                                    packet.getAddress(),
//                                    new String(buffer).trim()
//                            )
//                    );
//                    Platform.runLater(this::updateList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;

    }
}
