package io.github.gregoryconrad.chitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

class MessageDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MESSAGES.db";
    private static final String TABLE_NAME = "MESSAGES";
    private static final String COLUMN_IP = "IP";
    private static final String COLUMN_ROOM = "ROOM";
    private static final String COLUMN_TIME = "TIME";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_MESSAGE = "MESSAGE";
    private static final int CURRENT_VERSION = 1;

    MessageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
        deleteOldMessages();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_TIME + " INT, " +
                COLUMN_ID + " INT, " +
                COLUMN_IP + " TEXT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_MESSAGE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    ArrayList<DataTypes.ChatMessage> getMessages(String ip, String room) {
        deleteOldMessages();
        ArrayList<DataTypes.ChatMessage> messages = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(COLUMN_IP)).equals(ip) &&
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)).equals(room)) {
                    messages.add(new DataTypes().new ChatMessage(
                            cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                            cursor.getInt(cursor.getColumnIndex(COLUMN_TIME))));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(messages);
        return messages;
    }

    void addMessage(String ip, String room, int id, String name, String message) {
        deleteOldMessages();
        deleteMessage(ip, room, id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TIME, (int) (System.currentTimeMillis() / 1000));
        contentValues.put(COLUMN_IP, ip);
        contentValues.put(COLUMN_ROOM, room);
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_MESSAGE, message);
        getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    }

    void deleteMessage(String ip, String room, int id) {
        deleteOldMessages();
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=? AND " + COLUMN_ID + "=?",
                new String[]{ip, room, String.valueOf(id)});
    }

    private void deleteOldMessages() {
        getWritableDatabase().delete(TABLE_NAME, COLUMN_TIME + "<?",
                new String[]{String.valueOf((int) System.currentTimeMillis() / 1000 - 604800)});
    }
}
