package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mydiary.utilities.Constants;
import com.example.mydiary.utilities.ReminderManager;
import com.example.mydiary.controllers.ReminderManagerBroadcastReceiver;
import com.example.mydiary.R;
import com.example.mydiary.adapters.ReminderAdapter;
import com.example.mydiary.controllers.ReminderDialog;
import com.example.mydiary.controllers.SettingsDialog;
import com.example.mydiary.interfaces.IReminderDialogListener;
import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.DateManager;
import com.example.mydiary.utilities.Utils;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class ReminderActivity extends ListActivity implements IReminderDialogListener {
    private final String TAG = "ReminderActivity";
    private Activity context = this;
    private ReminderAdapter adapter;
    private List<ReminderModel> reminders;
    private ReminderModel reminderToUpdate;
    private boolean addReminder;
    private boolean noReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        addReminder = false;
        noReminders = false;

        reminders = ReminderManager.getAllRemindersList(context);
        if(reminders.size() == 0){
            noReminders = true;
            reminders.add(new ReminderModel(Constants.NO_REMINDERS, null));
        }

        Collections.sort(reminders);

        adapter = new ReminderAdapter(context,
                R.layout.listview_reminder_cell, reminders);

        this.setListAdapter(adapter);

        this.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                deleteReminder(position);
                return true;
            }
        });
    }


    private void deleteReminder(int position){
        final ReminderModel reminder = (ReminderModel) adapter.getItem(position);
        if(noReminders){
            return;
        }

        final int reqCode = Utils.getId(reminder.getNoteText() + DateManager.getDateTimeStringFromCalendar(reminder.getDate()));
        new AlertDialog.Builder(context)
                .setTitle(Constants.TITLE_DELETE_REMINDER)
                .setMessage(Constants.MESSAGE_DELETE_REMINDER)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ReminderManagerBroadcastReceiver reminderManager = new ReminderManagerBroadcastReceiver();
                        reminderManager.cancelAlarm(context, reqCode);
                        reminders.remove(reminder);
                        if(reminders.size() == 0){
                            reminders.add(new ReminderModel(Constants.NO_REMINDERS, null));
                            noReminders = true;
                        }

                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.alarm)
                .show();
    }

    private void removeAll(){
        if(noReminders){
            Toast.makeText(context, Constants.TOAST_ALL_REMINDERS_ALREADY_REMOVED, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle(Constants.TITLE_DELETE_REMINDERS)
                .setMessage(Constants.MESSAGE_DELETE_REMINDERS)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ReminderManagerBroadcastReceiver reminderManager = new ReminderManagerBroadcastReceiver();

                        for (int i = 0; i < reminders.size(); i++) {
                            ReminderModel reminder = reminders.get(i);
                            int reqCode = Utils.getId(reminder.getNoteText() + DateManager.getDateTimeStringFromCalendar(reminder.getDate()));
                            reminderManager.cancelAlarm(context, reqCode);
                        }

                        reminders.clear();
                        reminders.add(new ReminderModel(Constants.NO_REMINDERS, null));
                        noReminders = true;
                        adapter.notifyDataSetChanged();
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        reminderToUpdate = (ReminderModel) adapter.getItem(position);
        if(reminderToUpdate.getNoteText().equals(Constants.NO_REMINDERS)){
            return;
        }

        addReminder = false;
        ReminderDialog dialog = new ReminderDialog(context, this);
        String date = DateManager.getBGDateStringFromCalendar(new GregorianCalendar());
        dialog.show(Constants.TITLE_MAKE_REMINDER, reminderToUpdate.getNoteText(), date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        menu.add(0, 0, 1, getString(R.string.action_home)).setIcon(R.drawable.home).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onHomeMenuItemClicked();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 0, 1, getString(R.string.action_add)).setIcon(R.drawable.add).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onAddMenuItemClicked();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 0, 1, getString(R.string.action_cancel_all)).setIcon(R.drawable.clear).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                removeAll();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        menu.add(0, 0, 1, "Settings").setIcon(R.drawable.settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                onSettingsMenuItemClicked();
//                return true;
//            }
//        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        menu.add(0, 0, 1, "Help").setIcon(R.drawable.help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                onHelpMenuItemClicked();
//                return true;
//            }
//        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                onHomeMenuItemClicked();
                return true;
            case R.id.action_add:
                onAddMenuItemClicked();
                return true;
            case R.id.action_cancel_all:
                removeAll();
                return true;
            case R.id.action_settings:
                onSettingsMenuItemClicked();
                return true;
            case R.id.action_help:
                onHelpMenuItemClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onHomeMenuItemClicked(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void onAddMenuItemClicked(){
        addReminder = true;
        ReminderDialog reminderDialog = new ReminderDialog(context, this);
        String date = DateManager.getBGDateStringFromCalendar(new GregorianCalendar());
        reminderDialog.show(Constants.TITLE_MAKE_REMINDER, Constants.EMPTY_STRING, date);
    }

    private void onSettingsMenuItemClicked(){
        SettingsDialog settingsDialog = new SettingsDialog(context);
        settingsDialog.show();
    }

    private void onHelpMenuItemClicked(){
        Intent intentHelp = new Intent(this, HelpActivity.class);
        startActivity(intentHelp);
    }

    @Override
    public void onReminderDialogDismiss(boolean isConfirmed, ReminderModel reminder) {
        if(isConfirmed){
            if(addReminder){
                if(noReminders){
                    noReminders = false;
                    reminders.clear();
                }

                reminders.add(reminder);
                Collections.sort(reminders);
                adapter.notifyDataSetChanged();
                Toast.makeText(context, Constants.TOAST_REMINDER_MADE, Toast.LENGTH_LONG).show();
            } else {
                ReminderManagerBroadcastReceiver alarm = new ReminderManagerBroadcastReceiver();
                alarm.cancelAlarm(context, Utils.getId(reminderToUpdate.getNoteText() + DateManager.getDateTimeStringFromCalendar(reminderToUpdate.getDate())));
                reminderToUpdate.setNoteText(reminder.getNoteText());
                reminderToUpdate.setDate(reminder.getDate());
                Collections.sort(reminders);
                adapter.notifyDataSetChanged();
                Toast.makeText(context, Constants.TOAST_REMINDER_UPDATED, Toast.LENGTH_LONG).show();
            }
        }
    }
}
