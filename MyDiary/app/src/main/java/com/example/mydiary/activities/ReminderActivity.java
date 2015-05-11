package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mydiary.Constants;
import com.example.mydiary.ReminderManager;
import com.example.mydiary.ReminderManagerBroadcastReceiver;
import com.example.mydiary.R;
import com.example.mydiary.adapters.NoteAdapter;
import com.example.mydiary.adapters.ReminderAdapter;
import com.example.mydiary.models.NoteModel;
import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.Utils;

import java.util.HashMap;
import java.util.List;

public class ReminderActivity extends ListActivity {
    private final String TAG = "ReminderActivity";
    private Activity context = this;
    private ReminderAdapter adapter;
    private List<ReminderModel> reminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        reminders = ReminderManager.getAllRemindersList(context);
        if(reminders.size() == 0){
            reminders.add(new ReminderModel(Constants.NO_REMINDERS, null));
        }

        adapter = new ReminderAdapter(context,
                R.layout.listview_reminder_cell, reminders);

        this.setListAdapter(adapter);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final ReminderModel reminder = (ReminderModel) adapter.getItem(position);
        if(reminder.getNoteText().equals(Constants.NO_REMINDERS)){
            return;
        }

        final int reqCode = Utils.getId(reminder.getNoteText());
        new AlertDialog.Builder(context)
                .setTitle("Delete reminder")
                .setMessage("Do you want to delete this reminder?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ReminderManagerBroadcastReceiver reminderManager = new ReminderManagerBroadcastReceiver();
                        reminderManager.cancelAlarm(context, reqCode);
                        adapter.remove(reminder);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.alarm)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
