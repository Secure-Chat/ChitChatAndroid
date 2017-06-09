package io.github.gregoryconrad.chitchat.controller;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

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
        Log.e("ChatAdapter", String.valueOf(holder.colorIndicator.getWidth()));
        holder.setColor(0xFF00FF00);
        holder.chatName.setText(rooms.get(position).getRoom());
        holder.ip.setText(rooms.get(position).getIP());
        holder.colorIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder.with(activity)
                        .setTitle("Pick the chat color")
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(12)
                        .initialColor(holder.currentColor)
                        .setPositiveButton("Change", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                //todo write selectedColor to database
                                holder.setColor(selectedColor);
                            }
                        }).setNegativeButton("Cancel", null)
                        .noSliders().build().show();
            }
        });
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
        private ImageView colorIndicator = null;
        private TextView chatName = null;
        private TextView ip = null;
        private int currentColor = 0xFF111111;

        ChatHolder(View itemView) {
            super(itemView);
            this.colorIndicator = itemView.findViewById(R.id.color_indicator);
            this.chatName = itemView.findViewById(R.id.chat_name);
            this.ip = itemView.findViewById(R.id.ip);
            setColor(currentColor);
        }

        private void setColor(int color) {
            Bitmap bitmap = Bitmap.createBitmap(
                    activity.getResources().getDimensionPixelSize(R.dimen.rec_view),
                    activity.getResources().getDimensionPixelSize(R.dimen.rec_view),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            this.colorIndicator.setImageBitmap(bitmap);
        }
    }
}
