package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.MenuScraperService;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.NotificationService;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    /**
     * BootReceiver() is an empty default
     */
    public BootReceiver() {
    }

    /**
     * onRecieve() takes a context and an intent defined in the AndroidManifest.xml file, defined
     * as an intent released by the system when the OS is booted up
     * it then sets specifically timed dataAutomation and notification services
     * @param context the application context
     * @param intent the android boot intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


            // sets up notification service
            startNotificationService(context, alarmManager);

            // sets up data automation service

        }
    }

    private void startNotificationService(Context context, AlarmManager alarmManager) {
        Intent notificationIntent = new Intent(context, NotificationService.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(Calendar.HOUR_OF_DAY, 8);
        notificationCalendar.set(Calendar.MINUTE, 0);
        notificationCalendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC, notificationCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
    }

    private void startMenuScraperService(Context context, AlarmManager alarmManager) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        Intent timedIntent = new Intent(context, MenuScraperService.class);
        PendingIntent pendingAutomationIntent = PendingIntent.getService(context, 0, timedIntent, 0);
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 2 * 60 * 1000, pendingAutomationIntent);
            context.startService(timedIntent);
        }
        Calendar scraperCalendar = Calendar.getInstance();
        scraperCalendar.set(Calendar.HOUR_OF_DAY, 5);
        scraperCalendar.set(Calendar.MINUTE, 0);
        scraperCalendar.set(Calendar.SECOND, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, scraperCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingAutomationIntent);
    }
}