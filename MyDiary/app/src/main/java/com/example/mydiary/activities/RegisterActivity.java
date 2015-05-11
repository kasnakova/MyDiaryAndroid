package com.example.mydiary.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.example.mydiary.R;
import com.example.mydiary.utilities.DialogManager;
import com.example.mydiary.utilities.JsonManager;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Utils;

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

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(this, Utils.isOffline(this), this);

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

        if(email == null || email.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Email cannot be empty!");
            return;
        }

        if(name == null || name.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Name cannot be empty!");
            return;
        }

        if(password == null || password.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Password cannot be empty!");
            return;
        }

        if(confirmPassword == null || confirmPassword.equals("")){
            DialogManager.makeAlert(this, "Invalid input", "Password confirmation cannot be empty!");
            return;
        }

        if(!password.equals(confirmPassword)){
            DialogManager.makeAlert(this, "Invalid input", "The password confirmation doesn't match!");
            return;
        }

        if(password.length() < Constants.MIN_PASSWORD_LENGTH){
            DialogManager.makeAlert(this, "Invalid input", "The password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters long!");
            return;
        }

        myDiaryHttpRequester.register(email, password, name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        try {
            if (result != null) {
                switch (result.getService()) {
                    case Register:
                        if (result.getSuccess()) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Successful registration")
                                    .setMessage("You may login now")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setIcon(R.drawable.diary)
                                    .show();
                        } else {
                            JSONObject obj = JsonManager.makeJson(result.getData());
                            JSONObject errorObj = JsonManager.makeJson(obj.getString("ModelState"));
                            String error = errorObj.getJSONArray("").getString(0);
                            DialogManager.makeAlert(this, "Problem with registering", error);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                DialogManager.NoInternetOrServerAlert(this);
            }
        }catch(JSONException ex){
            Log.d(TAG, "JSONException: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }
}
