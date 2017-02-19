package com.tasomaniac.dashclock.hackerspace;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

class GoogleAnalytics implements Analytics {
    private final Tracker tracker;

    GoogleAnalytics(Tracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void sendScreenView(String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void sendEvent(String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder()
                             .setCategory(category)
                             .setAction(action)
                             .setLabel(label)
                             .build());
    }
}
