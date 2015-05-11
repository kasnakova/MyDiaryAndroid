package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.example.mydiary.Constants;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.models.MyDiaryUserModel;
import com.example.mydiary.R;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.utilities.JsonManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Utils;

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

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(this, Utils.isOffline(context), context);
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
                .setTitle("Continue offline?")
                .setMessage("You will only be able to use the reminder functionality. Do you want to proceed offline?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                        sharedPreferences.edit().putBoolean(Constants.IS_OFFLINE, true).commit();
                        myDiaryHttpRequester.setIsOffline(true);
                        Intent intent = new Intent(context, HomeActivity.class);
                        startActivity(intent);
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

        if(email == null || email.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Email cannot be empty!");
            return;
        }

        if(password == null || password.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Password cannot be empty!");
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

        try {
            if(result != null) {
                switch (result.getService()) {
                    case Login:
                        JSONObject obj = JsonManager.makeJson(result.getData());
                        if (result.getSuccess()) {
                            String accessToken = obj.getString("access_token");
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString("token", accessToken).commit();
                            MyDiaryUserModel.setToken(accessToken);
                            Log.d(TAG, accessToken);
                            this.myDiaryHttpRequester.getName();
                        } else {
                            DialogManager.makeAlert(this, "Problem with login", obj.getString("error_description"));
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
                DialogManager.NoInternetOrServerAlert(this);
            }
        } catch (JSONException ex){
            Log.d(TAG, "JSONException: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }
}
