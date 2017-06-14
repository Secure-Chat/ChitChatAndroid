package io.github.gregoryconrad.chitchat.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;

public class DataTypes {
    /**
     * A class that is used to parse JSON that meets the protocol
     */
    public class JSON {
        private String type = null;
        private String room = null;
        private String name = null;
        private String msg = null;
        private String timestamp = null;
        private String min = null;
        private String max = null;

        public JSON(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public String getRoom() {
            return this.room;
        }

        public String getName() {
            return this.name;
        }

        public String getMsg() {
            return this.msg;
        }

        public String getTimestamp() {
            return this.timestamp;
        }

        public JSON setRoom(String room) {
            this.room = room;
            return this;
        }

        public JSON setName(String name) {
            this.name = name;
            return this;
        }

        public JSON setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public JSON setMin(String min) {
            this.min = min;
            return this;
        }

        public JSON setMax(String max) {
            this.max = max;
            return this;
        }
    }

    /**
     * A class that represents a chat room
     */
    public class ChatRoom {
        private String ip, room, nickname, password;
        private int color;

        ChatRoom(String ip, String room, String nickname, String password, int color) {
            this.ip = ip;
            this.room = room;
            this.nickname = nickname;
            this.password = password;
            this.color = color;
        }

        public String getIP() {
            return this.ip;
        }

        public String getRoom() {
            return this.room;
        }

        public String getNickname() {
            return this.nickname;
        }

        public String getPassword() {
            return this.password;
        }

        public int getColor() {
            return this.color;
        }

        public ArrayList<ChatMessage> getMessages(Context context) {
            MessageDBHelper messageDB = new MessageDBHelper(context);
            ArrayList<DataTypes.ChatMessage> returnVal = messageDB.getMessages(this);
            messageDB.close();
            return returnVal;
        }

        public void addMessage(Context context, String time, String name, String message) {
            MessageDBHelper messageDB = new MessageDBHelper(context);
            messageDB.addMessage(this, time, name, message);
            messageDB.close();
        }

        public void removeMessage(Context context, BigInteger time) {
            MessageDBHelper messageDB = new MessageDBHelper(context);
            messageDB.deleteMessage(this, time.toString());
            messageDB.close();
        }
    }

    /**
     * A class that represents a single message
     */
    public class ChatMessage implements Comparable<ChatMessage> {
        private String name, message;
        private BigInteger timestamp;

        public ChatMessage(String name, String message, BigInteger timestamp) {
            this.name = name;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getName() {
            return this.name;
        }

        public String getMessage() {
            return this.message;
        }

        public BigInteger getTimestamp() {
            return this.timestamp;
        }

        @Override
        public int compareTo(@NonNull ChatMessage o) {
            return this.timestamp.subtract(o.timestamp).intValue();
        }
    }
}
