package com.tasomaniac.dashclock.hackerspace;

import android.app.Application;

import com.tasomaniac.android.widget.IntegrationPreference;
import com.tasomaniac.dashclock.hackerspace.ui.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                AppModule.class,
                AnalyticsModule.class
        }, dependencies = Application.class)
public interface HackDashComponent {

    void inject(StatusService service);

    void inject(StatusToastReceiver receiver);

    void inject(IntegrationPreference integrationPreference);

    void inject(SettingsFragment fragment);

}
