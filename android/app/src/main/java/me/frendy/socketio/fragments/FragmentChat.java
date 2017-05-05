package me.frendy.socketio.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.frendy.socketio.R;
import me.frendy.socketio.adapters.MessageAdapter;
import me.frendy.socketio.entities.Message;
import me.frendy.socketio.presenters.ChatHelper;
import me.frendy.socketio.presenters.interfaces.ChatView;


/**
 * A chat fragment containing messages view and input form.
 */
public class FragmentChat extends Fragment implements ChatView, View.OnClickListener {
    private ChatHelper mChatHelper;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;

    private RecyclerView.Adapter mAdapter;
    private List<Message> mMessages = new ArrayList<>();

    public static FragmentChat getInstance() {
        FragmentChat fragment = new FragmentChat();
        return fragment;
    }

    public FragmentChat() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapter(context, mMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onDestroy() {
        mChatHelper.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    mChatHelper.attemptSend();
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.send_button).setOnClickListener(this);

        mChatHelper = new ChatHelper(getActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            getActivity().finish();
            return;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_leave) {
            mChatHelper.leave();
            mInputMessageView.setText("");
            mInputMessageView.setEnabled(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return null;
        }
        mInputMessageView.setText("");
        return message;
    }

    @Override
    public void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    @Override
    public void addMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.send_button) {
            mChatHelper.attemptSend();
        }
    }
}

