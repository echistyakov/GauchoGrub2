package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.joda.time.DateTime;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.MenuScraperService;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.NotificationService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


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
            startMenuScraperService(context, alarmManager);
        }
    }

    private void startNotificationService(Context context, AlarmManager alarmManager) {
        Intent notificationIntent = new Intent(context, NotificationService.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
        DateTime dateTime = DateTime.now().withHourOfDay(7).withMinuteOfHour(0).withSecondOfMinute(0);
        alarmManager.setRepeating(AlarmManager.RTC, dateTime.getMillis(),
                AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
    }

    private void startMenuScraperService(Context context, AlarmManager alarmManager) {
        Intent menuScraperIntent = new Intent(context, MenuScraperService.class);
        PendingIntent pendingMenuScraperIntent = PendingIntent.getService(context, 0, menuScraperIntent, 0);
        DateTime dateTime = DateTime.now().withHourOfDay(5).withMinuteOfHour(0).withSecondOfMinute(0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, dateTime.getMillis(),
                AlarmManager.INTERVAL_DAY, pendingMenuScraperIntent);
    }

    /* from http://stackoverflow.com/questions/6493517/detect-if-android-device-has-internet-connection */
    private boolean hasInternetAccess(Context context) {
        if (hasNetworkAccess(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return urlc.getResponseCode() == 204;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean hasNetworkAccess(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null;
    }
}