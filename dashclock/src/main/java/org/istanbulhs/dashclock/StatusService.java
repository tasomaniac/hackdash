package org.istanbulhs.dashclock;

import android.content.Intent;
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

    @Override
    protected void onUpdateData(int reason) {
        //TODO If there is no hackerspace chosen display a message to chose one.

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String name = sp.getString("space_name", "");

        if(TextUtils.isEmpty(name)) {
            // Publish the extension data update.
            publishUpdate(new ExtensionData()
                    .visible(true)
                            //                .icon(R.drawable.ic_extension_example)
                    .status("Setup")
                    .expandedTitle("Choose your Hackerspace")
                    .expandedBody("Click here to choose your Hackerspace!")
                    .clickIntent(new Intent(this, MainActivity.class)));
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
                                final String message = jsonObject.optString("message");

                                String logo = jsonObject.optString("logo"); //TODO try to integrate this logo
                                publishHSUpdate(status, jsonObject.optString("space", name), message, jsonObject.optString("url"));

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }
            );

            Volley.newRequestQueue(this).add(request);
        }
    }

    private void publishHSUpdate(String status, String title, String message, String url) {

        if(!TextUtils.isEmpty(message))
            message = status + " | " + message;
        else
            message = getString(R.string.status_message, status);

        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(true)
//                .iconUri(iconUri)
                .status(status)
                .expandedTitle(title)
                .expandedBody(message)
                .clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
    }

}
