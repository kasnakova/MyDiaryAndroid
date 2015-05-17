package com.example.mydiary.fragments;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydiary.utilities.Constants;
import com.example.mydiary.controllers.ReminderDialog;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.R;
import com.example.mydiary.interfaces.IReminderDialogListener;
import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.DateManager;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.models.NoteType;
import com.example.mydiary.utilities.SettingsManager;

public class RecordFragment extends Fragment implements IMyDiaryHttpResponse, IReminderDialogListener {
    private final int REQ_CODE_SPEECH_INPUT = 100;

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
        myDiaryHttpRequester = new MyDiaryHttpRequester(this, SettingsManager.isOffline(context), context);
        setOnClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        myDiaryHttpRequester.setIsOffline(SettingsManager.isOffline(context));
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
                if(SettingsManager.isOffline(context)){
                    Toast.makeText(context, Constants.TOAST_IN_OFFLINE_MODE, Toast.LENGTH_LONG).show();
                } else {
                    saveToDb();
                }
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
                makeReminder();
            }
        });

        buttonSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SettingsManager.isOffline(context)){
                    Toast.makeText(context, Constants.TOAST_IN_OFFLINE_MODE, Toast.LENGTH_LONG).show();
                } else {
                    setPassword();
                }
            }
        });
    }

    private void makeReminder(){
        ReminderDialog dialog = new ReminderDialog(context, this);
        String date = textViewDateForNote.getText().toString();
        dialog.show(Constants.TITLE_MAKE_REMINDER, editTextSpeechInput.getText().toString(), date);
    }

    private void setPassword(){
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_set_password);
        dialog.setTitle(Constants.TITLE_SET_PASSWORD);
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
                    DialogManager.makeAlert(context, Constants.TITLE_INVALID_PASSWORD, String.format(Constants.MESSAGE_PASSWORD_LENGTH, Constants.MIN_PASSWORD_LENGTH));
                } else if(!pass.equals(passConf)) {
                    DialogManager.makeAlert(context, Constants.TITLE_PASSWORD_MISMATCH, Constants.MESSAGE_PASSWORD_CONFIRM_NOT_MATCH);
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
        if(noteText == null || noteText.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(context, Constants.TITLE_INVALID_NOTE, Constants.MESSAGE_INVALID_NOTE);
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Constants.SPEECH_INPUT_LANGUAGE);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                Constants.TITLE_SPEECH_INPUT);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    Constants.TOAST_SPEECH_INPUT_NOT_SUPPORTED,
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
                        Toast.makeText(context, Constants.TOAST_SUCCESSFULLY_SENT_NOTE, Toast.LENGTH_LONG).show();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_NOTE_NOT_SAVED);
                    }
                    break;
                default:
                    break;
            }
        } else {
            DialogManager.NoInternetOrServerAlert(context);
        }
    }

    @Override
    public void onReminderDialogDismiss(boolean isConfirmed, ReminderModel reminder) {
        if(isConfirmed){
            Toast.makeText(context, Constants.TOAST_REMINDER_MADE, Toast.LENGTH_LONG).show();
        }
    }
}
