package com.example.mydiary.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.example.mydiary.Constants;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.models.MyDiaryUserModel;
import com.example.mydiary.R;
import com.example.mydiary.adapters.TabsPagerAdapter;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Utils;

public class HomeActivity extends FragmentActivity implements ActionBar.TabListener, OnMenuItemClickListener, IMyDiaryHttpResponse {
    private final int REQ_CODE_LOGIN = 200;
    private final String TAG = "HomeActivity";

    private HomeActivity context = this;
    private MyDiaryHttpRequester myDiaryHttpRequester;
    public ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private TextView name;

    public ViewPager getViewPager(){
        return this.viewPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Remove focus on EditText when activity starts
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Initilization
        myDiaryHttpRequester = new MyDiaryHttpRequester(context, Utils.isOffline(context), this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        checkIfLogged();
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
//
//        sharedPreferences.edit().remove(Constants.REQUEST_CODES).commit();
//        sharedPreferences.edit().remove(Constants.NOTES).commit();
//        sharedPreferences.edit().remove(Constants.DATES).commit();
        //TODO: remove this if not needed later
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                //  actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        //TODO: REFACTOR
        //TODO: make a pretty UI

        //TODO: add 'help' in the menu and implements it
    }

    public void checkIfLogged(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString("token", "");

        if(accessToken.equals("")){
            login();
        } else {
            MyDiaryUserModel.setToken(accessToken);
            myDiaryHttpRequester.getName();
        }
    }

    //TODO: check if these are needed
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        //viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if(Utils.isOffline(context)){
            menu.findItem(R.id.action_online).setVisible(true);
        } else {
            menu.findItem(R.id.action_logout).setVisible(true);
        }

        setUpPopupMenu(menu);

        return true;
    }

    private boolean onClickMenuOptions(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_reminders:
                Intent intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_online:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                sharedPreferences.edit().putBoolean(Constants.IS_OFFLINE, false).commit();
                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        return onClickMenuOptions(item);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onClickMenuOptions(item);
    }

    private void setUpPopupMenu(Menu menu){


        this.name = new TextView(this);
        this.name.setText(MyDiaryUserModel.getName());
        this.name.setPadding(5, 0, 5, 0);
        this.name.setTextSize(20);
        this.name.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(HomeActivity.this, v);
                popupMenu.setOnMenuItemClickListener(HomeActivity.this);
                popupMenu.inflate(R.menu.main);
                popupMenu.show();

            }
        });
        menu.add(0, 0, 1, "Title").setActionView(this.name).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //  }
    }



    private void login() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivityForResult(intent, REQ_CODE_LOGIN);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        myDiaryHttpRequester.logout();
                        login();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }

    private void noConnectivity() {
        new AlertDialog.Builder(this)
                .setTitle("No connection to internet or server")
                .setMessage("Please check your connection. You will be redirected to the login screen")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        login();
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_LOGIN: {
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else {
                    invalidateOptionsMenu();
                }
                break;
            }
        }
    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        if(result != null) {
            switch (result.getService()) {
                case Name:
                    if (result.getSuccess()) {
                        String name = result.getData().replace("\"", "");
                        MyDiaryUserModel.setName(name);
                        invalidateOptionsMenu();
                    } else {
                        login();
                    }
                    break;
                case Logout:
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sharedPreferences.edit().remove("token").commit();
                    break;
                default:
                    break;
            }
        } else {
            noConnectivity();
        }
    }
}
