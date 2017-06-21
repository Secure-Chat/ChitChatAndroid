package io.github.gregoryconrad.chitchat.controller;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.data.DataTypes;
import io.github.gregoryconrad.chitchat.data.RoomsDBHelper;
import io.github.gregoryconrad.chitchat.ui.MainActivity;

/**
 * An adapter that feeds the RecyclerView for the chat rooms
 */
public class ChatsRecAdapter extends RecyclerView.Adapter<ChatsRecAdapter.ChatHolder> {
    private MainActivity activity = null;
    private boolean isUpdating = false; //forces recreation of all items to update

    public ChatsRecAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public ChatsRecAdapter.ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatsRecAdapter.ChatHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ChatsRecAdapter.ChatHolder holder, int position) {
        ArrayList<DataTypes.ChatRoom> rooms = RoomsDBHelper.getRooms(activity);
        holder.room.setText(rooms.get(position).getRoom());
        holder.ip.setText(rooms.get(position).getIP());
        holder.colorIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder.with(activity)
                        .setTitle("Pick the chat color")
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(12)
                        .initialColor(RoomsDBHelper.getRoom(activity,
                                String.valueOf(holder.ip.getText()),
                                String.valueOf(holder.room.getText())).getColor())
                        .setPositiveButton("Change", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor,
                                                Integer[] allColors) {
                                RoomsDBHelper.updateRoomColor(activity,
                                        RoomsDBHelper.getRoom(activity,
                                                String.valueOf(holder.ip.getText()),
                                                String.valueOf(holder.room.getText())),
                                        selectedColor);
                                holder.setColor();
                            }
                        }).setNegativeButton(R.string.button_cancel, null).noSliders().build().show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setCurrRoom(RoomsDBHelper.getRoom(activity,
                        String.valueOf(holder.ip.getText()),
                        String.valueOf(holder.room.getText())));
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(activity)
                        .setTitle("Delete room")
                        .setMessage("Are you sure you want to delete this room?")
                        .setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                RoomsDBHelper.removeRoom(activity, RoomsDBHelper.getRoom(activity,
                                        String.valueOf(holder.ip.getText()),
                                        String.valueOf(holder.room.getText())));
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.button_cancel, null).create().show();
                return true;
            }
        });
        holder.setColor();
    }

    @Override
    public int getItemCount() {
        return (isUpdating) ? 0 : RoomsDBHelper.getRooms(activity).size();
    }

    /**
     * Updates the displayed chat room list
     */
    public void update() {
        this.isUpdating = true;
        notifyDataSetChanged();
        this.isUpdating = false;
        notifyDataSetChanged();
    }

    class ChatHolder extends RecyclerView.ViewHolder {
        private ImageView colorIndicator = null;
        private TextView room = null;
        private TextView ip = null;

        ChatHolder(View itemView) {
            super(itemView);
            this.colorIndicator = itemView.findViewById(R.id.color_indicator);
            this.room = itemView.findViewById(R.id.chat_name);
            this.ip = itemView.findViewById(R.id.ip);
        }

        private void setColor() {
            Bitmap bitmap = Bitmap.createBitmap(
                    activity.getResources().getDimensionPixelSize(R.dimen.circle_diameter),
                    activity.getResources().getDimensionPixelSize(R.dimen.circle_diameter),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(RoomsDBHelper.getRoom(activity,
                    String.valueOf(ip.getText()), String.valueOf(room.getText())).getColor());
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            this.colorIndicator.setImageBitmap(bitmap);
        }
    }
}
