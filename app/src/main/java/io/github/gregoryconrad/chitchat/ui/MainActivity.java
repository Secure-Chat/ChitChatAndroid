package io.github.gregoryconrad.chitchat.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.google.gson.Gson;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.background.ChatSocket;
import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.data.JSEncryption;
import io.github.gregoryconrad.chitchat.data.RoomsDBHelper;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
    public ChatSocket chatSocket = null;
    public ProgressDialog connectDialog = null;
    public SelectiveSwipeViewPager pager = null;
    public ChatFragment chatFrag = new ChatFragment();
    private MainFragment mainFrag = new MainFragment();
    private DataTypes.ChatRoom currRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setActionBarTitle(getString(R.string.app_name));

        //todo add AlarmManager (for boot) checking for new messages on all servers

        this.pager = (SelectiveSwipeViewPager) findViewById(R.id.pager);
        this.pager.setPageMargin(6);
        this.pager.setPageMarginDrawable(R.color.primary);
        this.pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) return mainFrag;
                return chatFrag;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        if (RoomsDBHelper.getRooms(this).size() < 1) new AlertDialog.Builder(this)
                .setTitle("Welcome to ChitChat!")
                .setMessage("In order to use this application, first add a chat room " +
                        "by clicking the + button at the top of the screen.")
                .setPositiveButton("Ok", null).create().show();
    }

    private void startChatSocket() {
        this.connectDialog = ProgressDialog.show(this, "Connecting...",
                "Connecting to " + currRoom.getIP(), true);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                chatSocket = new ChatSocket(MainActivity.this);
            }
        }, 350);
    }

    @SuppressWarnings("unused")
    public DataTypes.ChatRoom getCurrRoom() {
        return this.currRoom;
    }

    @SuppressWarnings("unused")
    public void setCurrRoom(DataTypes.ChatRoom room) {
        this.currRoom = room;
        startChatSocket();
    }

    @SuppressWarnings("unused")
    public void sendMessage(String message) {
        JSEncryption.encrypt(this, message,
                this.currRoom.getPassword(),
                new JsCallback() {
                    @Override
                    public void onResult(String txt) {
                        try {
                            chatSocket.send(new Gson().toJson(new DataTypes().
                                    new JSON("message").setRoom(currRoom.getRoom()).setMsg(txt)));
                        } catch (Exception e) {
                            Log.e("ChatSocket", "Could not send a message: " + e.getMessage(), e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,
                                            "Could not send your message",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("MainActivity", "Failed to encrypt the message: " + error);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed to encrypt your message",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

    }

    /**
     * Sets the ActionBar's text with the correct color
     *
     * @param title The title to set the ActionBar
     * @return true if the text changed, false otherwise
     */
    @SuppressWarnings("ConstantConditions")
    public boolean setActionBarTitle(String title) {
        try {
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#" +
                    Integer.toHexString(ContextCompat.getColor(this, R.color.accent) & 0x00ffffff) +
                    "\">" + title + "</font>"));
            return true;
        } catch (Exception e) {
            Log.e("MainActivity", "Could not change the ActionBar's title");
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (this.pager.getCurrentItem() != 0) this.pager.setCurrentItem(0);
        else super.onBackPressed();
    }

    /*
     * ActionBar Action Buttons
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @SuppressWarnings("ConstantConditions") // to remove NullPointerException warning
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                WebView aboutView = new WebView(this);
                aboutView.loadUrl("file:///android_asset/about.html");
                new AlertDialog.Builder(this).setView(aboutView)
                        .setPositiveButton("Close", null).create().show();
                break;
            case R.id.action_add:
                new AlertDialog.Builder(this)
                        .setTitle("Add a room")
                        .setView(R.layout.dialog_login)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AlertDialog ad = (AlertDialog) dialog;
                                RoomsDBHelper.addRoom(MainActivity.this,
                                        ((EditText) ad.findViewById(R.id.ip))
                                                .getText().toString(),
                                        ((EditText) ad.findViewById(R.id.room))
                                                .getText().toString(),
                                        ((EditText) ad.findViewById(R.id.name))
                                                .getText().toString(),
                                        ((EditText) ad.findViewById(R.id.password))
                                                .getText().toString());
                                mainFrag.update();
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Cancel", null).create().show();
                this.mainFrag.scrollToEnd();
                break;
        }
        return true;
    }
}
