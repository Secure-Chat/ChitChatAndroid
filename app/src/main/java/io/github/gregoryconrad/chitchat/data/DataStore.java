package io.github.gregoryconrad.chitchat.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A bridge for accessing the data in SQLite Databases
 */
public class DataStore {
    public static ArrayList<String> getIPs(Context context) {
        ChatsDBHelper chatsDB = new ChatsDBHelper(context);
        ArrayList<String> returnVal = new ArrayList<>();
        for (HashMap<String, String> curr : chatsDB.getChats()) {
            if (returnVal.indexOf(curr.get("IP")) < 0) returnVal.add(curr.get("IP"));
        }
        chatsDB.close();
        return returnVal;
    }

    public static ArrayList<DataTypes.ChatRoom> getRoomsForIP(Context context, String ip) {
        ChatsDBHelper chatsDB = new ChatsDBHelper(context);
        ArrayList<DataTypes.ChatRoom> returnVal = new ArrayList<>();
        for (HashMap<String, String> curr : chatsDB.getChats()) {
            if (curr.get("IP").equals(ip)) returnVal.add(new DataTypes().new ChatRoom(
                    curr.get("IP"), curr.get("ROOM"), curr.get("NICKNAME"), curr.get("PASSWORD")));
        }
        chatsDB.close();
        return returnVal;
    }

    public static void addRoom(Context context, String ip,
                               String room, String nickname, String password) {
        ChatsDBHelper chatsDB = new ChatsDBHelper(context);
        chatsDB.addChat(ip, room, nickname, password);
        chatsDB.close();
    }

    public static void removeRoom(Context context, String ip, String room) {
        ChatsDBHelper chatsDB = new ChatsDBHelper(context);
        chatsDB.removeChat(ip, room);
        chatsDB.close();
    }

    public static ArrayList<DataTypes.ChatMessage> getMessages(Context context,
                                                                     String ip, String room) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        ArrayList<DataTypes.ChatMessage> returnVal = messageDB.getMessages(ip, room);
        messageDB.close();
        return returnVal;
    }

    public static void addMessage(Context context, String ip,
                                  String room, int id, String name, String message) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        messageDB.addMessage(ip, room, id, name, message);
        messageDB.close();
    }

    public static void removeMessage(Context context, String ip, String room, int id) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        messageDB.deleteMessage(ip, room, id);
        messageDB.close();
    }
}
