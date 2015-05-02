package com.example.mydiary;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.util.GregorianCalendar;

public class HomeActivity extends FragmentActivity implements ActionBar.TabListener, OnMenuItemClickListener, IMyDiaryHttpResponse {
    private final int REQ_CODE_LOGIN = 200;
    private final String TAG = "HomeActivity";

    private IMyDiaryHttpResponse context = this;
    private MyDiaryHttpRequester myDiaryHttpRequester;
    public ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private TextView name;
    private ProgressDialog progress;

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
        myDiaryHttpRequester = new MyDiaryHttpRequester(context);
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);

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


        checkIfLogged();


        //-----------------------------------
//        Intent intent = new Intent(this, Mote.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1253, intent, PendingIntent.FLAG_UPDATE_CURRENT|  Intent.FILL_IN_DATA);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        GregorianCalendar cal = Utils.getGregorianCalendarFromString("2015-05-02T05:54:00");
//        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),pendingIntent );
//        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
//        alarm.SetAlarm(this);
//        alarm.setOnetimeTimer(this);

       // Toast.makeText(this, "Alarm worked.", Toast.LENGTH_LONG).show();
    }

    public void checkIfLogged(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString("token", "");

        if(accessToken.equals("")){
            login();
        } else {
            progress = ProgressDialog.show(this, null, null, true);
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
        setUpPopupMenu(menu);

        return true;
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

    private boolean onClickMenuOptions(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        progress.show();
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
        progress.dismiss();
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
