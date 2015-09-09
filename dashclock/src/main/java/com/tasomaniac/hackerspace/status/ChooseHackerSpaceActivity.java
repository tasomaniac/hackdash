package com.tasomaniac.hackerspace.status;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.tasomaniac.hackerspace.status.data.HackerSpacePreference;
import com.tasomaniac.hackerspace.status.data.model.Directory;
import com.tasomaniac.hackerspace.status.data.model.HackerSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;


public class ChooseHackerSpaceActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    private ProgressDialog pd;

    @Inject
    SpaceApiService spaceApiService;
    @Inject
    HackerSpacePreference chosenSpacePref;

    private ArrayList<HackerSpace> spaces;
    private int chosenIndex;
    private Call<Directory> directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get(this).component().inject(this);

        if (pd == null) {
            try {
                pd = ProgressDialog.show(this, null, getString(R.string.please_wait), true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (directory != null) {
                            directory.cancel();
                        }
                        finish();
                    }
                });
            } catch (Exception ignored) {
            }
        }

        directory = spaceApiService.directory();
        directory.enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Response<Directory> response) {
                if (!response.isSuccess()) {
                    showError();
                    return;
                }

                spaces = response.body();
                Collections.sort(spaces, new Comparator<HackerSpace>() {
                    @Override
                    public int compare(HackerSpace lhs, HackerSpace rhs) {
                        return lhs.toString().compareTo(rhs.toString());
                    }
                });

                chosenIndex = spaces.indexOf(chosenSpacePref.getHackerSpace());

                try {
                    new AlertDialog.Builder(ChooseHackerSpaceActivity.this)
                            .setTitle(R.string.settings_choose_title)
                            .setSingleChoiceItems(new ArrayAdapter<>(ChooseHackerSpaceActivity.this, android.R.layout.select_dialog_singlechoice, spaces), chosenIndex, ChooseHackerSpaceActivity.this)
                            .setPositiveButton(android.R.string.ok, ChooseHackerSpaceActivity.this)
                            .setNegativeButton(android.R.string.cancel, ChooseHackerSpaceActivity.this)
                            .setCancelable(true)
                            .setOnCancelListener(ChooseHackerSpaceActivity.this)
                            .show();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showError();
            }
        });
    }

    private void showError() {
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
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which >= 0) {
            chosenIndex = which;
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            chosenSpacePref.saveHackerSpace(chosenIndex > 0 ? spaces.get(chosenIndex) : null);
            sendBroadcast(new Intent(StatusService.SETTINGS_CHANGED_EVENT));

            finish();
        } else {
            finish();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

}