package com.xingyeda.lowermachine.socket;

import com.xingyeda.lowermachine.base.ConnectPath;

import java.io.IOException;
import java.net.Socket;

public class SocketUtils {

    private static Socket mSocket;

    private SocketUtils() {

    }

    public static Socket getInstance() {
        if (mSocket == null) {
            synchronized (SocketUtils.class) {
                if (mSocket == null) {
                    try {
                        mSocket = new Socket(ConnectPath.SOCKET_HOST, ConnectPath.SOCKET_PORT);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }
        }
        return mSocket;
    }

}
