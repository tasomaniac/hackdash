package com.tasomaniac.hackerspace.status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.json.JSONObject;

public class StatusService extends DashClockExtension {

    public static final String SETTINGS_CHANGED_EVENT = "settings_changed";
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
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);

        if (mForceUpdateReceiver != null) {
            try {
                unregisterReceiver(mForceUpdateReceiver);
            } catch (Exception e) {
            }
        }
        IntentFilter intentFilter = new IntentFilter(SETTINGS_CHANGED_EVENT);
        mForceUpdateReceiver = new ForceUpdateReceiver();
        registerReceiver(mForceUpdateReceiver, intentFilter);
    }

    @Override
    protected void onUpdateData(int reason) {
        //TODO If there is no hackerspace chosen display a message to chose one.

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String name = sp.getString("space_name", "");

        if(TextUtils.isEmpty(name)) {
            // Publish the extension data update.
            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_hackerspace)
                    .status(getString(R.string.setup))
                    .expandedTitle(getString(R.string.settings_choose_title))
                    .expandedBody(getString(R.string.settings_choose_message))
                    .clickIntent(new Intent(this, ChooseHackerSpaceActivity.class)));
        }
        else  {
            String url = sp.getString("space_url", ""); //"https://istanbulhs.org/api/spaceapi";

            JsonObjectRequest request = new JsonObjectRequest(url, null,

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject jsonObject) {

                            JSONObject state = jsonObject.optJSONObject("state");
                            if(state != null) {
                                Boolean open = null;
                                if(!state.isNull("open"))
                                    open = state.optBoolean("open", false);

                                final String status = getString(open == null ? R.string.unknown : (open ? R.string.open : R.string.closed));
                                final String message = state.optString("message");


//                                String logo = jsonObject.optString("logo"); //TODO try to integrate this logo
                                publishHSUpdate(status, jsonObject.optString("space", name), message,
                                        jsonObject.optString("url"),
                                        open != null && open ? R.drawable.ic_action_good : R.drawable.ic_action_error);

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            publishUpdate(new ExtensionData().visible(false));
                        }
                    }
            );

            Volley.newRequestQueue(this).add(request);
        }
    }

    private void publishHSUpdate(String status, String title, String message, String url, int icon) {

        if(!TextUtils.isEmpty(message))
            message = status + " | " + message;
        else
            message = getString(R.string.status_message, status);

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
