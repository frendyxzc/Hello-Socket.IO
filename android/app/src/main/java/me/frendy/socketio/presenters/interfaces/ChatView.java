package me.frendy.socketio.presenters.interfaces;

/**
 * Created by iiMedia on 2017/5/5.
 */

public interface ChatView {
    String attemptSend();
    void addLog(String message);
    void addMessage(String username, String message);
}
