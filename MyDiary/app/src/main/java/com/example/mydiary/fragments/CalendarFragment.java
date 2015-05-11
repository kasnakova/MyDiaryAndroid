package com.example.mydiary.fragments;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.FragmentTransaction;

import com.example.mydiary.utilities.DateManager;
import com.example.mydiary.activities.HomeActivity;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.utilities.JsonManager;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.adapters.NoteAdapter;
import com.example.mydiary.models.NoteModel;
import com.example.mydiary.R;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Utils;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.GregorianCalendar;

public class CalendarFragment extends Fragment implements IMyDiaryHttpResponse {
    final CaldroidFragment caldroidFragment = new CaldroidFragment();
	private ListView listViewNotes;
    private HomeActivity context;
    private NoteAdapter adapter;
    private MyDiaryHttpRequester myDiaryHttpRequester;
    private List<NoteModel> notes;
    private NoteModel noteToDelete;
    private int indexOfUnlockedNote;
    private ArrayList<GregorianCalendar> dates;
    public static GregorianCalendar SelectedDate;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        context = (HomeActivity) getActivity();
        listViewNotes = (ListView) rootView.findViewById(R.id.listViewNotes);
        myDiaryHttpRequester = new MyDiaryHttpRequester(this, Utils.isOffline(context), context);
        SelectedDate = new GregorianCalendar();
        myDiaryHttpRequester.getNotesForDate(SelectedDate);
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
                myDiaryHttpRequester.getNotesForDate(DateManager.getGregorianCalendarFromDate(date));
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                SelectedDate = DateManager.getGregorianCalendarFromDate(date);
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

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NoteModel note = (NoteModel) adapterView.getItemAtPosition(i);
                if(note.getHasPassword()){
                    unlockNote(view);
                }
            }
        });
    }

    private void unlockNote(View v){
        indexOfUnlockedNote = listViewNotes.indexOfChild(v);
        final NoteModel note = (NoteModel) adapter.getItem(indexOfUnlockedNote);
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_unlock);
        dialog.setTitle("Enter password");
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.diary);
        final EditText editTextUnlock = (EditText) dialog.findViewById(R.id.editTextUnlockPassword);
        Button buttonUnlockCancel = (Button) dialog.findViewById(R.id.buttonUnlockCancel);
        Button buttonUnlockDone = (Button) dialog.findViewById(R.id.buttonUnlockDone);
        buttonUnlockCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        buttonUnlockDone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String password = editTextUnlock.getText().toString();
                if(password.length() < 6){
                    DialogManager.makeAlert(context, "Invalid password", "Password must be at least 6 characters long!");
                } else {
                    myDiaryHttpRequester.getDecryptedNoteText(note.getId(), password);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void deleteNote(){
        final int id = noteToDelete.getId();
        new AlertDialog.Builder(context)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this valuable memory?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
        if(result != null){
            switch(result.getService()){
                case GetNotesForDate:
                    if(result.getSuccess()){
                        notes = JsonManager.makeNotesFromJson(result.getData());
                        if(notes.size() == 0){
                            notes.add(new NoteModel(-1, context.getResources().getString(R.string.no_notes_for_day), null, null, false, true));
                        }

                        populateNoteListView();
                    } else {
                        DialogManager.makeAlert(context, "A problem occurred", "Sorry, we couldn't retrieve your notes");
                    }
                    break;
                case DeleteNote:
                    if(result.getSuccess()){
                        adapter.remove(noteToDelete);
                    } else {
                        DialogManager.makeAlert(context, "A problem occurred", "Sorry, we couldn't delete this note");
                    }
                    break;
                case GetDatesWithNotes:
                    if(result.getSuccess()){
                        dates = JsonManager.makeGregorianCalendarArrayFromData(result.getData());
                        setDatesWithNotes(R.color.dark_green);
                    } else {
                        DialogManager.makeAlert(context, "A problem occurred", "Sorry, we couldn't retrieve your notes");
                    }
                    break;
                case GetDecryptedNoteText:
                    if(result.getSuccess()){
                        String noteText = result.getData().replace("\"", "") + "\n";
                        NoteModel note = (NoteModel) adapter.getItem(indexOfUnlockedNote);
                        note.setNoteText(noteText);
                        note.setHasPassword(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        DialogManager.makeAlert(context, "A problem occurred", JsonManager.getErrorMessage(result.getData()));
            }
                    break;
                default:
                    break;
            }
        } else {
            DialogManager.NoInternetOrServerAlert(context);
        }
    }
}

