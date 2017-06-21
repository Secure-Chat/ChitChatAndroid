package io.github.gregoryconrad.chitchat.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.gregoryconrad.chitchat.R;
import io.github.gregoryconrad.chitchat.controller.ChatsRecAdapter;

/**
 * A Fragment that is used to display the chat rooms
 */
public class MainFragment extends Fragment {
    private ChatsRecAdapter chatsRecAdapter = null;
    private RecyclerView chats = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        this.chatsRecAdapter = new ChatsRecAdapter((MainActivity) getActivity());
        this.chats = view.findViewById(R.id.chats);
        this.chats.setAdapter(chatsRecAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.chats.setLayoutManager(layoutManager);
        this.chats.addItemDecoration(new DividerItemDecoration(getContext(),
                layoutManager.getOrientation()));
        update();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        update();
        if (getActivity() != null && isVisibleToUser) {
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name));
            ((MainActivity) getActivity()).chatSocket.close();
            ((MainActivity) getActivity()).chatSocket = null;
            ((MainActivity) getActivity()).setCurrRoom(null);
        }
    }

    public void update() {
        if (this.chatsRecAdapter != null) {
            chatsRecAdapter.update();
            this.chats.scrollToPosition(chatsRecAdapter.getItemCount() - 1);
        }
    }
}
