package com.example.mydiary;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
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

public class RecordFragment extends Fragment {
    private EditText txtSpeechInput;
    private Button btnSpeak;
    private Button buttonSaveToDb;
    private Button buttonClear;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Activity context = getActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        txtSpeechInput = (EditText) rootView.findViewById(R.id.txtSpeechInput);
        btnSpeak = (Button) rootView.findViewById(R.id.btnSpeak);
        buttonSaveToDb = (Button) rootView.findViewById(R.id.buttonSaveToDb);
        buttonClear = (Button) rootView.findViewById(R.id.buttonClear);

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
//        ParseObject object = new ParseObject("Diary");
//        ParseUser currentUser = ParseUser.getCurrentUser();
//        object.put("Text", this.txtSpeechInput.getText().toString());
//        object.put("User", currentUser);
//
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH) + 1;
//        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
//        String date = String.valueOf(dayOfMonth) + "." +
//        				String.valueOf(month) + "." +
//        				String.valueOf(year);
//        System.out.println(date);
//        object.put("Date", date);
//
//        object.saveInBackground(new SaveCallback() {
//			@Override
//			public void done(ParseException arg0) {
//				String msg = getString(R.string.success_saving_to_db);
//				if(arg0 != null){
//				msg = getString(R.string.error_saving_to_db) + arg0.getMessage();
//				}
//
//				Toast.makeText(getActivity(),
//						msg,
//	                    Toast.LENGTH_SHORT).show();
//			}
//		} );
    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");//"en-US","bg-BG"//.getDefault());
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
}
