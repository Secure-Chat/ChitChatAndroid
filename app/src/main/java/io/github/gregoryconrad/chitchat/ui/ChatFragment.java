package io.github.gregoryconrad.chitchat.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.controller.MessageRecAdapter;

/**
 * A Fragment that is used for chatting
 */
public class ChatFragment extends Fragment {
    private MessageRecAdapter messageRecAdapter = null;
    private RecyclerView messages = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);

        this.messageRecAdapter = new MessageRecAdapter((MainActivity) getActivity());
        this.messages = view.findViewById(R.id.messages);
        this.messages.setAdapter(messageRecAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.messages.setLayoutManager(layoutManager);
        this.messages.addItemDecoration(new DividerItemDecoration(getContext(),
                layoutManager.getOrientation()));

        view.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendMessage(
                        ((EditText) view.findViewById(R.id.message_box)).getText().toString());
                ((EditText) view.findViewById(R.id.message_box)).setText("");
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        update();
        if (getActivity() != null && isVisibleToUser) ((MainActivity) getActivity())
                .setActionBarTitle(((MainActivity) getActivity()).getCurrRoom().getRoom());
    }

    void update() {
        if (this.messageRecAdapter != null) {
            this.messageRecAdapter.update();
            if (this.messages != null) { //fixme
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messageRecAdapter.getItemCount() > 0) {
                            messages.smoothScrollToPosition(messageRecAdapter.getItemCount() - 1);
                        }
                    }
                });
            }
        }
    }

    void addMessage(String name, String message) {
        this.messageRecAdapter.addMessage(name, message);
    }
}