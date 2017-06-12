package io.github.gregoryconrad.chitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * SQLite Database that holds all of the chat rooms
 */
class RoomsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ROOMS.db";
    private static final String TABLE_NAME = "ROOMS";
    private static final String COLUMN_IP = "IP";
    private static final String COLUMN_ROOM = "ROOM";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PASSWORD = "PASS";
    private static final String COLUMN_COLOR = "COLOR";
    private static final int CURRENT_VERSION = 1;

    RoomsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_IP + " TEXT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_COLOR + " INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * @return list of the saved chat rooms
     */
    ArrayList<DataTypes.ChatRoom> getRooms() {
        ArrayList<DataTypes.ChatRoom> returnVal = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                returnVal.add(new DataTypes().new ChatRoom(
                        cursor.getString(cursor.getColumnIndex(COLUMN_IP)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return returnVal;
    }

    /**
     * Adds a chat room to the database
     *
     * @param ip       the ip of the room
     * @param room     the room identifier
     * @param nickname the nickname to use for this room
     * @param password the password to use for this room
     */
    void addRoom(String ip, String room, String nickname, String password) {
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?",
                new String[]{ip, room});
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IP, ip);
        contentValues.put(COLUMN_ROOM, room);
        contentValues.put(COLUMN_NAME, nickname);
        contentValues.put(COLUMN_PASSWORD, password);
        contentValues.put(COLUMN_COLOR, 0xFFFFFFFF); // default to solid white
        getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    }

    /**
     * Changes the color for a specified room
     *
     * @param room  the ChatRoom to change the color of
     * @param color the color to use for this room
     */
    void updateRoomColor(DataTypes.ChatRoom room, int color) {
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_COLOR, color);
        getWritableDatabase().update(TABLE_NAME, newValues,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?",
                new String[]{room.getIP(), room.getRoom()});
    }

    /**
     * @param ip   the ip of the room to return
     * @param room the room identifier of the room to return
     * @return the room object for the specified room
     */
    DataTypes.ChatRoom getRoom(String ip, String room) {
        for (DataTypes.ChatRoom curr : getRooms()) {
            if (curr.getIP().equals(ip) && curr.getRoom().equals(room)) return curr;
        }
        return null;
    }

    /**
     * Removes a saved room from the database
     *
     * @param room the room to remove
     */
    void removeRoom(DataTypes.ChatRoom room) {
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?",
                new String[]{room.getIP(), room.getRoom()});
    }
}
