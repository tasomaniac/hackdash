package com.tasomaniac.dashclock.hackerspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.tasomaniac.dashclock.hackerspace.data.HackerSpacePreference;
import com.tasomaniac.dashclock.hackerspace.data.model.HackerSpace;
import com.tasomaniac.dashclock.hackerspace.data.model.SpaceApiResponse;
import com.tasomaniac.dashclock.hackerspace.data.model.State;
import com.tasomaniac.dashclock.hackerspace.ui.SettingsActivity;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class StatusService extends DashClockExtension {

    public static final String SETTINGS_CHANGED_EVENT = BuildConfig.APPLICATION_ID + ".settings_changed";

    @Inject
    HackerSpacePreference hackerSpacePreference;
    @Inject
    SpaceApiService spaceApiService;
    @Inject Analytics analytics;

    BroadcastReceiver mForceUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_SETTINGS_CHANGED);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mForceUpdateReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        App.get(this).component().inject(this);

        try {
            unregisterReceiver(mForceUpdateReceiver);
        } catch (Exception ignored) {
        }
        IntentFilter intentFilter = new IntentFilter(SETTINGS_CHANGED_EVENT);
        registerReceiver(mForceUpdateReceiver, intentFilter);
    }

    @Override
    protected void onUpdateData(int reason) {
        Timber.d("Update requested " + reason);

        final HackerSpace chosenSpace = hackerSpacePreference.getHackerSpace();
        if (TextUtils.isEmpty(chosenSpace.space)) {
            // Publish the extension data update.
            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_hackerspace)
                    .status(getString(R.string.setup))
                    .expandedTitle(getString(R.string.settings_choose_title))
                    .expandedBody(getString(R.string.settings_choose_message))
                    .clickIntent(new Intent(this, SettingsActivity.class)));
        } else {
            spaceApiService.spaceStatus(chosenSpace.url).enqueue(new Callback<SpaceApiResponse>() {

                @Override
                public void onResponse(Call<SpaceApiResponse> call, Response<SpaceApiResponse> response) {
                    if (!response.isSuccessful()) {
                        try {
                            Timber.e("Network Error %s", response.errorBody().string());
                        } catch (IOException ignored) {
                        }
                        publishUpdate(new ExtensionData().visible(false));
                        return;
                    }
                    final SpaceApiResponse body = response.body();
                    final State state = body.getState();
                    if (state == null) {
                        return;
                    }

                    final Boolean open = state.isOpen();
                    final String status = getString(open == null
                            ? R.string.unknown : (open ? R.string.open : R.string.closed));

                    final StringBuilder message = new StringBuilder();

                    if (!TextUtils.isEmpty(state.getMessage())) {
                        message.append(status)
                                .append(" | ")
                                .append(state.getMessage());
                    } else {
                        message.append(getString(R.string.status_message, status));
                    }

                    if (state.getLastchange() != null) {
                        message.append("\nLast Change: ");

                        long when = state.getLastchange() * 1000;
                        if (DateUtils.isToday(when)) {
                            message.append(DateFormat.getTimeInstance(DateFormat.SHORT)
                                    .format(new Date(when)));
                        } else {
                            message.append(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                    DateFormat.SHORT).format(new Date(when)));
                        }
                    }
                    publishHSUpdate(status,
                            body.getSpace(),
                            message.toString(),
                            body.getUrl(),
                            open != null && open ? R.drawable.ic_action_good : R.drawable.ic_action_error);

                    analytics.sendScreenView("Status Update");
                    analytics.sendEvent("Status Update", "Publish Update", body.getSpace());
                }

                @Override
                public void onFailure(Call<SpaceApiResponse> call, Throwable t) {
                    Timber.d(t, "Network error. ");
                }
            });
        }
    }

    private void publishHSUpdate(String status, String title, String message, String url, int icon) {
        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(icon)
                .status(status)
                .expandedTitle(title)
                .expandedBody(message)
                .clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
    }

}
