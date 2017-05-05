package me.frendy.socketio.presenters;

import android.app.Activity;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.frendy.socketio.R;
import me.frendy.socketio.presenters.interfaces.ChatView;

/**
 * Created by iiMedia on 2017/5/5.
 */

public class ChatHelper {
    private static final String TAG = "ChatHelper";

    private Activity mActivity;
    private ChatView mView;

    private Socket mSocket;
    private Boolean isConnected = false;

    private String mUsername;
    private String mAndroidID;

    public ChatHelper(Activity activity, ChatView view) {
        mActivity = activity;
        mView = view;
        mAndroidID = Settings.System.getString(
                mActivity.getContentResolver(), Settings.System.ANDROID_ID);

        try {
            //1.初始化socket.io，设置链接
            mSocket = IO.socket("http://10.1.1.105:5000");

            //2.建立socket.io服务器的连接
            mSocket.connect();

            //3.注册监听
            mSocket.on(Socket.EVENT_CONNECT,onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on("_message", onNewMessage);
            mSocket.on("_joined", onUserJoined);
            mSocket.on("_left", onUserLeft);

            addLog(mActivity.getResources().getString(R.string.message_welcome));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        mSocket.emit("left", "0");
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("_message", onNewMessage);
        mSocket.off("_joined", onUserJoined);
        mSocket.off("_left", onUserLeft);

        mActivity = null;
        mView = null;
    }


    public void attemptLogin(String username, String room) {
        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(mActivity, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mUsername = username;

        // perform the user login attempt.
        try {
            JSONObject data = new JSONObject();
            data.put("name", username);
            data.put("room", room);
            mSocket.emit("joined", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        String message = mView.attemptSend();
        if(message == null) return;

        addMessage(mUsername, message);

        // perform the sending message attempt.
        try {
            JSONObject data = new JSONObject();
            data.put("message", message);
            mSocket.emit("message", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void leave() {
        mUsername = null;
        mSocket.emit("left", "1");
        mSocket.disconnect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        if(mUsername == null)
                            attemptLogin(mAndroidID, "100");
                        Toast.makeText(mActivity.getApplicationContext(),
                                R.string.connect, Toast.LENGTH_LONG).show();
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Toast.makeText(mActivity.getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity.getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("user_name");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("user_name");
                        numUsers = data.getInt("num_users");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addLog(mActivity.getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("user_name");
                        numUsers = data.getInt("num_users");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addLog(mActivity.getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private void addLog(String message) {
        mView.addLog(message);
    }

    private void addParticipantsLog(int numUsers) {
        addLog(mActivity.getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addMessage(String username, String message) {
        mView.addMessage(username, message);
    }
}
