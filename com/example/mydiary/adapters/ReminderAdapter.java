package com.example.mydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mydiary.R;
import com.example.mydiary.models.NoteModel;
import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.DateManager;

import java.util.List;

/**
 * Created by Liza on 1.5.2015 г..
 */
public class ReminderAdapter extends ArrayAdapter{
    private Context context;
    private int layoutResourceId;
    private List<ReminderModel> reminders;

    public ReminderAdapter(Context context, int layoutResourceId, List<ReminderModel> reminders){
        super(context, layoutResourceId, reminders);
        this.context = context;
        this.layoutResourceId= layoutResourceId;
        this.reminders= reminders;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ReminderHolder holder = null;

        if(view == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new ReminderHolder();
            holder.date = (TextView) view.findViewById(R.id.textViewReminderDate);
            holder.reminderText = (TextView) view.findViewById(R.id.textViewReminderText);

            view.setTag(holder);
        } else {
            holder = (ReminderHolder) view.getTag();
        }

        ReminderModel reminder = reminders.get(position);
        holder.date.setText(DateManager.getBGDateTimeStringFromCalendar(reminder.getDate()));
        String noteText = reminder.getNoteText();
        holder.reminderText.setText(noteText);

        return view;
    }

    static class ReminderHolder
    {
        TextView date;
        TextView reminderText;
    }
}
