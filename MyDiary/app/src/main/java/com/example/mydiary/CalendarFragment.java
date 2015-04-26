package com.example.mydiary;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TextView;
import android.widget.Toast;
 
public class CalendarFragment extends Fragment {
 
	private CalendarView calendar;
	private TextView textViewNote;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendar = (CalendarView) rootView.findViewById(R.id.calendarView); 
        textViewNote = (TextView) rootView.findViewById(R.id.textViewNote);
        showNoteForCurrentDay();
     
        this.calendar.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                    int dayOfMonth) {
            	textViewNote.setText("");
                 showNotesForDay(year, month, dayOfMonth);
            }
        });
        return rootView;
    }
    
    private void showNoteForCurrentDay(){
    	Calendar calendar = Calendar.getInstance();
    	int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    	showNotesForDay(year, month, dayOfMonth);
    }
    
    private void showNotesForDay(int year, int month, int dayOfMonth) {
//    	ParseUser currentUser = ParseUser.getCurrentUser();
//    	ParseQuery<ParseObject> query = ParseQuery.getQuery("Diary");
//    	query.whereEqualTo("User", currentUser);
//
//    	String date = String.valueOf(dayOfMonth) + "." +
//				String.valueOf(month + 1) + "." +
//				String.valueOf(year);
//    	query.whereEqualTo("Date", date);
//    	System.out.println(date);
//    	query.findInBackground(new FindCallback<ParseObject>() {
//    	    public void done(List<ParseObject> notes, ParseException e) {
//    	        if (e == null) {
//    	        	String text = "No notes for this date!";
//    	        	if(notes.size() != 0){
//    	        		text = "";
//    	        		for (ParseObject note : notes) {
//    	        			Date date = note.getCreatedAt();
//    	        			String time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
//        	        		text += time + "\t"+ note.getString("Text") + "\n";
//						}
//    	        	}
//
//    	        	textViewNote.setText(text);
//    	        } else {
//    	        	showErrorRetrievingFromDbToast();
//    	        }
//    	    }
//    	});
    }
    
    private void showErrorRetrievingFromDbToast(){
    	Toast.makeText(getActivity(),
    			getString(R.string.error_retrieving_from_db),
                Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }
}

