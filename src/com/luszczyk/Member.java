package com.luszczyk;

import java.io.PrintWriter;
import java.net.Socket;

class Member {
    private PrintWriter writer;
    private Socket clientSocket;

    void setWriter(PrintWriter write) {
        this.writer = write;
    }

    PrintWriter getWriter(){
        return this.writer;
    }

    void setSocket(Socket socket){
        this.clientSocket = socket;
    }

    Socket getSocket(){
        return this.clientSocket;
    }
}
