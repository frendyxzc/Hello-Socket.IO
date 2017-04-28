package me.frendy.socketio.presenters;

import android.content.Context;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import me.frendy.socketio.presenters.interfaces.SocketIOView;

/**
 * Created by iiMedia on 2017/4/28.
 */

public class SocketIOHelper {
    private Context mContext;
    private SocketIOView mView;

    private Socket mSocket;

    public SocketIOHelper(Context context, SocketIOView view) {
        mContext = context;
        mView = view;

        try {
            //1.初始化socket.io，设置链接
            mSocket = IO.socket("http://10.1.1.105:5000");

            //2.建立socket.io服务器的连接
            mSocket.connect();

            //3.对服务器发送一个"my event"事件，其值为"hi, i am android!"：
            mSocket.emit("my event", "hi, i am android!");

            mView.showMessage("hi, i am android!");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String event, String message) {
        if(mSocket != null) {
            mSocket.emit(event, message);
            mView.showMessage(message);
        }
    }

    public void onDestroy() {
        mSocket.disconnect();

        mContext = null;
        mView = null;
    }
}