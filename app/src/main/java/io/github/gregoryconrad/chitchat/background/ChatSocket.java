package io.github.gregoryconrad.chitchat.background;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.data.JSEncryption;
import io.github.gregoryconrad.chitchat.ui.MainActivity;

public class ChatSocket extends WebSocketClient {
    private MainActivity activity = null;

    public ChatSocket(MainActivity activity) {
        super(URI.create("ws://" + activity.getCurrRoom().getIP() + ":6789"));
        this.activity = activity;
        connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.i("ChatSocket", "Connection to the server has been established");
        this.send(new Gson().toJson(new DataTypes().new JSON("connect")
                .setRoom(activity.getCurrRoom().getRoom())
                .setName(activity.getCurrRoom().getNickname())));
        ArrayList<DataTypes.ChatMessage> messages =
                activity.getCurrRoom().getMessages(activity);
        this.send(new Gson().toJson(new DataTypes().new JSON("request")
                .setRoom(activity.getCurrRoom().getRoom())
                .setMin((messages.size() > 0) ?
                        messages.get(messages.size() - 1).getTimestamp().toString() : "0")
                .setMax(String.valueOf(System.currentTimeMillis()))));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.chatFrag.update();
                activity.connectDialog.dismiss();
                activity.pager.setCurrentItem(1);
            }
        });
    }

    @Override
    public void onMessage(final String s) {
        final DataTypes.JSON json = new Gson().fromJson(s, DataTypes.JSON.class);
        try {
            switch (json.getType()) {
                case "message":
                    if (json.getTimestamp() == null || json.getName() == null ||
                            json.getMsg() == null) throw new Exception();
                    JSEncryption.decrypt(activity, json.getMsg(),
                            activity.getCurrRoom().getPassword(),
                            new JsCallback() {
                                @Override
                                public void onResult(String txt) {
                                    activity.getCurrRoom().addMessage(activity,
                                            json.getTimestamp(), json.getName(), txt);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.chatFrag.update();
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    Log.i("ChatSocket", "Failed to decrypt a message: " + error);
                                }
                            });
                    break;
                case "server-message":
                    if (json.getRoom() == null || json.getMsg() == null)
                        throw new Exception();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(activity)
                                    .setTitle("Message from the server")
                                    .setMessage(json.getMsg())
                                    .setPositiveButton("Ok", null).create().show();
                        }
                    });
                    break;
            }

        } catch (Exception e) {
            Log.i("ChatSocket", "Got nonconforming data: " + s);
        }
    }

    @Override
    public void onClose(int i, String s, final boolean remote) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (remote) {
                    Log.w("ChatSocket", "Lost connection to the server");
                    Toast.makeText(activity, "Lost connection to the server",
                            Toast.LENGTH_SHORT).show();
                } else Log.w("ChatSocket", "Not connected to the server");
                activity.pager.setCurrentItem(0);
            }
        });
    }

    @Override
    public void onError(Exception e) {
        Log.e("ChatSocket", "Encountered error " + e.getMessage(), e);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.connectDialog.isShowing()) { // error during connection
                    activity.connectDialog.dismiss();
                    Log.w("ChatSocket", "Could not connect to the server");
                    Toast.makeText(activity, "Could not connect to the server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
