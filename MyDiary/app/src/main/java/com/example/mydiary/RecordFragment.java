package com.example.mydiary;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RecordFragment extends Fragment implements IMyDiaryHttpResponse {
    private final int REQ_CODE_SPEECH_INPUT = 100;private EditText txtSpeechInput;
    private final String SUCCESSFULLY_SENT_NOTE = "Your note was successfully saved!";
//TODO: maybe make better UI
    private Button btnSpeak;
    private Button buttonSaveToDb;
    private Button buttonClear;
    private Activity context;

    private MyDiaryHttpRequester myDiaryHttpRequester;
    private ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        context = getActivity();
        txtSpeechInput = (EditText) rootView.findViewById(R.id.txtSpeechInput);
        btnSpeak = (Button) rootView.findViewById(R.id.btnSpeak);
        buttonSaveToDb = (Button) rootView.findViewById(R.id.buttonSaveToDb);
        buttonClear = (Button) rootView.findViewById(R.id.buttonClear);
        myDiaryHttpRequester = new MyDiaryHttpRequester(this);
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
                txtSpeechInput.setText("");
            }
        });
    }

    private void saveToDb() {
        String noteText = txtSpeechInput.getText().toString();
        if(noteText == null || noteText.equals("")){
            Utils.makeAlert(context, "Invalid note", "Your note can't be empty");
        } else {
            progress = ProgressDialog.show(context, null, null, true);
            myDiaryHttpRequester.sendNote(noteText, new GregorianCalendar());
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
                    txtSpeechInput.setText(result.get(0));
                    this.buttonSaveToDb.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        progress.dismiss();
        if(result != null) {
            switch (result.getService()) {
                case SaveNote:
                    if (result.getSuccess()) {
                        //TODO: find out why this doesn't appear
                        Toast.makeText(context, SUCCESSFULLY_SENT_NOTE, Toast.LENGTH_LONG);
                    } else {
                        //TODO: get these strings out of here
                        Utils.makeAlert(context, "A problem occurred", "Your note was not saved");
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
