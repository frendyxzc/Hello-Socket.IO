package me.frendy.socketio.presenters;

import android.app.Activity;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.frendy.socketio.presenters.interfaces.SocketIOView;

/**
 * Created by iiMedia on 2017/4/28.
 */

public class SocketIOHelper {
    private Activity mActivity;
    private SocketIOView mView;

    private Socket mSocket;
    private String mAndroidID;

    public SocketIOHelper(Activity activity, SocketIOView view) {
        mActivity = activity;
        mView = view;
        mAndroidID = Settings.System.getString(
                mActivity.getContentResolver(), Settings.System.ANDROID_ID);

        try {
            //1.初始化socket.io，设置链接
            mSocket = IO.socket("http://10.1.1.105:5000");

            //2.建立socket.io服务器的连接
            mSocket.connect();

            //3.对服务器发送一个"my event"事件，其值为"hi, i am android!"：
            try {
                JSONObject data = new JSONObject();
                data.put("data", "hi, i am android!");
                mSocket.emit("my event", data);

                mView.showMessage(data.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //4.注册广播接收监听器
            mSocket.on("app broadcast", onAppBroadcast);

            //5.向服务器注册点对点通信监听
            try {
                JSONObject data = new JSONObject();
                data.put("data", mAndroidID);
                mSocket.emit("register", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.on(mAndroidID, onP2PMessage);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String event, String message) {
        if(mSocket != null) {
            try {
                JSONObject data = new JSONObject();
                data.put("data", message);
                mSocket.emit(event, data);
                mView.showMessage(data.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Emitter.Listener onAppBroadcast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        mView.showMessage(data.getString("data"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onP2PMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        mView.showMessage(data.getString("data"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public void onDestroy() {
        mSocket.disconnect();
        mSocket.off("app broadcast", onAppBroadcast);
        mSocket.off(mAndroidID, onP2PMessage);

        mActivity = null;
        mView = null;
    }
}
