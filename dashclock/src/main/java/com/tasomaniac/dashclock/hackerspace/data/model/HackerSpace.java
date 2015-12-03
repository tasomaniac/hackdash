package com.tasomaniac.dashclock.hackerspace.data.model;

import android.support.annotation.NonNull;

public class HackerSpace implements Comparable {

    public String space;
    public String url;

    public HackerSpace(String space, String url) {
        this.space = space;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof HackerSpace && this.compareTo(o) == 0;
    }

    @Override
    public String toString() {
        return space;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return toString().compareTo(another.toString());
    }
}
