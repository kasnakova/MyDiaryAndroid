package com.example.mydiary;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.FragmentTransaction;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.GregorianCalendar;

public class CalendarFragment extends Fragment implements IMyDiaryHttpResponse {
    final CaldroidFragment caldroidFragment = new CaldroidFragment();
	private ListView listViewNotes;
    private HomeActivity context;
    private NoteAdapter adapter;
    private MyDiaryHttpRequester myDiaryHttpRequester;
    private ProgressDialog progress;
    private List<NoteModel> notes;
    private NoteModel noteToDelete;
    private ArrayList<GregorianCalendar> dates;
    public static GregorianCalendar SelectedDate;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        context = (HomeActivity) getActivity();
        listViewNotes = (ListView) rootView.findViewById(R.id.listViewNotes);
        myDiaryHttpRequester = new MyDiaryHttpRequester(this);
        SelectedDate = new GregorianCalendar();
        progress = ProgressDialog.show(context, null, null, true);
        myDiaryHttpRequester.getNotesForDate(new GregorianCalendar());
//TODO: add settings and user can choose from which day the week begins
        setUpCalendar();
        FragmentTransaction t = getFragmentManager().beginTransaction();
        t.replace(R.id.fragmentCalendarView, caldroidFragment);
        t.commit();

        return rootView;
    }

    private void setUpCalendar(){
        int month = SelectedDate.get(Calendar.MONTH) + 1;
        int year = SelectedDate.get(Calendar.YEAR);
        myDiaryHttpRequester.getDatesWithNotes(month, year);

        CaldroidListener caldroidListener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                caldroidFragment.setSelectedDates(date, date);
                caldroidFragment.refreshView();
                progress.show();
                myDiaryHttpRequester.getNotesForDate(Utils.getGregorianCalendarFromDate(date));
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                SelectedDate = Utils.getGregorianCalendarFromDate(date);
                context.getViewPager().setCurrentItem(0);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                if(dates != null && dates.size() > 0) {
                    int previousMonth = dates.get(0).get(Calendar.MONTH) + 1;
                    setDatesWithNotes(R.color.caldroid_black);
                }

                myDiaryHttpRequester.getDatesWithNotes(month, year);
            }
        };

        caldroidFragment.setCaldroidListener(caldroidListener);
    }

    private void setDatesWithNotes(int color){
        for (int i = 0; i < dates.size(); i++){
            Date date = dates.get(i).getTime();
            caldroidFragment.setTextColorForDate(color, date);
        }

        caldroidFragment.refreshView();
    }

    private void populateNoteListView(){
        adapter = new NoteAdapter(context,
                R.layout.listview_note_cell, notes);
        listViewNotes.setAdapter(adapter);
        listViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                noteToDelete = (NoteModel) adapter.getItem(i);
                deleteNote();
                return true;
            }
        });
    }

    private void deleteNote(){
        final int id = noteToDelete.getId();
        new AlertDialog.Builder(context)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this valuable memory?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        progress.show();
                        myDiaryHttpRequester.deleteNote(id);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        progress.dismiss();
        if(result != null){
            switch(result.getService()){
                case GetNotesForDate:
                    if(result.getSuccess()){
                        notes = Json.makeNotesFromJson(result.getData());
                        if(notes.size() == 0){
                            notes.add(new NoteModel(-1, "No notes for this day", null, true));
                        }

                        populateNoteListView();
                    } else {
                        Utils.makeAlert(context, "A problem occurred", "Sorry, we couldn't retrieve your notes");
                    }
                    break;
                case DeleteNote:
                    if(result.getSuccess()){
                        adapter.remove(noteToDelete);
                        Toast.makeText(context, "Your note has been deleted", Toast.LENGTH_LONG).show();
                    } else {
                        Utils.makeAlert(context, "A problem occurred", "Sorry, we couldn't delete this note");
                    }
                    break;
                case GetDatesWithNotes:
                    if(result.getSuccess()){
                        dates = Json.makeGregorianCalendarArrayFromData(result.getData());
                        setDatesWithNotes(R.color.dark_green);
                    } else {
                        Utils.makeAlert(context, "A problem occurred", "Sorry, we couldn't retrieve your notes");
                    }
                    break;
                default:
                    break;
            }
        } else {
            Utils.NoInternetOrServerAlert(context);
        }
    }
}

