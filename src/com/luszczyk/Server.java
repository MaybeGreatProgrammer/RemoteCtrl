package com.luszczyk;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

class Server {
    private SecretKey secretKey;
    void start() throws IOException, NoSuchAlgorithmException {

        Properties p = new Properties();
        final String defaultPassword = "hunter2";
        String password = defaultPassword;
        String filePath = System.getProperty("user.home") + "\\Appdata\\Roaming\\rc.pw";

        File file = new File(filePath);
        if(file.exists()){
            p.load(new FileReader(file));
            password = p.getProperty("password");
            if(password.length()==0) password = defaultPassword;
        } else {
            p.setProperty("password", defaultPassword);
            p.store(new FileWriter(file),"");
        }

        byte[] key = password.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        secretKey = new SecretKeySpec(key, "AES");

        ServerSocket serverSocket = new ServerSocket(24771);
        System.out.println("Server started");
        while(true){
            Socket clientSocket = serverSocket.accept();
            Member member = new Member();
            member.setSocket(clientSocket);
            member.setWriter(new PrintWriter(clientSocket.getOutputStream(), true));
            new Handler(member).start();
        }
    }

    private class Handler extends Thread{
        private Member member;
        private BufferedReader in;
        private String inputLine = "";

        Handler(Member member){
            this.member = member;
        }

        public void run(){
            try{
                System.out.println("Client connected");
                in = new BufferedReader(new InputStreamReader(member.getSocket().getInputStream()));
                while(true) {
                    inputLine = in.readLine();
                    try{
                        inputLine = MSGEncrypt.AESDecrypt(inputLine,secretKey);
                    } catch (Exception e){
                        System.out.println("Client password incorrect");
                        member.getSocket().close();
                        break;
                    }
                    new ProcessThread(inputLine,member.getWriter()).start();
                }
            } catch (Exception e){
                System.out.println("Client disconnected");
            }
        }
    }

    private class ProcessThread extends Thread{
        private String inputLine;
        private PrintWriter writer;

        ProcessThread(String inputLine, PrintWriter writer){
            this.inputLine = inputLine;
            this.writer = writer;
        }

        public void run(){
            try{
                if(inputLine.length()!=0){
                    //Needed for Windows commands
                    inputLine = "cmd /c " + inputLine;

                    Process pr = Runtime.getRuntime().exec(inputLine);
                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

                    String line;
                    while ((line = buf.readLine()) != null) {
                        writer.println(MSGEncrypt.AESEncrypt(line,secretKey));
                    }
                    while ((line = stdError.readLine()) != null) {
                        writer.println(MSGEncrypt.AESEncrypt(line,secretKey));
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
