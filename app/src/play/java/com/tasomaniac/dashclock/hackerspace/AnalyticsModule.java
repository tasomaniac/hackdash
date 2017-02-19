package com.tasomaniac.dashclock.hackerspace;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    Analytics provideAnalytics(Application app) {
        if (BuildConfig.DEBUG) {
            return new Analytics.DebugAnalytics();
        }

        com.google.android.gms.analytics.GoogleAnalytics googleAnalytics = com.google.android.gms.analytics.GoogleAnalytics.getInstance(app);
        Tracker tracker = googleAnalytics.newTracker(BuildConfig.ANALYTICS_KEY);
        tracker.setSessionTimeout(300); // ms? s? better be s.
        return new GoogleAnalytics(tracker);
    }

}
