package io.github.gregoryconrad.chitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SQLite Database that holds all of the messages
 */
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

    /**
     * @param room the room to get messages from
     * @return all of the messages stored
     */
    ArrayList<DataTypes.ChatMessage> getMessages(DataTypes.ChatRoom room) {
        deleteOldMessages();
        ArrayList<DataTypes.ChatMessage> messages = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(COLUMN_IP)).equals(room.getIP()) &&
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)).equals(room.getRoom()))
                    messages.add(new DataTypes().new ChatMessage(
                            cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)),
                            cursor.getInt(cursor.getColumnIndex(COLUMN_ID))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(messages);
        return messages;
    }

    /**
     * Adds a message to the database
     *
     * @param room    the room the message came from
     * @param id      the id of the message
     * @param name    the nickname of the user who sent the message
     * @param message the actual message
     */
    void addMessage(DataTypes.ChatRoom room, int id, String name, String message) {
        deleteMessage(room, id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TIME, (int) (System.currentTimeMillis() / 1000));
        contentValues.put(COLUMN_IP, room.getIP());
        contentValues.put(COLUMN_ROOM, room.getRoom());
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_MESSAGE, message);
        getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    }

    void deleteMessage(DataTypes.ChatRoom room, int id) {
        deleteOldMessages();
        getWritableDatabase().delete(TABLE_NAME,
                COLUMN_IP + "=? AND " + COLUMN_ROOM + "=? AND " + COLUMN_ID + "=?",
                new String[]{room.getIP(), room.getRoom(), String.valueOf(id)});
    }

    private void deleteOldMessages() {
        getWritableDatabase().delete(TABLE_NAME, COLUMN_TIME + "<?",
                new String[]{String.valueOf((int) System.currentTimeMillis() / 1000 - 604800)});
    }
}
