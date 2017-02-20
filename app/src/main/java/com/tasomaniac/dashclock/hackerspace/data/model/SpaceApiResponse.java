package com.tasomaniac.dashclock.hackerspace.data.model;

public class SpaceApiResponse {

    private String space;
    private String url;
    private State state;

    public State getState() {
        return state;
    }

    public String getSpace() {
        return space;
    }

    public String getUrl() {
        return url;
    }
}
