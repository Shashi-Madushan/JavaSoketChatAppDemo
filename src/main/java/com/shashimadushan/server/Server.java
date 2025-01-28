package com.shashimadushan.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int  PORT = 5000;
    public static List<ClinetsMannager> clients = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("Server started at port: " + PORT);
      try( ServerSocket serverSocket = new ServerSocket(5000)){
          System.out.println("Waiting for connection...");
          while(true){
              Socket socket = serverSocket.accept();
              System.out.println("New client connected");
              ClinetsMannager clinetsMannager = new ClinetsMannager(socket);
                clients.add(clinetsMannager);
                new Thread(clinetsMannager).start();
          }

      }catch(IOException e){
          e.printStackTrace();

      }

    }

}