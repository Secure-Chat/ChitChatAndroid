package io.github.gregoryconrad.chitchat.controller;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.data.JSEncryption;
import io.github.gregoryconrad.chitchat.ui.MainActivity;

/**
 * An adapter that feeds the RecyclerView for the messages
 */
public class MessageRecAdapter extends RecyclerView.Adapter<MessageRecAdapter.MessageHolder> {
    private MainActivity activity = null;
    private DataTypes.ChatRoom room = null;
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

    /**
     * Updates the displayed messages
     */
    public void update() {
        if (!activity.getCurrRoom().equals(this.room)) {
            this.room = activity.getCurrRoom();
            this.messages = new ArrayList<>();
        }
        update(this.room.getMessages(activity), messages.size());
    }

    private void update(final ArrayList<DataTypes.ChatMessage> encryptedMessages, final int index) {
        if (index >= 0 && index < encryptedMessages.size()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSEncryption.decrypt(activity, encryptedMessages.get(index).getMessage(),
                            room.getPassword(),
                            new JSEncryption.EncryptCallback() {
                                @Override
                                public void onResult(String txt) {
                                    messages.add(new DataTypes().new ChatMessage(
                                            encryptedMessages.get(index).getName(), txt,
                                            encryptedMessages.get(index).getTimestamp()));
                                    notifyDataSetChanged();
                                    update(encryptedMessages, index + 1);
                                }

                                @Override
                                public void onError(String error) {
                                    Log.i("MessageRecAdapter",
                                            "Failed to decrypt a message: " + error);
                                    update(encryptedMessages, index + 1);
                                }
                            }
                    );
                }
            });
        }
    }


    class MessageHolder extends RecyclerView.ViewHolder {
        private TextView username = null;
        private TextView message = null;

        MessageHolder(View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.username);
            this.message = itemView.findViewById(R.id.message);
        }
    }
}