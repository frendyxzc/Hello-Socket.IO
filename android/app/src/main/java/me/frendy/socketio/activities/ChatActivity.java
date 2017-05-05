package me.frendy.socketio.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import me.frendy.socketio.fragments.FragmentChat;

/**
 * Created by iiMedia on 2017/5/5.
 */

public class ChatActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultFragment(FragmentChat.getInstance());
    }
}
