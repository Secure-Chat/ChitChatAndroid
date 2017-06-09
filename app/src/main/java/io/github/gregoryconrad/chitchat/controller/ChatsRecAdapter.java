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

public class ChatsRecAdapter extends RecyclerView.Adapter<ChatsRecAdapter.ChatHolder> {
    private MainActivity activity = null;
    private boolean isUpdating = false;

    public ChatsRecAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public ChatsRecAdapter.ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatsRecAdapter.ChatHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ChatsRecAdapter.ChatHolder holder, int position) {
        ArrayList<DataTypes.ChatRoom> rooms =
                DataStore.getRoomsForIP(activity, activity.getCurrIP());
        holder.chatName.setText(rooms.get(position).getRoom());
        holder.ip.setText(rooms.get(position).getIP());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setCurrRoom(String.valueOf(holder.chatName.getText()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return (isUpdating) ? 0 : DataStore.getRoomsForIP(activity, activity.getCurrIP()).size();
    }

    public void update() {
        this.isUpdating = true;
        notifyDataSetChanged();
        this.isUpdating = false;
        notifyDataSetChanged();
    }

    class ChatHolder extends RecyclerView.ViewHolder {
        private TextView chatName = null;
        private TextView ip = null;

        ChatHolder(View itemView) {
            super(itemView);
            this.chatName = itemView.findViewById(R.id.chat_name);
            this.ip = itemView.findViewById(R.id.ip);
        }
    }
}
