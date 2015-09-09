package com.tasomaniac.hackerspace.status.data.model;

public class HackerSpace {

    public String space;
    public String url;

    public HackerSpace(String space, String url) {
        this.space = space;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof HackerSpace && this.url != null && this.url.equals( ((HackerSpace)o).url );
    }

    @Override
    public String toString() {
        return space;
    }
}
