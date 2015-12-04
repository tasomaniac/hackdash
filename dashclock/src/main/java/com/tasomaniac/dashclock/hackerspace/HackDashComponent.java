package com.tasomaniac.dashclock.hackerspace;

import com.tasomaniac.android.widget.IntegrationPreference;
import com.tasomaniac.dashclock.hackerspace.ui.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class })
public interface HackDashComponent {

    void inject(App app);
    void inject(StatusService service);
    void inject(StatusToastReceiver receiver);
    void inject(IntegrationPreference integrationPreference);
    void inject(SettingsFragment fragment);

    /**
     * An initializer that creates the graph from an application.
     */
    final class Initializer {
        static HackDashComponent init(App app) {
            return DaggerHackDashComponent.builder()
                    .appModule(new AppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
