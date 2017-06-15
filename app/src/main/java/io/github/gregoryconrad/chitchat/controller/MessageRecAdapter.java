package io.github.gregoryconrad.chitchat.controller;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        holder.username.setText(activity.getCurrRoom()
                .getMessages(activity).get(position).getName());
        holder.message.setText(activity.getCurrRoom()
                .getMessages(activity).get(position).getMessage());
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
        private TextView username = null;
        private TextView message = null;

        MessageHolder(View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.username);
            this.message = itemView.findViewById(R.id.message);
        }
    }
}