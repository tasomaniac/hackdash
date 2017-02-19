package com.tasomaniac.dashclock.hackerspace;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public class BaseApp extends Application {
    private HackDashComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        component = DaggerHackDashComponent.builder()
                .application(this)
                .build();
    }

    public HackDashComponent component() {
        return component;
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
}
