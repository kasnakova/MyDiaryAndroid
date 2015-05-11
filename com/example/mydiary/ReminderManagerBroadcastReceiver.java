package com.example.mydiary;

/**
 * Created by Liza on 2.5.2015 г..
 */
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.mydiary.activities.RegisterActivity;
import com.example.mydiary.activities.ReminderActivity;
import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.Utils;

public class ReminderManagerBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "ReminderManagerBroadcastReceiver";
    private boolean onBoot = false;

    @Override
    public void onReceive(Context context, Intent intent) {//TODO: make this usable offline
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();

        //Check if the phone wasn't rebooted - only then this extra will not exist
        if(intent.hasExtra(Constants.REMINDER_ACTION)){
            this.onBoot = false;
            ReminderAction reminderAction = (ReminderAction) intent.getSerializableExtra(Constants.REMINDER_ACTION);
            switch (reminderAction){
                case MakeNotification:
                    makeNotification(context, intent);
                    break;
                case CancelAlarm:
                    int reqCode = intent.getIntExtra(Constants.REQ_CODE, -1);
                    removeNotification(context, reqCode);
                    cancelAlarm(context, reqCode);
                    Toast.makeText(context, Constants.ALARM_CANCELED, Toast.LENGTH_LONG).show();
                    break;
                case SnoozeAlarm:
                    removeNotification(context, intent.getIntExtra(Constants.REQ_CODE, -1));
                    Toast.makeText(context, Constants.ALARM_SNOOZED, Toast.LENGTH_LONG).show();
                    //Do nothing - leave the repeating alarm continue
                    break;
                default:
                    break;
            }
        } else {
            this.onBoot = true;
            setAlarmsAfterReboot(context, ReminderManager.getAllReminders(context));
        }

        wl.release();
    }

    public void cancelAlarm(Context context, int reqCode)
    {
        Intent intent = new Intent(context, ReminderManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, reqCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        ReminderManager.removeReminder(context, reqCode);
    }

    public void setAlarm(Context context, GregorianCalendar cal, String noteText){
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderManagerBroadcastReceiver.class);
        intent.putExtra(Constants.NOTE_TEXT, noteText);
        intent.putExtra(Constants.REMINDER_ACTION, ReminderAction.MakeNotification);
        int requestCode = Utils.getId(noteText);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), Constants.INTERVAL_MILIS, pi);

        //No need to save them again if it's just setting the alarms after reboot
        if(!onBoot){
            ReminderManager.addReminder(context, requestCode, noteText, cal);
        }
    }

    private void removeNotification(Context context, int reqCode){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(reqCode);
    }

    private void makeNotification(Context context, Intent intent){
        //The intent if the notification itself is pressed
        String noteText = intent.getStringExtra(Constants.NOTE_TEXT);
        //Using the same value for notificationId and request code for the alarmManager
        int notificationId = Utils.getId(noteText);
        Intent resultIntent = new Intent(context, ReminderActivity.class);
        resultIntent.setAction(String.valueOf(notificationId));
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, notificationId, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //The intent if the cancel button on the notification is pressed
        Intent cancelIntent = new Intent(context, ReminderManagerBroadcastReceiver.class);
        cancelIntent.setAction("cancel");
        cancelIntent.putExtra(Constants.REMINDER_ACTION, ReminderAction.CancelAlarm);
        cancelIntent.putExtra(Constants.REQ_CODE, notificationId);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //The intent if the snooze button on the notification is pressed
        Intent snoozeIntent = new Intent(context, ReminderManagerBroadcastReceiver.class);
        snoozeIntent.setAction("snooze");
        snoozeIntent.putExtra(Constants.REMINDER_ACTION, ReminderAction.SnoozeAlarm);
        snoozeIntent.putExtra(Constants.REQ_CODE, notificationId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.diary)
                .setContentTitle(context.getResources().getString(R.string.my_diary_reminder))
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentText(noteText)
                .addAction(R.drawable.exit, context.getResources().getString(R.string.cancel), cancelPendingIntent)
                .addAction(R.drawable.alarm_small, context.getResources().getString(R.string.snooze), snoozePendingIntent);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    //Alarms for different notes have to have different request codes and notification ids


    private void setAlarmsAfterReboot(Context context, HashMap<String, ReminderModel> reminders){
        Iterator it = reminders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry reminder = (Map.Entry)it.next();
            int reqCode = Integer.parseInt(reminder.getKey().toString());
            ReminderModel remind = (ReminderModel) reminder.getValue();
            setAlarm(context, remind.getDate(), remind.getNoteText());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
