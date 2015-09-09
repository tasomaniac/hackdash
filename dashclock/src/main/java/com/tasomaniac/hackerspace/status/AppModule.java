package com.tasomaniac.hackerspace.status;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.moshi.Moshi;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.tasomaniac.hackerspace.status.data.ChosenHackerSpaceName;
import com.tasomaniac.hackerspace.status.data.ChosenHackerSpaceUrl;
import com.tasomaniac.hackerspace.status.data.DirectoryConverter;
import com.tasomaniac.hackerspace.status.data.StringPreference;
import com.tasomaniac.hackerspace.status.data.model.Directory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.MoshiConverterFactory;
import retrofit.Retrofit;

@Module
final class AppModule {
    private final App app;

    AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton Application application() {
        return app;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides @Singleton @ChosenHackerSpaceName StringPreference provideChosenSpaceNamePreference(
            SharedPreferences prefs) {
        return new StringPreference(prefs, "space_name", null);
    }

    @Provides @Singleton @ChosenHackerSpaceUrl StringPreference provideChosenSpaceUrlPreference(
            SharedPreferences prefs) {
        return new StringPreference(prefs, "space_url", null);
    }

    @Provides @Singleton Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(Directory.class, new DirectoryConverter())
                .build();
    }

    @Provides @Singleton
    OkHttpClient provideOkHttpClient() {
        return createOkHttpClient(app);
    }

    static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);

        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, 50 * 1024 * 1024);
        client.setCache(cache);

        /** Dangerous interceptor that rewrites the server's cache-control header. */
        client.interceptors().add(new Interceptor() {
            @Override public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .header("Cache-Control", "max-age=300")
                        .build();
            }
        });

        return client;
    }

    public static final HttpUrl BASE_API_URL = HttpUrl.parse("http://spaceapi.net/");

    @Provides @Singleton HttpUrl provideBaseUrl() {
        return BASE_API_URL;
    }

    @Provides @Singleton
    Retrofit provideRetrofit(HttpUrl baseUrl, OkHttpClient client, Moshi moshi) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides @Singleton SpaceApiService provideSpaceApiService(Retrofit retrofit) {
        return retrofit.create(SpaceApiService.class);
    }
}
