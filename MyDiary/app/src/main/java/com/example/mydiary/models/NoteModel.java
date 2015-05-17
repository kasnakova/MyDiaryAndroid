package com.example.mydiary.models;

import com.example.mydiary.utilities.DateManager;

import java.util.GregorianCalendar;

/**
 * Created by Liza on 1.5.2015 г..
 */
public class NoteModel {
    private int id;
    private String date;
    private GregorianCalendar calendar;
    private String noteText;
    private NoteType noteType;
    private boolean hasPassword;
    private boolean onlyTime;

    public NoteModel(int id, String noteText, GregorianCalendar calendar, NoteType noteType, boolean hasPassword, boolean onlyTime){
        this.id = id;
        this.hasPassword = hasPassword;
        this.onlyTime = onlyTime;
        this.setNoteType(noteType);
        this.setNoteText(noteText);
        this.setDate(calendar);
    }

    public int getId(){
        return this.id;
    }

    public String getDate(){
        return this.date;
    }

    public NoteType getNoteType(){
        return this.noteType;
    }

    public String getNoteText(){
        return this.noteText;
    }

    public boolean getHasPassword(){
        return this.hasPassword;
    }
    public GregorianCalendar getCalendarDate(){
        return this.calendar;
    }

    public void setDate(GregorianCalendar calendar){
        if(calendar != null) {
            this.calendar = calendar;
            if (this.onlyTime) {
                this.date = DateManager.getTimeStringFromCalendar(calendar);
            } else {
                this.date = DateManager.getDateTimeStringFromCalendar(calendar);
            }
        } else {
            this.date = "";
        }
    }

    public void setNoteText(String noteText){
        this.noteText = noteText;
    }

    public void setNoteType(NoteType noteType){
        this.noteType = noteType;
    }

    public void setHasPassword(boolean hasPassword){
        this.hasPassword = hasPassword;
    }
}
