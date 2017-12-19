package com.pk.server;

import com.pk.common.Constants;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        initServer();
        System.out.println("Server IP : " + serverSocket.getLocalSocketAddress());
        while (true) {
            Thread t = new SensorDataRecorder(serverSocket.accept());
            t.start();
        }
    }

    static void initServer() throws IOException {
        serverSocket = new ServerSocket(Constants.PORT_NUMBER);
    }
}
