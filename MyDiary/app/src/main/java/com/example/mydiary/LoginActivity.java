package com.example.mydiary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
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

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity implements  IMyDiaryHttpResponse {
    private final String TAG = "LoginActivity";

    private ProgressDialog progress;
    private IMyDiaryHttpResponse context = this;
    private MyDiaryHttpRequester myDiaryHttpRequester;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(context);
        this.editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        this.editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        this.buttonLogin = (Button) findViewById(R.id.buttonLogin);
        this.buttonToRegister = (Button) findViewById(R.id.buttonToRegister);
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

        //TODO: make it on boot as well
//        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
//        final String SOME_ACTION = "com.example.mydiary.AlarmManagerBroadcastReceiver";
//        IntentFilter intentFilter = new IntentFilter(SOME_ACTION);
//        registerReceiver(alarm, intentFilter);
//       //alarm.SetAlarm(getApplicationContext());
//        alarm.setOnetimeTimer(getApplicationContext());
    }

    private void login(){
        String email = this.editTextEmail.getText().toString();
        String password = this.editTextPassword.getText().toString();

        if(email == null || email.equals("")){
            Utils.makeAlert(this, "Invalid input", "Email cannot be empty!");
            return;
        }

        if(password == null || password.equals("")){
            Utils.makeAlert(this, "Invalid input", "Password cannot be empty!");
            return;
        }

        progress = ProgressDialog.show(this, null, null, true);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        progress.dismiss();

        try {
            if(result != null) {
                switch (result.getService()) {
                    case Login:
                        JSONObject obj = Json.makeJson(result.getData());
                        if (result.getSuccess()) {
                            String accessToken = obj.getString("access_token");
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString("token", accessToken).commit();
                            MyDiaryUserModel.setToken(accessToken);
                            Log.d(TAG, accessToken);
                            progress.show();
                            this.myDiaryHttpRequester.getName();
                        } else {
                            Utils.makeAlert(this, "Problem with login", obj.getString("error_description"));
                        }
                        break;
                    case Name:
                        if (result.getSuccess()) {
                            Log.d(TAG, "name: " + result.getData());
                            //to remove the quotes
                            String name = result.getData().replace("\"", "");
                            MyDiaryUserModel.setName(name);
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                        break;
                    default:
                        break;
                }
            } else {
                Utils.NoInternetOrServerAlert(this);
            }
        } catch (JSONException ex){
            Log.d(TAG, "JSONException: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }
}
