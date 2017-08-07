package in.xplor.xplor.services.tasks;

/**
 * Created by himanshu on 25/2/17.
 */

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import in.xplor.xplor.services.NotifyService;

/**
 * Set an alarm for the date passed into the constructor
 * When the alarm is raised it will start the NotifyService
 *
 * This uses the android build in alarm manager *NOTE* if the phone is turned off this alarm will be cancelled
 *
 * This will run on it's own thread.
 *
 * @author paul.blundell
 */
public class AlarmTask implements Runnable{
    // The date selected for the alarm
    private final Calendar date;
    // The android system alarm manager
    private final AlarmManager am;
    // Your context to retrieve the alarm manager from
    private final Context context;
    // Title for the notification
    private String title;
    private long stime;
    private long ftime;
    // Description of the notification
    private String description;
    private double latitude;
    private double longitude;
    private String venue;

    public AlarmTask(Context context, Calendar c, String title, long stime, long ftime, String description, double lat, double lng, String venue) {
        this.context = context;
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date = c;
        this.title = title;
        this.description = description;
        this.stime = stime;
        this.ftime = ftime;
        this.latitude = lat;
        this.longitude = lng;
        this.venue = venue;
    }

    @Override
    public void run() {
        // Request to start are service when the alarm date is upon us
        // We don't start an activity as we just want to pop up a notification into the system bar not a full activity
        Intent intent = new Intent(context, NotifyService.class);

        intent.putExtra("title", title);
        intent.putExtra("stime", stime);
        intent.putExtra("ftime", ftime);
        intent.putExtra("description", description);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("venue", venue);

        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        // Sets an alarm - note this alarm will be lost if the phone is turned off and on again
        am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
    }


}