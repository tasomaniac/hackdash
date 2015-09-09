package com.tasomaniac.hackerspace.status.data;

import com.tasomaniac.hackerspace.status.data.model.HackerSpace;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HackerSpacePreference {
    private final StringPreference spaceNamePreference;
    private final StringPreference spaceUrlPreference;

    @Inject
    public HackerSpacePreference(@ChosenHackerSpaceName StringPreference spaceNamePreference,
                                 @ChosenHackerSpaceUrl StringPreference spaceUrlPreference) {
        this.spaceNamePreference = spaceNamePreference;
        this.spaceUrlPreference = spaceUrlPreference;
    }

    public void saveHackerSpace(HackerSpace space) {
        if (space != null) {
            spaceNamePreference.set(space.space);
            spaceUrlPreference.set(space.url);
        } else {
            spaceNamePreference.delete();
            spaceUrlPreference.delete();
        }
    }

    public HackerSpace getHackerSpace() {
        return new HackerSpace(spaceNamePreference.get(), spaceUrlPreference.get());
    }
}
