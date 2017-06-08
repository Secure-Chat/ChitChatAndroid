package io.github.gregoryconrad.chitchat.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.data.DataStore;
import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.ui.MainActivity;

public class MessageRecAdapter extends RecyclerView.Adapter<MessageRecAdapter.MessageHolder> {
    private MainActivity activity = null;
    private String room = null;
    private ArrayList<DataTypes.ChatMessage> messages = new ArrayList<>();

    public MessageRecAdapter(MainActivity activity) {
        this.activity = activity;
        this.room = this.activity.getCurrRoom();
    }

    @Override
    public MessageRecAdapter.MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageRecAdapter.MessageHolder holder, int position) {
        holder.username.setText(messages.get(position).getName());
        holder.message.setText(messages.get(position).getMessage());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO popup options and return true;
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void update() {
        if (!this.room.equals(activity.getCurrRoom())) {
            this.room = activity.getCurrRoom();
            this.messages = new ArrayList<>();
        }
        ArrayList<DataTypes.ChatMessage> newMsgs =
                DataStore.getMessages(activity, activity.getCurrIP(), room);
        for (int i = this.messages.size(); i < newMsgs.size(); ++i) {
            this.messages.add(newMsgs.get(i));
        }
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        private TextView username = null;
        private TextView message = null;

        MessageHolder(View itemView) {
            super(itemView);
            this.username = (TextView) itemView.findViewById(R.id.username);
            this.message = (TextView) itemView.findViewById(R.id.message);
        }
    }
}