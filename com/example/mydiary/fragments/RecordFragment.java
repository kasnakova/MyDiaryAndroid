package com.example.mydiary.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mydiary.ReminderManagerBroadcastReceiver;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.R;
import com.example.mydiary.utilities.DateManager;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.models.NoteType;
import com.example.mydiary.utilities.Utils;

public class RecordFragment extends Fragment implements IMyDiaryHttpResponse {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final String SUCCESSFULLY_SENT_NOTE = "Your note was successfully saved!";

    private EditText editTextSpeechInput;
    private TextView textViewDateForNote;
    private Button btnSpeak;
    private Button buttonSaveToDb;
    private Button buttonClear;
    private Button buttonSetAlarm;
    private Button buttonSetPassword;
    private Activity context;
    private String notePassword = null;

    private MyDiaryHttpRequester myDiaryHttpRequester;

    public static NoteType NoteType = com.example.mydiary.models.NoteType.Normal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        context = getActivity();
        editTextSpeechInput = (EditText) rootView.findViewById(R.id.txtSpeechInput);
        textViewDateForNote = (TextView) rootView.findViewById(R.id.textViewDateForNote);
        textViewDateForNote.setText(DateManager.getBGDateStringFromCalendar(new GregorianCalendar()));
        btnSpeak = (Button) rootView.findViewById(R.id.btnSpeak);
        buttonSaveToDb = (Button) rootView.findViewById(R.id.buttonSaveToDb);
        buttonClear = (Button) rootView.findViewById(R.id.buttonClear);
        buttonSetAlarm = (Button) rootView.findViewById(R.id.buttonSetAlarm);
        buttonSetPassword = (Button) rootView.findViewById(R.id.buttonSetPassword);
        myDiaryHttpRequester = new MyDiaryHttpRequester(this, Utils.isOffline(context), context);
        setOnClickListeners();

        return rootView;
    }

    private void setOnClickListeners() {
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        buttonSaveToDb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveToDb();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editTextSpeechInput.setText("");
            }
        });

        buttonSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm();
            }
        });

        buttonSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassword();
            }
        });
    }

    private void setAlarm(){
        if(editTextSpeechInput.getText().toString().equals("")){
            DialogManager.makeAlert(context, "Invalid reminder", "You can't set a reminder without text!");
            return;
        }

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_set_alarm);
        dialog.setTitle("Set Alarm");
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.diary);

        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePickerSetAlarm);
        final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePickerSetAlarm);
        Button buttonSetAlarm = (Button) dialog.findViewById(R.id.buttonSetAlarm);
        Button buttonSetAlarmCancel = (Button) dialog.findViewById(R.id.buttonSetAlarmCancel);
        datePicker.setCalendarViewShown(false);
        timePicker.setIs24HourView(true);

        int[] dateNumbers = DateManager.getDateInNumbersFromString(textViewDateForNote.getText().toString());
        datePicker.updateDate(dateNumbers[2], dateNumbers[1], dateNumbers[0]);
        datePicker.setMinDate(DateManager.getTimeInMilisFromString(textViewDateForNote.getText().toString()));
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        buttonSetAlarmCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                if(timePicker.getCurrentHour() >= now.get(Calendar.HOUR_OF_DAY)) {
                    if(timePicker.getCurrentMinute() >= now.get(Calendar.MINUTE)){
                        //TODO: make it on boot as well
                        ReminderManagerBroadcastReceiver alarm = new ReminderManagerBroadcastReceiver();
                        GregorianCalendar cal  = DateManager.getGregorianCalendarFromNumbers(datePicker.getDayOfMonth(),
                                datePicker.getMonth(), datePicker.getYear(),
                                timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                        alarm.setAlarm(context, cal, editTextSpeechInput.getText().toString());
                        dialog.dismiss();
                        Toast.makeText(context, "Alarm is set", Toast.LENGTH_LONG).show();
                    } else {
                        DialogManager.makeAlert(context, "Invalid time", "Your alarm must be after the current time");
                    }
                } else {
                    DialogManager.makeAlert(context, "Invalid time", "Your alarm must be after the current time");
                }
            }
        });

        dialog.show();
    }

    private void setPassword(){
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_set_password);
        dialog.setTitle("Set note password");
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.diary);
        final EditText editTextPassword = (EditText) dialog.findViewById(R.id.editTextNotePassword);
        final EditText editTextPasswordConfirm = (EditText) dialog.findViewById(R.id.editTextNotePasswordConfirm);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonSetPasswordCancel);
        Button buttonDone = (Button) dialog.findViewById(R.id.buttonSetPasswordDone);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = editTextPassword.getText().toString();
                String passConf = editTextPasswordConfirm.getText().toString();
                if(pass.length() < 6){
                    DialogManager.makeAlert(context, "Invalid password", "Password must be at least 6 characters long");
                } else if(!pass.equals(passConf)) {
                    DialogManager.makeAlert(context, "Password mismatch", "The password confirmation doesn't match!");
                } else {
                    notePassword = pass;
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void saveToDb() {
        String noteText = editTextSpeechInput.getText().toString();
        if(noteText == null || noteText.equals("")){
            DialogManager.makeAlert(context, "Invalid note", "Your note can't be empty");
        } else {
            myDiaryHttpRequester.sendNote(noteText, RecordFragment.NoteType, notePassword, CalendarFragment.SelectedDate);
        }
    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editTextSpeechInput.setText(result.get(0));
                    this.buttonSaveToDb.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && textViewDateForNote != null){
            textViewDateForNote.setText(DateManager.getBGDateStringFromCalendar(CalendarFragment.SelectedDate));
        }
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        if(result != null) {
            switch (result.getService()) {
                case SaveNote:
                    if (result.getSuccess()) {
                        notePassword = null;
                        Toast.makeText(context, SUCCESSFULLY_SENT_NOTE, Toast.LENGTH_LONG).show();
                    } else {
                        //TODO: get these strings out of here
                        DialogManager.makeAlert(context, "A problem occurred", "Your note was not saved");
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
