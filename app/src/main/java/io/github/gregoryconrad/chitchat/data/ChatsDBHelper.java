package io.github.gregoryconrad.chitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

class ChatsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CHATS.db";
    private static final String TABLE_NAME = "CHATS";
    private static final String COLUMN_IP = "IP";
    private static final String COLUMN_ROOM = "ROOM";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PASSWORD = "PASS";
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
                COLUMN_PASSWORD + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    ArrayList<HashMap<String, String>> getChats() {
        ArrayList<HashMap<String, String>> returnVal = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> curr = new HashMap<>();
                curr.put("IP", cursor.getString(cursor.getColumnIndex(COLUMN_IP)));
                curr.put("ROOM", cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)));
                curr.put("NICKNAME", cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                curr.put("PASSWORD", cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                returnVal.add(curr);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return returnVal;
    }

    void addChat(String ip, String room, String nickname, String password) {
        for (HashMap<String, String> currChat : getChats()) {
            if (currChat.get("IP").equals(ip) && currChat.get("ROOM").equals(room)) {
                ContentValues newValues = new ContentValues();
                newValues.put(COLUMN_NAME, nickname);
                newValues.put(COLUMN_PASSWORD, password);
                getWritableDatabase().update(TABLE_NAME, newValues,
                        COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?", new String[]{ip, room});
                return;
            }
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IP, ip);
        contentValues.put(COLUMN_ROOM, room);
        contentValues.put(COLUMN_NAME, nickname);
        contentValues.put(COLUMN_PASSWORD, password);
        getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    }

    void removeChat(String ip, String room) {
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=?", new String[]{ip, room});
    }
}
