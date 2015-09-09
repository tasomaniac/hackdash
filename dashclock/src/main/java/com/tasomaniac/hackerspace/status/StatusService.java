package com.tasomaniac.hackerspace.status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.tasomaniac.hackerspace.status.data.HackerSpacePreference;
import com.tasomaniac.hackerspace.status.data.model.HackerSpace;
import com.tasomaniac.hackerspace.status.data.model.SpaceApiResponse;
import com.tasomaniac.hackerspace.status.data.model.State;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class StatusService extends DashClockExtension {

    public static final String SETTINGS_CHANGED_EVENT = BuildConfig.APPLICATION_ID + "settings_changed";

    @Inject
    HackerSpacePreference hackerSpacePreference;
    @Inject
    SpaceApiService spaceApiService;

    ForceUpdateReceiver mForceUpdateReceiver;

    class ForceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_SETTINGS_CHANGED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mForceUpdateReceiver != null) {
            try {
                unregisterReceiver(mForceUpdateReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        App.get(this).component().inject(this);

        if (mForceUpdateReceiver != null) {
            try {
                unregisterReceiver(mForceUpdateReceiver);
            } catch (Exception ignored) {
            }
        }
        IntentFilter intentFilter = new IntentFilter(SETTINGS_CHANGED_EVENT);
        mForceUpdateReceiver = new ForceUpdateReceiver();
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
                    .clickIntent(new Intent(this, ChooseHackerSpaceActivity.class)));
        } else {
            spaceApiService.spaceStatus(chosenSpace.url).enqueue(new Callback<SpaceApiResponse>() {
                @Override
                public void onResponse(Response<SpaceApiResponse> response) {
                    if (!response.isSuccess()) {
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
                            message.append(DateFormat.getTimeInstance(DateFormat.MEDIUM)
                                    .format(new Date(when)));
                        } else {
                            message.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                    DateFormat.MEDIUM).format(new Date(when)));
                        }
                    }

//                  String logo = jsonObject.optString("logo"); //TODO try to integrate this logo
                    publishHSUpdate(status,
                            body.getSpace(),
                            message.toString(),
                            body.getUrl(),
                            open != null && open ? R.drawable.ic_action_good : R.drawable.ic_action_error);
                }

                @Override
                public void onFailure(Throwable t) {
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
