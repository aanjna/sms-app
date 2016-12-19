package com.solution.sms_app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;


public class SMSReceiver extends BroadcastReceiver
{
    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";
            }
            showNotification(context, smsMessageStr);

        }
    }

    private void showNotification(Context c,String notificationTitle){

        Intent intent = new Intent(c, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c)
                        .setSmallIcon(R.drawable.mr_ic_cast_light)
                        .setContentTitle("New Message")
                        .setContentText(notificationTitle)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);

        NotificationManager notificationmanager = (NotificationManager) c
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify(2, mBuilder.build());
    }
}
