package com.tasomaniac.hackerspace.status;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class })
public interface HackDashComponent {

    void inject(App app);
    void inject(ChooseHackerSpaceActivity activity);
    void inject(StatusService service);
    void inject(StatusToastReceiver receiver);

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
