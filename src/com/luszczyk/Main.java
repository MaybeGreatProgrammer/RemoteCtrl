package com.luszczyk;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
	    if(Arrays.toString(args).contains("client")){
            Client client = new Client();
            try {
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Server server = new Server();
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
