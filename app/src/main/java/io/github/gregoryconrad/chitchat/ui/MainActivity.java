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
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.data.JSEncryption;
import io.github.gregoryconrad.chitchat.data.RoomsDBHelper;

public class MainActivity extends AppCompatActivity {
    private SelectiveSwipeViewPager pager = null;
    private MainFragment mainFrag = new MainFragment();
    private ChatFragment chatFrag = new ChatFragment();
    private DataTypes.ChatRoom currRoom = null;
    private WebSocketClient chatSocket = null;
    private ProgressDialog connectDialog = null;

    @SuppressWarnings("SpellCheckingInspection") // to remove the warning for chat.etcg.pw
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setActionBarTitle(getString(R.string.app_name));

        //todo long click on messages and rooms
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
        if (this.chatSocket != null) this.chatSocket.close();
        if (currRoom != null) {
            this.chatSocket = new WebSocketClient(
                    URI.create("ws://" + currRoom.getIP() + ":6789")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i("ChatSocket", "Connection to the server has been established");
                    this.send(new Gson().toJson(new DataTypes().new JSON("connect")
                            .setRoom(currRoom.getRoom())
                            .setName(currRoom.getNickname())));
                    //TODO request messages
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatFrag.update();
                            connectDialog.dismiss();
                            pager.setCurrentItem(1);
                        }
                    });
                }

                @Override
                public void onMessage(final String s) {
                    DataTypes.JSON json = new Gson().fromJson(s, DataTypes.JSON.class);
                    try {
                        switch (json.getType()) {
                            case "message":
                                if (json.getTimestamp() == null || json.getName() == null ||
                                        json.getMsg() == null) throw new Exception();
                                currRoom.addMessage(MainActivity.this,
                                        json.getTimestamp(), json.getName(), json.getMsg());
                                chatFrag.update();
                                break;
                            case "server-message":
                                if (json.getRoom() == null || json.getMsg() == null)
                                    throw new Exception();
                                //todo add directly to view
                                break;
                        }

                    } catch (Exception e) {
                        Log.i("ChatSocket", "Got nonconforming data: " + s);
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.w("ChatSocket", "Not connected to the server");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (connectDialog.isShowing()) {
                                connectDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Could not connect to the server",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Lost connection to the server",
                                        Toast.LENGTH_SHORT).show();
                                pager.setCurrentItem(0);
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("ChatSocket", "Encountered error " + e.getMessage());
                    e.printStackTrace();
                }
            };
            this.chatSocket.connect();
        }
    }

    @SuppressWarnings("unused")
    public DataTypes.ChatRoom getCurrRoom() {
        return this.currRoom;
    }

    @SuppressWarnings("unused")
    public void setCurrRoom(DataTypes.ChatRoom room) {
        this.currRoom = room;
        this.connectDialog = ProgressDialog.show(this, "Connecting...",
                "Connecting to " + room.getIP(), true);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startChatSocket();
            }
        }, 500);
    }

    @SuppressWarnings("unused")
    public void sendMessage(String message) {
        JSEncryption.encrypt(this, message,
                this.currRoom.getPassword(),
                new JSEncryption.EncryptCallback() {
                    @Override
                    public void onResult(String txt) {
                        try {
                            chatSocket.send(new Gson().toJson(new DataTypes().
                                    new JSON("message").setRoom(currRoom.getRoom()).setMsg(txt)));
                        } catch (Exception e) {
                            Log.e("ChatSocket", "Could not send a message: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Could not send your message",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "Failed to encrypt your message",
                                Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", "Failed to encrypt the message: " + error);
                    }
                });

    }

    /**
     * Sets the ActionBar's text with the correct color
     *
     * @param title The title to set the ActionBar
     * @return true if the text changed, false otherwise
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
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
        if (this.pager.getCurrentItem() != 0) {
            this.pager.setCurrentItem(0);
        } else moveTaskToBack(true);
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
                Toast.makeText(this, "About is to come", Toast.LENGTH_SHORT).show();
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
