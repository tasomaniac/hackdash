package org.istanbulhs.dashclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.apps.dashclock.api.DashClockExtension;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;


public class ChooseHackerSpaceActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    private ProgressDialog pd;

    private RequestQueue queue;

    private ArrayList<HackerSpace> spaces;
    private HackerSpace choosen_space;
    private int choosen_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(pd == null) {
            try {
                pd = ProgressDialog.show(this, null, "Please wait!", true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                        queue.cancelAll(ChooseHackerSpaceActivity.this);
                    }
                });
            } catch (Exception e){}
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        choosen_space = new HackerSpace(prefs.getString("space_name", ""), prefs.getString("space_url", ""));
        queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest("http://spaceapi.net/directory.json?api=0.13", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        if(jsonObject == null) return;

                        spaces = new ArrayList<>();

                        Iterator<String> iterator = jsonObject.keys();
                        while(iterator.hasNext()) {
                            String key = iterator.next();
                            String value = jsonObject.optString(key);
                            if(value != null) {
                                spaces.add(new HackerSpace(key, value));
                            }
                        }
                        Collections.sort(spaces, new Comparator<HackerSpace>() {
                            @Override
                            public int compare(HackerSpace lhs, HackerSpace rhs) {
                                return lhs.toString().compareTo(rhs.toString());
                            }
                        });

                        choosen_index = spaces.indexOf(choosen_space);

                        try {
                            new AlertDialog.Builder(ChooseHackerSpaceActivity.this)
                                    .setTitle(R.string.settings_choose_title)
                                    .setSingleChoiceItems(new ArrayAdapter<>(ChooseHackerSpaceActivity.this, android.R.layout.select_dialog_singlechoice, spaces), choosen_index, ChooseHackerSpaceActivity.this)
                                    .setPositiveButton(android.R.string.ok, ChooseHackerSpaceActivity.this)
                                    .setNegativeButton(android.R.string.cancel, ChooseHackerSpaceActivity.this)
                                    .setCancelable(true)
                                    .setOnCancelListener(ChooseHackerSpaceActivity.this)
                                    .show();
                        } catch (Exception e) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        try {
                            new AlertDialog.Builder(ChooseHackerSpaceActivity.this)
                                    .setTitle(R.string.error_title)
                                    .setMessage(R.string.error_message)
                                    .setNegativeButton(android.R.string.ok, ChooseHackerSpaceActivity.this)
                                    .setOnCancelListener(ChooseHackerSpaceActivity.this)
                                    .show();
                        } catch (Exception e) {
                            try {
                                Toast.makeText(ChooseHackerSpaceActivity.this, R.string.error_message, Toast.LENGTH_SHORT).show();
                            } catch (Exception e2) {}
                        }
                    }
                }
        );
        queue.add(request);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if(which >= 0) {
            choosen_index = which;
        }
        else if(which == DialogInterface.BUTTON_POSITIVE) {
            HackerSpace chosen_space = choosen_index > 0 ? spaces.get(choosen_index) : null;
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("space_name", chosen_space != null ? chosen_space.space : null)
                    .putString("space_url", chosen_space != null ? chosen_space.url : null)
                    .apply();
            sendBroadcast(new Intent(StatusService.SETTINGS_CHANGED_EVENT));

            finish();
        }
        else
            finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

}