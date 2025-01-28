package com.shashimadushan.server;

import java.io.*;
import java.net.Socket;

import static com.shashimadushan.server.Server.clients;

public class ClinetsMannager implements Runnable {
    private final Socket socket;
    private String username;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClinetsMannager(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            username = inputStream.readUTF();
            broadcast(username + " has joined the chat!", this);

            while (true) {
                try {
                    String message = inputStream.readUTF();

                    if (message.startsWith("[IMAGE]")) {
                        int length = inputStream.readInt();
                        byte[] imageBytes = new byte[length];
                        inputStream.readFully(imageBytes);
                        broadcast(username + " has sen a photo :", this);
                        broadcastImage(imageBytes, this);
                    } else if (message.equals("exit")) {
                        disconnect();
                    } else {
                        broadcast(username + ": " + message, this);
                    }
                } catch (UTFDataFormatException e) {
                    System.out.println("Received malformed UTF data.");
                } catch (IOException e) {
                    System.out.println(username + " has disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }




    // Broadcast text message to all clients
    private void broadcast(String message, ClinetsMannager sender) {
        for (ClinetsMannager client : clients) {
            try {
                if (client != sender) {
                    client.outputStream.writeUTF(message);
                    client.outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastImage(byte[] imageBytes, ClinetsMannager sender) {
        for (ClinetsMannager client : clients) {
            try {
                if (client != sender) {
                    client.outputStream.writeUTF("[IMAGE]");
                    client.outputStream.writeInt(imageBytes.length);
                    client.outputStream.write(imageBytes);
                    client.outputStream.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeClient(this);
        broadcast(username + " has left the chat.", this);
    }

    private void removeClient(ClinetsMannager client) {
        clients.remove(client);
    }
}
