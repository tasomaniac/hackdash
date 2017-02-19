package com.tasomaniac.dashclock.hackerspace;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    static Analytics provideAnalytics() {
        return new Analytics.DebugAnalytics();
    }

}
