package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mydiary.utilities.Constants;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.R;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.utilities.JsonManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Logger;
import com.example.mydiary.utilities.SettingsManager;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends Activity implements IMyDiaryHttpResponse {
    private final String TAG = "RegisterActivity";

    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private MyDiaryHttpRequester myDiaryHttpRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(this, SettingsManager.isOffline(this), this);

        this.editTextEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        this.editTextName = (EditText) findViewById(R.id.editTextRegisterName);
        this.editTextPassword = (EditText) findViewById(R.id.editTextRegisterPassword);
        this.editTextConfirmPassword = (EditText) findViewById(R.id.editTextRegisterConfirmPassword);
        this.buttonRegister = (Button) findViewById(R.id.buttonRegister);
        this.buttonRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                register();
            }
        });
        //When the user presses done in the soft keyboard it will trigger the buttonRegister click
        this.editTextConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        buttonRegister.callOnClick();
                        return true;
                    default:
                        break;
                }

                return  false;
            }
        });
    }


    private void register(){
        String email = this.editTextEmail.getText().toString();
        String name = this.editTextName.getText().toString();
        String password = this.editTextPassword.getText().toString();
        String confirmPassword = this.editTextConfirmPassword.getText().toString();

        if(email == null || email.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_EMAIL_CANNOT_BE_EMPTY);
            return;
        }

        if(name == null || name.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_NAME_CANNOT_BE_EMPTY);
            return;
        }

        if(password == null || password.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_PASSWORD_CANNOT_BE_EMPTY);
            return;
        }

        if(confirmPassword == null || confirmPassword.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_PASSWORD_CONFIRM_CANNOT_BE_EMPTY);
            return;
        }

        if(!password.equals(confirmPassword)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_PASSWORD_CONFIRM_NOT_MATCH);
            return;
        }

        if(password.length() < Constants.MIN_PASSWORD_LENGTH){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, String.format(Constants.MESSAGE_PASSWORD_LENGTH, Constants.MIN_PASSWORD_LENGTH));
            return;
        }

        myDiaryHttpRequester.register(email, password, name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.add(0, 0, 1, getString(R.string.action_help)).setIcon(R.drawable.help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onHelpMenuItemClicked();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            onHelpMenuItemClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onHelpMenuItemClicked(){
        Intent intentHelp = new Intent(this, HelpActivity.class);
        startActivity(intentHelp);
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        try {
            if (result != null) {
                switch (result.getService()) {
                    case Register:
                        if (result.getSuccess()) {
                            new AlertDialog.Builder(this)
                                    .setTitle(Constants.TITLE_SUCCESSFUL_REGISTRATION)
                                    .setMessage(Constants.MESSAGE_SUCCESSFUL_REGISTRATION)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setIcon(R.drawable.diary)
                                    .show();
                            Logger.getInstance().logMessage(TAG, "Successful registration");
                        } else {
                            JSONObject obj = JsonManager.makeJson(result.getData());
                            JSONObject errorObj = JsonManager.makeJson(obj.getString(Constants.JSON_MODEL_STATE));
                            String error = errorObj.getJSONArray(Constants.EMPTY_STRING).getString(0);
                            DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_REGISTERING, error);
                            Logger.getInstance().logMessage(TAG, "Unsuccessful registration: " + error);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                DialogManager.NoInternetOrServerAlert(this);
                Logger.getInstance().logMessage(TAG, "The result of the http request was null");
            }
        }catch(JSONException ex){
            Logger.getInstance().logError(TAG, ex);
        }
    }
}
