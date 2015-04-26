package com.lundincast.my_finance_manager.activities.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lundincast.my_finance_manager.activities.Services.NotificationService;

/**
 * Created by lundincast on 23/04/15.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, NotificationService.class);
        context.startService(service);
    }
}
