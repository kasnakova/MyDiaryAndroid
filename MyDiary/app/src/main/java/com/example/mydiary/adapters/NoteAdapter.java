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
import com.example.mydiary.utilities.Constants;

import java.util.List;

/**
 * Created by Liza on 1.5.2015 г..
 */
public class NoteAdapter extends ArrayAdapter{
    private Context context;
    private int layoutResourceId;
    private List<NoteModel> notes;

    public NoteAdapter(Context context, int layoutResourceId, List<NoteModel> notes){
        super(context, layoutResourceId, notes);
        this.context = context;
        this.layoutResourceId= layoutResourceId;
        this.notes= notes;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        NoteHolder holder = null;

        if(view == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new NoteHolder();
            holder.date = (TextView) view.findViewById(R.id.textViewDate);
            holder.noteText = (TextView) view.findViewById(R.id.textViewNoteText);
            holder.imageViewUnlock = (ImageView) view.findViewById(R.id.imageViewUnlock);

            view.setTag(holder);
        } else {
            holder = (NoteHolder) view.getTag();
        }

        NoteModel note = notes.get(position);
        holder.date.setText(note.getDate());
        String noteText = note.getNoteText();
        int image = R.drawable.unlock;
        if(note.getHasPassword()) {
            noteText = Constants.PASSWORD_NEEDED;
            image = R.drawable.lock;
        }

        holder.imageViewUnlock.setImageResource(image);
        if(noteText.equals(Constants.NO_NOTES_FOR_DAY)){
            holder.imageViewUnlock.setVisibility(View.GONE);
        } else {
            holder.imageViewUnlock.setVisibility(View.VISIBLE);
        }

        holder.noteText.setText(noteText + "\n");
        return view;
    }

    static class NoteHolder
    {
        TextView date;
        TextView noteText;
        ImageView imageViewUnlock;
    }
}
