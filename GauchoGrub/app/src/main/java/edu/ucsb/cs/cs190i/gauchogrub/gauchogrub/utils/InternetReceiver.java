package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.services.DataAutomationService;

public class InternetReceiver extends BroadcastReceiver {

    /**
     * onRecieve() takes a context and an intent defined in the AndroidManifest.xml file, defined
     * as an intent released by the system when the network state changes
     * it then attempts to start the dataAutomation services if connected to wifi
     * @param context the application context
     * @param intent the android boot intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        Intent automationIntent = new Intent(context, DataAutomationService.class);
        PendingIntent pendingAutomationIntent = PendingIntent.getService(context, 0, automationIntent, 0);
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, 30 * 1000, pendingAutomationIntent);
            }
        }
    }
}
