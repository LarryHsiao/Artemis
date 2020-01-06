package com.silverhetch.artemis;

import android.app.Application;

import okhttp3.OkHttpClient;

/**
 * Application implementation of this app.
 */
public class ArtemisApp extends Application {
    public final OkHttpClient httpClient;

    public ArtemisApp(){
        httpClient = new OkHttpClient.Builder()
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

