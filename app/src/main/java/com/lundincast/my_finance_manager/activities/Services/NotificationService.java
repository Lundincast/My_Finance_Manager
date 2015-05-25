package com.lundincast.my_finance_manager.activities.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.lundincast.my_finance_manager.R;
import com.lundincast.my_finance_manager.activities.CreateTransactionActivity;
import com.lundincast.my_finance_manager.activities.MainActivity;
import com.lundincast.my_finance_manager.activities.data.TransactionDataSource;

import java.sql.SQLException;
import java.util.Date;

public class NotificationService extends Service {

    private NotificationManager mManager;

    public NotificationService() {
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationEnabled = sharedPref.getBoolean("pref_key_daily_reminder", false);
        boolean adaptedEnabled = sharedPref.getBoolean("pref_key_adapted_reminder", true);

        if (notificationEnabled) {
            if (adaptedEnabled) {
                TransactionDataSource datasource = new TransactionDataSource(getApplicationContext());
                try {
                    datasource.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Cursor cursor = datasource.getTransactionByDate(new Date());
                if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                    setupNotification();
                }
            } else {
                setupNotification();
            }

        }

    }

    private void setupNotification() {
        mManager = (NotificationManager) this.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        PendingIntent pendingAddIntent = PendingIntent.getActivity(
                this, 0, new Intent(getApplicationContext(), CreateTransactionActivity.class), PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingListIntent = PendingIntent.getActivity(
                this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.appicon)
                .setContentTitle("Too good to be true !")
                .setContentText("Haven't you spent any money today ?")
                .addAction(R.drawable.ic_action_new, "Add expense", pendingAddIntent)
                .addAction(R.drawable.ic_action_overflow, "View list", pendingListIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
