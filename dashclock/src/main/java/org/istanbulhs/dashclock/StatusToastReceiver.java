package org.istanbulhs.dashclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by tasomaniac on 25/3/14.
 */
public class StatusToastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {


        String url = PreferenceManager.getDefaultSharedPreferences(context).getString("space_url", "");
        if(TextUtils.isEmpty(url))
            return;

        JsonObjectRequest request = new JsonObjectRequest(url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject jsonObject) {

                        JSONObject state = jsonObject.optJSONObject("state");
                        if(state != null) {
                            Boolean open = null;
                            if(!state.isNull("open"))
                                open = state.optBoolean("open", false);

                            final String status = context.getString(open == null ? R.string.unknown : (open ? R.string.open : R.string.closed));
                            final String name = jsonObject.optString("space");

                            Toast.makeText(context.getApplicationContext(), (name != null ? name : "Your hackerspace") + " is " + status, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }
        );

        Volley.newRequestQueue(context).add(request);

    }
}
