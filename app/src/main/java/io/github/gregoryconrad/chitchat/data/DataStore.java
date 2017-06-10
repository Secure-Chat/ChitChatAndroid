package io.github.gregoryconrad.chitchat.data;

import android.content.Context;

import java.util.ArrayList;

/**
 * A bridge for accessing the data in SQLite Databases
 */
public class DataStore {
    public static ArrayList<String> getIPs(Context context) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        ArrayList<String> returnVal = new ArrayList<>();
        for (DataTypes.ChatRoom curr : roomsDB.getRooms()) {
            if (returnVal.indexOf(curr.getIP()) < 0) returnVal.add(curr.getIP());
        }
        roomsDB.close();
        return returnVal;
    }

    public static ArrayList<DataTypes.ChatRoom> getRoomsForIP(Context context, String ip) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        ArrayList<DataTypes.ChatRoom> returnVal = new ArrayList<>();
        for (DataTypes.ChatRoom curr : roomsDB.getRooms()) {
            if (curr.getIP().equals(ip)) returnVal.add(curr);
        }
        roomsDB.close();
        return returnVal;
    }

    public static void addRoom(Context context, String ip,
                               String room, String nickname, String password) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.addRoom(ip, room, nickname, password);
        roomsDB.close();
    }

    public static void updateRoomColor(Context context, String ip, String room, int color) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.updateRoomColor(ip, room, color);
        roomsDB.close();
    }

    public static DataTypes.ChatRoom getRoom(Context context, String ip, String room) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        DataTypes.ChatRoom returnVal = roomsDB.getRoom(ip, room);
        roomsDB.close();
        return returnVal;
    }

    public static void removeRoom(Context context, String ip, String room) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.removeRoom(ip, room);
        roomsDB.close();
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
