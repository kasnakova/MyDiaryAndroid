package com.example.mydiary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.myDiaryHttpRequester = new MyDiaryHttpRequester(context);
        this.editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        this.editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        this.buttonLogin = (Button) findViewById(R.id.buttonLogin);
        this.buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

   private void login(){
       progress = ProgressDialog.show(this, null,
               null, true);
       myDiaryHttpRequester.login("liza_93@abv.bg", "elizaveta");
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
            switch (result.getService()) {
                case Login:
                    if (result.getSuccess()) {
                        JSONObject obj = Utils.makeJson(result.getData());
                        String accessToken = obj.getString("access_token");
                        Log.d(TAG, accessToken);
                        this.myDiaryHttpRequester.getName(accessToken);
//                    Intent resultIntent = new Intent();
//                    setResult(Activity.RESULT_OK, resultIntent);
//                    finish();
                    }
                    break;
                case Name:
                    if (result.getSuccess()) {
                        Log.d(TAG, "name: " + result.getData());
                    }
                    break;
                default:
                    break;
            }
        } catch (JSONException ex){
            Log.d(TAG, "JSONException: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }
}
