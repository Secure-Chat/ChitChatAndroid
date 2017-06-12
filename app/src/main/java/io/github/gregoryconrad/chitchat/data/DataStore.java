package io.github.gregoryconrad.chitchat.data;

import android.content.Context;

import java.util.ArrayList;

/**
 * A bridge for accessing the data in SQLite Databases
 */
public class DataStore {
    public static ArrayList<DataTypes.ChatRoom> getRooms(Context context) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        ArrayList<DataTypes.ChatRoom> returnVal = roomsDB.getRooms();
        roomsDB.close();
        return returnVal;
    }

    public static void addRoom(Context context, String ip,
                               String room, String nickname, String password) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.addRoom(ip, room, nickname, password);
        roomsDB.close();
    }

    public static void updateRoomColor(Context context, DataTypes.ChatRoom room, int color) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.updateRoomColor(room, color);
        roomsDB.close();
    }

    public static DataTypes.ChatRoom getRoom(Context context, String ip, String room) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        DataTypes.ChatRoom returnVal = roomsDB.getRoom(ip, room);
        roomsDB.close();
        return returnVal;
    }

    public static void removeRoom(Context context, DataTypes.ChatRoom room) {
        RoomsDBHelper roomsDB = new RoomsDBHelper(context);
        roomsDB.removeRoom(room);
        roomsDB.close();
    }

    public static ArrayList<DataTypes.ChatMessage> getMessages(Context context,
                                                               DataTypes.ChatRoom room) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        ArrayList<DataTypes.ChatMessage> returnVal = messageDB.getMessages(room);
        messageDB.close();
        return returnVal;
    }

    public static void addMessage(Context context, DataTypes.ChatRoom room,
                                  int id, String name, String message) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        messageDB.addMessage(room, id, name, message);
        messageDB.close();
    }

    public static void removeMessage(Context context, DataTypes.ChatRoom room, int id) {
        MessageDBHelper messageDB = new MessageDBHelper(context);
        messageDB.deleteMessage(room, id);
        messageDB.close();
    }
}
