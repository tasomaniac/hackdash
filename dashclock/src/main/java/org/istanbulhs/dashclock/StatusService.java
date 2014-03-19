package org.istanbulhs.dashclock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.google.android.apps.dashclock.api.DashClockExtension;

public class StatusService extends DashClockExtension {

    @Override
    protected void onUpdateData(int reason) {

        //TODO Get status of the chosen hackerspace

        //TODO If there is no hackerspace chosen display a message to chose one.

    }
}
