package io.github.gregoryconrad.chitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.jar.Pack200;

class ChatsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CHATS.db";
    private static final String TABLE_NAME = "CHATS";
    private static final String COLUMN_IP = "IP";
    private static final String COLUMN_ROOM = "ROOM";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PASSWORD = "PASS";
    private static final String COLUMN_COLOR = "COLOR";
    private static final int CURRENT_VERSION = 1;

    ChatsDBHelper(Context context) {
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

    ArrayList<DataTypes.ChatRoom> getChats() {
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

    void addChat(String ip, String room, String nickname, String password) {
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?",
                new String[]{ip, room});
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IP, ip);
        contentValues.put(COLUMN_ROOM, room);
        contentValues.put(COLUMN_NAME, nickname);
        contentValues.put(COLUMN_PASSWORD, password);
        contentValues.put(COLUMN_COLOR, 0xFFFFFFFF);
        getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    }

    void updateRoomColor(String ip, String room, int color) {
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_COLOR, color);
        getWritableDatabase().update(TABLE_NAME, newValues,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?", new String[]{ip, room});
    }

    DataTypes.ChatRoom getRoom(String ip, String room) {
        for (DataTypes.ChatRoom curr : getChats()) {
            if (curr.getIP().equals(ip) && curr.getRoom().equals(room)) return curr;
        }
        return null;
    }

    void removeChat(String ip, String room) {
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?", new String[]{ip, room});
    }
}
