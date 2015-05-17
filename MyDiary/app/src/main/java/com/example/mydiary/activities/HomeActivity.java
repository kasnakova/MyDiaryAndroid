package com.example.mydiary.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.example.mydiary.utilities.Constants;
import com.example.mydiary.controllers.SettingsDialog;
import com.example.mydiary.http.MyDiaryHttpRequester;
import com.example.mydiary.http.MyDiaryHttpResult;
import com.example.mydiary.models.MyDiaryUserModel;
import com.example.mydiary.R;
import com.example.mydiary.adapters.TabsPagerAdapter;
import com.example.mydiary.interfaces.IMyDiaryHttpResponse;
import com.example.mydiary.utilities.Logger;
import com.example.mydiary.utilities.SettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        myDiaryHttpRequester = new MyDiaryHttpRequester(context, SettingsManager.isOffline(context), this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        if(!SettingsManager.isOffline(context)) {
            checkIfLogged();
        }

        //TODO: put the hints in the login
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
//
//        sharedPreferences.edit().remove(Constants.REQUEST_CODES).commit();
//        sharedPreferences.edit().remove(Constants.NOTES).commit();
//        sharedPreferences.edit().remove(Constants.DATES).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myDiaryHttpRequester.setIsOffline(SettingsManager.isOffline(context));
    }

    public void checkIfLogged(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString(Constants.TOKEN, Constants.EMPTY_STRING);

        if(accessToken.equals(Constants.EMPTY_STRING)){
            login();
        } else {
            MyDiaryUserModel.setToken(accessToken);
            myDiaryHttpRequester.getName();
        }
    }

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
        if(SettingsManager.isOffline(context)){
            menu.findItem(R.id.action_online).setVisible(true);
        } else {
            menu.findItem(R.id.action_logout).setVisible(true);
        }

        setUpMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private boolean onClickMenuOptions(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
               onSettingsMenuItemClicked();
                return true;
            case R.id.action_reminders:
                onRemindersMenuItemClicked();
                return true;
            case R.id.action_help:
                onHelpMenuItemClicked();
                return true;
            case R.id.action_online:
                onGoOnlineMenuItemClicked();
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

    private void setUpMenu(Menu menu){
        menu.add(0, 0, 1, getString(R.string.action_reminders)).setIcon(R.drawable.alarm).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onRemindersMenuItemClicked();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 0, 1, getString(R.string.action_settings)).setIcon(R.drawable.settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onSettingsMenuItemClicked();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        menu.add(0, 0, 1, "Help").setIcon(R.drawable.help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                onHelpMenuItemClicked();
//                return true;
//            }
//        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        if(SettingsManager.isOffline(context)){
//            menu.add(0, 0, 1, "GoOnline").setIcon(R.drawable.online).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem) {
//                    onGoOnlineMenuItemClicked();
//                    return true;
//                }
//            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        } else {
//            menu.add(0, 0, 1, "Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem) {
//                    logout();
//                    return true;
//                }
//            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        }

        //Logic for adding the name of the user in the right side of the action bar
        this.name = new TextView(this);
        String title = Constants.MODE_OFFLINE;
        if(!SettingsManager.isOffline(context)){
            title = MyDiaryUserModel.getName();
        }

        this.name.setText(title);
        this.name.setPadding(5, 0, 5, 0);
        this.name.setTextSize(20);
        menu.add(0, 0, 1, getString(R.string.action_name)).setActionView(this.name).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void onRemindersMenuItemClicked(){
        Intent intent = new Intent(this, ReminderActivity.class);
        startActivity(intent);
    }

    private void onSettingsMenuItemClicked(){
        SettingsDialog settingsDialog = new SettingsDialog(context);
        settingsDialog.show();
    }

    private void onHelpMenuItemClicked(){
        Intent intentHelp = new Intent(this, HelpActivity.class);
        startActivity(intentHelp);
    }

    private void onGoOnlineMenuItemClicked(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        sharedPreferences.edit().putBoolean(Constants.IS_OFFLINE, false).commit();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        //startActivityForResult(intent, REQ_CODE_LOGIN);
        startActivity(intent);
        finish();
    }

    private void login() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
       // startActivityForResult(intent, REQ_CODE_LOGIN);
        startActivity(intent);
        finish();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle(Constants.TITLE_LOGOUT)
                .setMessage(Constants.MESSAGE_LOGOUT)
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
                .setTitle(Constants.TITLE_NO_CONNECTION)
                .setMessage(Constants.MESSAGE_NO_CONNECTION)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        login();
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQ_CODE_LOGIN: {
//                if (resultCode == RESULT_CANCELED) {
//                    finish();
//                } else {
//                    invalidateOptionsMenu();
//                }
//                break;
//            }
//        }
//    }

    @Override
    public void myDiaryProcessFinish(MyDiaryHttpResult result) {
        if(result != null) {
            switch (result.getService()) {
                case Name:
                    if (result.getSuccess()) {
                        String name = result.getData().replace("\"", "");
                        MyDiaryUserModel.setName(name);
                        invalidateOptionsMenu();
                        Logger.getInstance().logMessage(TAG, "User had been logged in");
                    } else {
                        login();
                        Logger.getInstance().logMessage(TAG, "User wasn't logged in");
                    }
                    break;
                case Logout:
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sharedPreferences.edit().remove(Constants.TOKEN).commit();
                    Logger.getInstance().logMessage(TAG, "User logged out");
                    break;
                default:
                    break;
            }
        } else {
            Logger.getInstance().logMessage(TAG, "The result of the http request was null");
            noConnectivity();
        }
    }
}
