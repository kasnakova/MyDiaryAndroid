package com.example.mydiary;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements ActionBar.TabListener, OnMenuItemClickListener, IAsyncResponse {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private final int REQ_CODE_LOGIN = 200;
    private IAsyncResponse context = this;

    final String TAG = "HomeActivity";

    @Override
    public void processFinish(String data) {
        Log.d("HomeActivity", "Data: " + data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Remove focus on EditText when activity starts
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      //  MyDiaryHttpRequester myDiaryHttpRequester = new MyDiaryHttpRequester(context);
      //  myDiaryHttpRequester.Register("pesho@abv.bg", "petrov", "Petar");
        //Login logic
        //   ParseUser currentUser = ParseUser.getCurrentUser();
//	        if(currentUser == null) {
	        	login();
//	        }




//        try {
//            final String urlParameters = "Email=" + "kjslk@anc.vd" +
//                    "&Password=" + "ksjdsl" +
//                    "&ConfirmPassword=" + "ksjdsl" +
//                    "&Name=" + "Liza";
//            Log.d(TAG, "Someone here?");
//
//          //  Runnable r = new Runnable() {
//              //  @Override
//             //   public void run() {
////                    Log.d(TAG, "in the runnable");
////                    new HttpRequester(context)
////                            .execute(
////                                    "http://192.168.0.147:50264/Token",
////                                    "POST",
////                                    "grant_type=password&username=shasha@abv.bg&password=abracadabra");
//                    //"Email=shasha@abv.bg&Password=abracadabra&ConfirmPassword=abracadabra&Name=Maria");
//           //     }
//        //    };
//            Log.d(TAG, "oskpa her?");
//        } catch(Exception ex) {
//            Log.d(TAG, "Exception: " + ex.toString() + " | Message: " + ex.getMessage());
//        }





        // Initilization
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
    }

    public void register(String email, String password, String name) {
        try {
            final String urlParameters = "Email=" + email +
                    "&Password=" + password +
                    "&ConfirmPassword=" + password +
                    "&Name=" + name;
            Log.d(TAG, "Someone here?");

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "in the runnable");
                    new HttpRequester(context)
                            .execute(
                                    "http://192.168.0.147:50264/Token",
                                    "POST",
                                    "grant_type=password&username=shasha@abv.bg&password=abracadabra");
                    //"Email=shasha@abv.bg&Password=abracadabra&ConfirmPassword=abracadabra&Name=Maria");
                }
            };
            Log.d(TAG, "oskpa her?");
        } catch(Exception ex) {
            Log.d(TAG, "Exception: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    //TODO: check if these are needed
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
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
        //ParseUser currentUser = ParseUser.getCurrentUser();
        //  if(currentUser != null){
        TextView tv = new TextView(this);
        // tv.setText(currentUser.getString("name"));
        tv.setText("Liza");
        tv.setPadding(5, 0, 5, 0);
        tv.setTextSize(20);
        tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(HomeActivity.this, v);
                popupMenu.setOnMenuItemClickListener(HomeActivity.this);
                popupMenu.inflate(R.menu.main);
                popupMenu.show();

            }
        });
        menu.add(0, 0, 1, "Title").setActionView(tv).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
                        //  	ParseUser.logOut();
                        login();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
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
}
