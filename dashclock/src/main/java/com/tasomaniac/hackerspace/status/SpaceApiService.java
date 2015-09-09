package com.tasomaniac.hackerspace.status;

import com.tasomaniac.hackerspace.status.data.model.Directory;
import com.tasomaniac.hackerspace.status.data.model.SpaceApiResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Url;

public interface SpaceApiService {

    @GET("directory.json?api=0.13")
    Call<Directory> directory();

    @GET
    Call<SpaceApiResponse> spaceStatus(@Url String url);

}