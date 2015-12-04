package com.tasomaniac.dashclock.hackerspace.data;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.tasomaniac.dashclock.hackerspace.data.model.Directory;
import com.tasomaniac.dashclock.hackerspace.data.model.HackerSpace;

import java.io.IOException;

public class DirectoryConverter extends JsonAdapter<Directory> {

    @Override
    public void toJson(JsonWriter writer, Directory value) throws IOException {
    }

    @Override
    public Directory fromJson(JsonReader reader) throws IOException {
        Directory directory = new Directory();

        reader.beginObject();
        while (reader.hasNext()) {
            directory.add(new HackerSpace(reader.nextName(), reader.nextString()));
        }

        return directory;
    }
}
