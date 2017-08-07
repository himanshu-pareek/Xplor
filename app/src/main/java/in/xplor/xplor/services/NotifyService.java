package in.xplor.xplor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import in.xplor.xplor.EventActivity;
import in.xplor.xplor.EventListActivity;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by himanshu on 4/3/17.
 * This is to notify the user about the event
 */

public class NotifyService extends Service {

    private String title, description, venue;
    private long stime, ftime;
    private double latitude, longitude;

    /**
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    // Unique id to identify the notification.
    private static final int NOTIFICATION = 123;
    // Name of an intent extra we can use to identify if this service was started to create a notification
    public static final String INTENT_NOTIFY = "com.blundell.tut.service.INTENT_NOTIFY";
    // The system notification manager
    private NotificationManager mNM;

    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // If this service was started by out AlarmTask intent then we want to show our notification
        if(intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification();

        // We don't care if this service is stopped as we have already delivered our notification
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients
    private final IBinder mBinder = new NotifyService.ServiceBinder();


    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */
    private void showNotification() {
        // This is the 'title' of the notification
        CharSequence title = "Your event is ready";
        // What time to show on the notification
        long time = System.currentTimeMillis();

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa");

        //Notification notification = new Notification(icon, text, time);
        Notification notification = null;



        // The PendingIntent to launch our activity if the user selects this notification
        Intent eventIntent = new Intent(this, EventActivity.class);

        SharedPreferences settings = getSharedPreferences("myPrefs", 0);
        title = settings.getString("title", "");
        time = settings.getLong("stime", time);
        long ftime = settings.getLong("ftime", 0);
        String desc = settings.getString("description", "");
        double latitude = settings.getFloat("latitude", 0);
        double longitude = settings.getFloat("longitude", 0);
        String venue = settings.getString("venue", "");

        eventIntent.putExtra("title", title);
        eventIntent.putExtra("stime", time);
        eventIntent.putExtra("ftime", ftime);
        eventIntent.putExtra("description", desc);
        eventIntent.putExtra("latitude", latitude);
        eventIntent.putExtra("longitude", longitude);
        eventIntent.putExtra("latitude_user", 0);
        eventIntent.putExtra("longitude_user", 0);
        eventIntent.putExtra("venue", venue);


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, eventIntent, 0);

        Uri alarmSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText("Starting : " + formatter.format(new Date(time)))
                .setContentIntent(contentIntent)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setSound(alarmSound);

        notification = builder.getNotification();
        mNM.notify(NOTIFICATION, notification);

        // Set the info for the views that show in the notification panel.
       // notification.setLatestEventInfo(this, title, text, contentIntent);

        // Clear the notification when it is pressed
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Send the notification to the system.
        mNM.notify(NOTIFICATION, notification);

        // Stop the service when we are finished
        stopSelf();
    }
}
