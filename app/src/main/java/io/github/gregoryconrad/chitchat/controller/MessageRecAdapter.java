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
                this.colorIndicator.setBackgroundColor(stringToColor(name.getText().toString()));
            }
        }

        /**
         * Converts a given String to a somewhat unique color
         * In essence, this method hashes a String to a color
         * @param str the String to create a color for
         * @return the int representation of the color (with a leading 0xFF)
         */
        private int stringToColor(String str) {
            byte[] nameDigest;
            try {
                nameDigest = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
                int currColor = 0xFF, color = 0; //currColor represents either r, g, or b
                for (int i = 0; i < nameDigest.length; ++i) {
                    if (i % (nameDigest.length / 3) == 0) {
                        color <<= 8;
                        color += currColor & 0xFF;
                        currColor = 0;
                    }
                    currColor += nameDigest[i];
                }
                return color;
            } catch(Exception e) {
                return 0xFF000000; //return black on fail, which should never happen
            }
        }
    }
}