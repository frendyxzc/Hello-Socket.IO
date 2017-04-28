package me.frendy.socketio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import me.frendy.socketio.presenters.SocketIOHelper;
import me.frendy.socketio.presenters.interfaces.SocketIOView;

public class MainActivity extends AppCompatActivity implements SocketIOView, View.OnClickListener {
    private SocketIOHelper mSocketIOHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_send).setOnClickListener(this);

        mSocketIOHelper = new SocketIOHelper(this, this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_send) {
            mSocketIOHelper.sendMessage("my event", "i am clicked on android!");
        }
    }

    @Override
    public void showMessage(String message) {
        ((TextView) findViewById(R.id.tv_message)).setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocketIOHelper.onDestroy();
    }
}
