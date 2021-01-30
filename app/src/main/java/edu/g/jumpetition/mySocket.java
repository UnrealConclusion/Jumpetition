package edu.g.jumpetition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class mySocket {
    private static Socket sock;
    private static InputStream ip;
    private static OutputStream op;


    public static synchronized Socket getSocket(){
        return sock;
    }

    public static synchronized void setSocket(Socket socket) throws IOException {
        mySocket.sock = socket;
    }

    public static synchronized InputStream getInputStream(){
        return ip;
    }

    public static synchronized void setStream() throws IOException {
        mySocket.ip = mySocket.sock.getInputStream();
        mySocket.op = mySocket.sock.getOutputStream();
    }

    public static synchronized OutputStream getOutputStream(){
        return op;
    }

}
