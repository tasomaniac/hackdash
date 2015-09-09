package com.tasomaniac.hackerspace.status.data.model;

public class State {

    private Boolean open;
    private String message;
    private Long lastchange;

    public Boolean isOpen() {
        return open;
    }

    public String getMessage() {
        return message;
    }

    public Long getLastchange() {
        return lastchange;
    }
}
