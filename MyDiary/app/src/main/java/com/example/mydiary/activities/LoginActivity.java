package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import com.example.mydiary.models.MyDiaryUserModel;
import com.example.mydiary.R;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.utilities.JsonManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Logger;
import com.example.mydiary.utilities.SettingsManager;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity implements IMyDiaryHttpResponse {
    private final String TAG = "LoginActivity";

    private Activity context = this;
    private MyDiaryHttpRequester myDiaryHttpRequester;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonToRegister;
    private Button buttonOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(this, SettingsManager.isOffline(context), context);
        this.editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        this.editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        this.buttonLogin = (Button) findViewById(R.id.buttonLogin);
        this.buttonToRegister = (Button) findViewById(R.id.buttonToRegister);
        this.buttonOffline = (Button) findViewById(R.id.buttonOffline);
        this.buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
        this.buttonToRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                register();
            }
        });
        this.buttonOffline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goOffline();
            }
        });
        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        buttonLogin.callOnClick();
                        return true;
                    default:
                        break;
                }

                return  false;
            }
        });
    }

    private void goOffline(){
        new AlertDialog.Builder(this)
                .setTitle(Constants.TITLE_CONTINUE_OFFLINE)
                .setMessage(Constants.MESSAGE_CONTINUE_OFFLINE)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                        sharedPreferences.edit().putBoolean(Constants.IS_OFFLINE, true).commit();
                        myDiaryHttpRequester.setIsOffline(true);
                        Logger.getInstance().logMessage(TAG, "User switched to offline mode");
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }

    private void login(){
        String email = this.editTextEmail.getText().toString();
        String password = this.editTextPassword.getText().toString();

        if(email == null || email.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_EMAIL_CANNOT_BE_EMPTY);
            return;
        }

        if(password == null || password.equals(Constants.EMPTY_STRING)){
            DialogManager.makeAlert(this, Constants.TITLE_INVALID_INPUT, Constants.MESSAGE_PASSWORD_CANNOT_BE_EMPTY);
            return;
        }

        myDiaryHttpRequester.login(email, password);
    }

    private void register(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_activty, menu);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            if(result != null) {
                switch (result.getService()) {
                    case Login:
                        JSONObject obj = JsonManager.makeJson(result.getData());
                        if (result.getSuccess()) {
                            String accessToken = obj.getString(Constants.JSON_ACCESS_TOKEN);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString(Constants.TOKEN, accessToken).commit();
                            MyDiaryUserModel.setToken(accessToken);
                            Logger.getInstance().logMessage(TAG, "Access token obtained");
                            this.myDiaryHttpRequester.getName();
                        } else {
                            DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_WITH_LOGIN, obj.getString(Constants.JSON_ERROR_DESCRIPTION));
                            Logger.getInstance().logMessage(TAG, "Problem logging in: " + obj.getString(Constants.JSON_ERROR_DESCRIPTION));
                        }
                        break;
                    case Name:
                        if (result.getSuccess()) {
                            Logger.getInstance().logMessage(TAG, "Success with getting the name: " + result.getData());
                            //to remove the quotes
                            String name = result.getData().replace("\"", "");
                            MyDiaryUserModel.setName(name);
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Logger.getInstance().logMessage(TAG, "Problem with getting the name. Result wasn't successful.");
                        }
                        break;
                    default:
                        break;
                }
            } else {
                DialogManager.NoInternetOrServerAlert(this);
                Logger.getInstance().logMessage(TAG, "The result of the http request was null");
            }
        } catch (JSONException ex){
            Logger.getInstance().logError(TAG, ex);
        }
    }
}
