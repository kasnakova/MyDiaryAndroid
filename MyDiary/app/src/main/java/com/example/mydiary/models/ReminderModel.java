package com.example.mydiary.models;

import java.util.GregorianCalendar;

/**
 * Created by Liza on 4.5.2015 г..
 */
public class ReminderModel implements Comparable<ReminderModel> {
    private String noteText;
    private GregorianCalendar date;

    public ReminderModel(String noteText, GregorianCalendar date){
        this.noteText = noteText;
        this.date = date;
    }

    public String getNoteText(){
        return this.noteText;
    }

    public GregorianCalendar getDate(){
        return this.date;
    }

    public void setNoteText(String noteText){
        this.noteText = noteText;
    }

    public void setDate(GregorianCalendar date){
        this.date = date;
    }

    @Override
    public int compareTo(ReminderModel other) {
        return getDate().compareTo(other.getDate());
    }
}
