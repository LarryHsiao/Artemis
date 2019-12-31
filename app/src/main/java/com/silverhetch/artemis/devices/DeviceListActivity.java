package com.silverhetch.artemis.devices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import com.silverhetch.artemis.PlayerActivity;
import com.silverhetch.aura.AuraActivity;

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
    private final HashMap<String, String> devices = new HashMap<>();
    private boolean running = true;
    private ArrayAdapter<String> adapter;
    private DatagramSocket socket;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        final ListView list = new ListView(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(DeviceListActivity.this, PlayerActivity.class);
                intent.setData(Uri.parse(
                        "http:/"+adapter.getItem(position)+":8080/Dropbox/Elizabeth/MediaSamples/WeAreGoingOnBullrun.mp4"
                ));
                startActivity(intent);
            }
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
                    runOnUiThread(() -> {
//                        try {
                        devices.put(
                                packet.getAddress().toString(),
//                                    new JSONObject(msg).getString("name")
                                packet.getAddress().toString()
                        );
                        adapter.clear();
                        adapter.addAll(devices.values());
                        adapter.notifyDataSetChanged();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
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
