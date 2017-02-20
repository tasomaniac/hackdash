package com.tasomaniac.dashclock.hackerspace;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.moshi.Moshi;
import com.tasomaniac.dashclock.hackerspace.data.ChosenHackerSpaceName;
import com.tasomaniac.dashclock.hackerspace.data.ChosenHackerSpaceUrl;
import com.tasomaniac.dashclock.hackerspace.data.DirectoryConverter;
import com.tasomaniac.dashclock.hackerspace.data.StringPreference;
import com.tasomaniac.dashclock.hackerspace.data.model.Directory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
final class AppModule {

    private static final HttpUrl BASE_API_URL = HttpUrl.parse("https://spaceapi.fixme.ch");

    @Provides
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Reusable
    @ChosenHackerSpaceName
    StringPreference provideChosenSpaceNamePreference(
            Application app,
            SharedPreferences prefs) {
        return new StringPreference(prefs, app.getString(R.string.pref_key_space_name), null);
    }

    @Provides
    @Reusable
    @ChosenHackerSpaceUrl
    StringPreference provideChosenSpaceUrlPreference(
            Application app,
            SharedPreferences prefs) {
        return new StringPreference(prefs, app.getString(R.string.pref_key_space_url), null);
    }

    @Provides
    @Singleton
    static Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(Directory.class, new DirectoryConverter())
                .build();
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(Application app) {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .cache(cache(app))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .header("Cache-Control", "max-age=300")
                                .build();
                    }
                }).build();
    }

    private static Cache cache(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        return new Cache(cacheDir, 50 * 1024 * 1024);
    }

    @Provides
    HttpUrl provideBaseUrl() {
        return BASE_API_URL;
    }

    @Provides
    Retrofit provideRetrofit(HttpUrl baseUrl, OkHttpClient client, Moshi moshi) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides
    @Singleton
    SpaceApiService provideSpaceApiService(Retrofit retrofit) {
        return retrofit.create(SpaceApiService.class);
    }
}
