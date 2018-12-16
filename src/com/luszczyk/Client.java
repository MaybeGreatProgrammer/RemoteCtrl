package com.luszczyk;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

class Client {
    private BufferedReader in;
    private SecretKey secretKey;
    private Socket clientSocket;

    void start() throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP: ");
        String ip = scanner.nextLine();

        try{
            clientSocket = new Socket(ip, 24771);
        } catch(Exception e){
            System.out.println("Connection refused");
            System.exit(2);
        }
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.print("Enter server password: ");
        String password = scanner.nextLine();

        byte[] key = password.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        secretKey = new SecretKeySpec(key, "AES");

        new Receiver().start();
        String userInput;
        out.println(MSGEncrypt.AESEncrypt("echo Welcome",secretKey));
        while(true) {
            userInput = scanner.nextLine();
            out.println(MSGEncrypt.AESEncrypt(userInput,secretKey));
        }
    }

    private class Receiver extends Thread{

        Receiver(){}

        public void run(){
            String inputLine;
            try{
                while (true){
                    inputLine = in.readLine();
                    try{
                        inputLine = MSGEncrypt.AESDecrypt(inputLine,secretKey);
                    } catch(Exception e){
                        System.out.println("Password incorrect");
                        clientSocket.close();
                        System.exit(3);
                    }
                    System.out.println(inputLine);
                }
            } catch (Exception e){
                System.out.println("Server disconnected");
                System.exit(2);
            }
        }
    }
}
