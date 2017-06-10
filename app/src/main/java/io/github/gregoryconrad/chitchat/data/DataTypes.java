package io.github.gregoryconrad.chitchat.data;

import android.support.annotation.NonNull;

public class DataTypes {
    /**
     * A class that is used to parse JSON that meets the protocol
     */
    public class JSON {
        private String type = null;
        private String room = null;
        private String name = null;
        private String msg = null;
        private int id = -1;
        private int min = -1;
        private int max = -1;

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

        public int getId() {
            return this.id;
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

        public JSON setId(int id) {
            this.id = id;
            return this;
        }

        public JSON setMin(int min) {
            this.min = min;
            return this;
        }

        public JSON setMax(int max) {
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
    }

    /**
     * A class that represents a single message
     */
    public class ChatMessage implements Comparable<ChatMessage> {
        private String name, message;
        private int id;

        ChatMessage(String name, String message, int id) {
            this.name = name;
            this.message = message;
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public String getMessage() {
            return this.message;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public int compareTo(@NonNull ChatMessage o) {
            return this.id - o.id;
        }
    }
}
