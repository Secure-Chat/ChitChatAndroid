package io.github.gregoryconrad.chitchat.controller;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.MessageDigest;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.ui.MainActivity;

/**
 * An adapter that feeds the RecyclerView for the messages
 */
public class MessageRecAdapter extends RecyclerView.Adapter<MessageRecAdapter.MessageHolder> {
    private MainActivity activity = null;

    public MessageRecAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public MessageRecAdapter.MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MessageRecAdapter.MessageHolder holder, int position) {
        holder.name.setText(activity.getCurrRoom()
                .getMessages(activity).get(position).getName());
        holder.message.setText(activity.getCurrRoom()
                .getMessages(activity).get(position).getMessage());
        holder.setColor();
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(activity)
                        .setTitle("Delete message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activity.getCurrRoom().removeMessage(activity,
                                        activity.getCurrRoom().getMessages(activity)
                                                .get(holder.getAdapterPosition()).getTimestamp());
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel", null).create().show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (activity.getCurrRoom() == null) return 0;
        return activity.getCurrRoom().getMessages(activity).size();
    }

    /**
     * Updates the displayed messages
     */
    public void update() {
        if (getItemCount() > 0) notifyItemRangeChanged(0, getItemCount());
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        private View colorIndicator = null;
        private TextView name = null;
        private TextView message = null;

        MessageHolder(View itemView) {
            super(itemView);
            this.colorIndicator = itemView.findViewById(R.id.color_indicator);
            this.name = itemView.findViewById(R.id.name);
            this.message = itemView.findViewById(R.id.message);
        }

        private void setColor() {
            if (name != null) {
                try {
                    byte[] nameDigest = MessageDigest.getInstance("MD5")
                            .digest(name.getText().toString().getBytes("UTF-8"));
                    int r = 0, g = 0, b = 0, color = 0xFF;
                    int third = nameDigest.length / 3;
                    for (int i = 0; i < nameDigest.length + 1; ++i) {
                        if (i % third == 0) {
                            color <<= 8;
                            if (i == third) color += r;
                            else if (i == third * 2) color += g;
                            else if (i == third * 3) {
                                color += b;
                                break;
                            }
                        }
                        if (i < third) r += nameDigest[i];
                        else if (i < 2 * third) g += nameDigest[i];
                        else b += nameDigest[i];
                    }
                    this.colorIndicator.setBackgroundColor(color);
                } catch (Exception e) {
                    Log.e("MessageRecAdapter", "Failed to change a message color", e);
                }
            }
        }
    }
}