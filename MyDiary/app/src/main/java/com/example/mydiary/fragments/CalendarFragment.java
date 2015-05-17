package com.example.mydiary.fragments;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
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

import com.example.mydiary.utilities.Constants;
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
import com.example.mydiary.utilities.Logger;
import com.example.mydiary.utilities.SettingsManager;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.GregorianCalendar;

public class CalendarFragment extends Fragment implements IMyDiaryHttpResponse {
    final CaldroidFragment caldroidFragment = new CaldroidFragment();
    private final String TAG = "CalendarFragment";
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
        myDiaryHttpRequester = new MyDiaryHttpRequester(this, SettingsManager.isOffline(context), context);
        SelectedDate = new GregorianCalendar();
        myDiaryHttpRequester.getNotesForDate(SelectedDate);
        setUpCalendar();
        FragmentTransaction t = getFragmentManager().beginTransaction();
        t.replace(R.id.fragmentCalendarView, caldroidFragment);
        t.commit();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        myDiaryHttpRequester.setIsOffline(SettingsManager.isOffline(context));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && myDiaryHttpRequester != null) {
            getDatesWithNotes();
            myDiaryHttpRequester.getNotesForDate(SelectedDate);
        }
    }

    private void getDatesWithNotes(){
        int month = SelectedDate.get(Calendar.MONTH) + 1;
        int year = SelectedDate.get(Calendar.YEAR);
        myDiaryHttpRequester.getDatesWithNotes(month, year);
    }

    private void setUpCalendar(){
        getDatesWithNotes();
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
                    setDatesWithNotes(R.color.caldroid_black, dates);
                }

                myDiaryHttpRequester.getDatesWithNotes(month, year);
            }
        };

        int firstDayOfWeek = CaldroidFragment.SUNDAY;
        if(SettingsManager.isMondayFirstDayOfWeek(context)) {
            firstDayOfWeek = CaldroidFragment.MONDAY;
        }

        Bundle args = new Bundle();
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, firstDayOfWeek);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
        caldroidFragment.setArguments(args);

        caldroidFragment.setCaldroidListener(caldroidListener);
    }

    private void setDatesWithNotes(int color, ArrayList<GregorianCalendar> dates){
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
        dialog.setTitle(Constants.TITLE_UNLOCK_NOTE);
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
                    DialogManager.makeAlert(context, Constants.TITLE_INVALID_PASSWORD, String.format(Constants.MESSAGE_PASSWORD_LENGTH, Constants.MIN_PASSWORD_LENGTH));
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
                .setTitle(Constants.TITLE_DELETE_NOTE)
                .setMessage(Constants.MESSAGE_DELETE_NOTE)
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
                            notes.add(new NoteModel(-1, Constants.NO_NOTES_FOR_DAY, null, null, false, true));
                        }

                        populateNoteListView();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_COULD_NOT_RETRIEVE_NOTES);
                    }
                    break;
                case DeleteNote:
                    if(result.getSuccess()){
                        notes.remove(noteToDelete);
                        if(notes.size() == 0){
                            ArrayList<GregorianCalendar> dateToDelete = new ArrayList<GregorianCalendar>();
                            dateToDelete.add(noteToDelete.getCalendarDate());
                            setDatesWithNotes(R.color.caldroid_black, dateToDelete);
                            notes.add(new NoteModel(-1, Constants.NO_NOTES_FOR_DAY, null, null, false, true));
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_COULD_NOT_RETRIEVE_NOTES);
                    }
                    break;
                case GetDatesWithNotes:
                    if(result.getSuccess()){
                        dates = JsonManager.makeGregorianCalendarArrayFromData(result.getData());
                        setDatesWithNotes(R.color.dark_green, dates);
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_COULD_NOT_RETRIEVE_NOTES);
                    }
                    break;
                case GetDecryptedNoteText:
                    if(result.getSuccess()){
                        String noteText = result.getData().replace("\"", Constants.EMPTY_STRING) + "\n";
                        NoteModel note = (NoteModel) adapter.getItem(indexOfUnlockedNote);
                        note.setNoteText(noteText);
                        note.setHasPassword(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, JsonManager.getErrorMessage(result.getData()));
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

