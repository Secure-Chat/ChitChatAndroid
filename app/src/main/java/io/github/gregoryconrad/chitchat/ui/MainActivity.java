package io.github.gregoryconrad.chitchat.ui;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.data.DataStore;
import io.github.gregoryconrad.chitchat.data.DataTypes;

public class MainActivity extends AppCompatActivity {
    private SelectiveSwipeViewPager pager = null;
    private MainFragment mainFrag = new MainFragment();
    private ChatFragment chatFrag = new ChatFragment();
    private String currIP = null;
    private String currRoom = null;
    private WebSocketClient chatSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setActionBarTitle(getString(R.string.app_name));

        //todo long click on messages and rooms
        //todo add AlarmManager (for boot) checking for new messages on all servers
        //todo used sharedpref for saving curr ip and for notification settings
        //fixme there might be an issue with sorting messages,
        //fixme   if so use ids as a secondary compare feature if timestamps compare is 0
        // https://github.com/QuadFlask/colorpicker

        try {
            this.currIP = DataStore.getIPs(this).get(0);
        } catch (Exception e) {
            DataStore.addRoom(this, "chat.etcg.pw", "main", "", "");
            this.currIP = DataStore.getIPs(this).get(0);
        }
        this.currRoom = DataStore.getRoomsForIP(this,
                DataStore.getIPs(this).get(0)).get(0).getRoom();
        startChatSocket(this.currIP);

        {
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
        }
    }

    @SuppressWarnings("unused")
    public String getCurrIP() {
        return this.currIP;
    }

    @SuppressWarnings("unused")
    public String getCurrRoom() {
        return this.currRoom;
    }

    @SuppressWarnings("unused")
    public void setCurrRoom(String room) {
        this.currRoom = room;
        this.chatFrag.update();
        this.pager.setCurrentItem(1);
    }

    @SuppressWarnings("unused")
    public void sendMessage(String message) {
        try {
            this.chatSocket.send(new Gson().toJson(new DataTypes().new JSON("message")
                    .setRoom(currRoom).setMsg(message)));
        } catch (Exception e) {
            Log.e("ChatSocket", "Could not send a message: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Could not send your message", Toast.LENGTH_SHORT).show();
        }
    }

    private void startChatSocket(final String ip) {
        this.currIP = ip;
        if (this.chatSocket != null) this.chatSocket.close();
        this.chatSocket = new WebSocketClient(URI.create(ip + ":6789")) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("ChatSocket", "Connection to the server has been established");
                for (DataTypes.ChatRoom room : DataStore.getRoomsForIP(MainActivity.this, ip)) {
                    this.send(new Gson().toJson(new DataTypes().new JSON("connect")
                            .setRoom(room.getRoom())
                            .setName(room.getNickname())));
                }
            }

            @Override
            public void onMessage(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Received: " + s,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                DataTypes.JSON json = new Gson().fromJson(s, DataTypes.JSON.class);
                if ("message".equals(json.getType()) &&
                        json.getName() != null && json.getMsg() != null) {
                    DataStore.addMessage(MainActivity.this, ip, currRoom,
                            json.getId(), json.getName(), json.getMsg());
                    chatFrag.update();
                } else {
                    Log.i("ChatSocket", "Got nonconforming data: " + s);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("ChatSocket", "Not connected to the server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Not connected to the server " +
                                        "(no messages can be sent until app restart)",
                                Toast.LENGTH_LONG).show();
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
            Log.i(getString(R.string.app_name), "Could not change the ActionBar's title");
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
                        .setTitle("Add or update a room")
                        .setView(R.layout.dialog_login)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AlertDialog ad = (AlertDialog) dialog;
                                String room = ((EditText) ad.findViewById(R.id.room))
                                        .getText().toString();
                                if (room.length() < 1) room = "main";
                                DataStore.addRoom(MainActivity.this, currIP, room,
                                        ((EditText) ad.findViewById(R.id.nickname))
                                                .getText().toString(),
                                        ((EditText) ad.findViewById(R.id.password))
                                                .getText().toString());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create().show();
                this.mainFrag.scrollToEnd();
                break;
            case R.id.action_add_ip:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.actionbar_add_ip))
                        .setView(R.layout.dialog_add_ip)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                currIP = ((EditText) ((AlertDialog) dialog).findViewById(R.id.ip))
                                        .getText().toString();
                                DataStore.addRoom(MainActivity.this, currIP, "main", "", "");
                                startChatSocket(currIP);
                                dialog.dismiss();
                                mainFrag.update();
                                pager.setCurrentItem(0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create().show();
                break;
            case R.id.action_switch_ip:
                final ArrayAdapter<String> ips = new ArrayAdapter<>(this,
                        android.R.layout.select_dialog_item);
                ips.addAll(DataStore.getIPs(MainActivity.this));
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.actionbar_switch_ip))
                        .setAdapter(ips, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startChatSocket(ips.getItem(which));
                                dialog.dismiss();
                                mainFrag.update();
                                pager.setCurrentItem(0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create().show();
                break;
        }
        return true;
    }
}
