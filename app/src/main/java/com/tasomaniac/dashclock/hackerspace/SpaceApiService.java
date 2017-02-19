package com.tasomaniac.dashclock.hackerspace;

import com.tasomaniac.dashclock.hackerspace.data.model.Directory;
import com.tasomaniac.dashclock.hackerspace.data.model.SpaceApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface SpaceApiService {

    @GET("directory.json?api=0.13")
    Call<Directory> directory();

    @GET
    Call<SpaceApiResponse> spaceStatus(@Url String url);

}
