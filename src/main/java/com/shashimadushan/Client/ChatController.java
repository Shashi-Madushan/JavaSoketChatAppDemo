package com.shashimadushan.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ChatController {
    @FXML
    private TextField usernameField;

    @FXML
    private Button connectButton;

    @FXML
    private ListView<Object> chatMessages;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;



    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    @FXML
    public void initialize() {
        messageField.setDisable(true);
        sendButton.setDisable(true);

        connectButton.setOnAction(event -> connectToServer());
        sendButton.setOnAction(event -> sendMessage());

    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Error", "Username cannot be empty!", Alert.AlertType.ERROR);
            return;
        }

        try {
            socket = new Socket("127.0.0.1", 5000);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            outputStream.writeUTF(username);
            outputStream.flush();

            messageField.setDisable(false);
            sendButton.setDisable(false);
            connectButton.setDisable(true);
            usernameField.setDisable(true);

            new Thread(this::listenForMessages).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to connect to server.", Alert.AlertType.ERROR);
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String prefix = inputStream.readUTF();

                if (prefix.equals("[IMAGE]")) {
                    int length = inputStream.readInt();
                    byte[] imageBytes = new byte[length];
                    inputStream.readFully(imageBytes);

                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);

                    Platform.runLater(() -> chatMessages.getItems().add(imageView));
                } else {
                    Platform.runLater(() -> chatMessages.getItems().add(new Label(prefix)));
                }
            }
        } catch (EOFException e) {
            System.out.println("Connection closed by the server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        try {
            outputStream.writeUTF(message);
            outputStream.flush();
            Platform.runLater(() -> chatMessages.getItems().add(new Label("You: " + message)));
            messageField.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to send message.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void sendImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());

                outputStream.writeUTF("[IMAGE]");
                outputStream.flush();
                outputStream.writeInt(imageBytes.length);
                outputStream.write(imageBytes);
                outputStream.flush();

                Platform.runLater(() -> chatMessages.getItems().add(
                        new Label("You sent an image: " + file.getName())
                ));
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Unable to send image due to I/O error.", Alert.AlertType.ERROR);
            }
        }
    }


    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void sendFile(ActionEvent actionEvent) {
    }

    @FXML
    public void disConnect() {
        try {
            outputStream.writeUTF("exit");
            outputStream.flush();
            messageField.setDisable(true);
            sendButton.setDisable(true);

            connectButton.setDisable(false);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
