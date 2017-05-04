package io.lucrib.feewilo;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Main extends Application {

  private boolean makeRead = true;

  private ServerSocket socketServer;

  private TextField portNumberTextField;
  private TextFlow textArea;

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Feewilo");

    Label portNumberLabel = new Label("Server Port");
    portNumberLabel.setTextAlignment(TextAlignment.CENTER);
    portNumberTextField = new TextField();
    portNumberLabel.setLabelFor(portNumberTextField);
    portNumberTextField.promptTextProperty().setValue("port number");

    Button startSocketServerButton = new Button("Start Socket Server");
    startSocketServerButton.setOnAction(e -> startSocketServerAction());

    Button stopSocketServerButton = new Button("Stop Socket Server");
    stopSocketServerButton.setOnAction(e -> stopSocketServerAction());

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button clearButton = new Button("Clear text");
    clearButton.setAlignment(Pos.CENTER_RIGHT);
    clearButton.setOnAction(e -> clearTextArea());

    HBox hbox = new HBox();
    hbox.getChildren().addAll(
        portNumberLabel,
        portNumberTextField,
        startSocketServerButton,
        stopSocketServerButton,
        spacer,
        clearButton);
    hbox.setPadding(new Insets(10, 10, 10, 10));
    hbox.setSpacing(7);
    hbox.setAlignment(Pos.CENTER_LEFT);

    textArea = new TextFlow();
    ScrollPane scroll = new ScrollPane();
    scroll.setContent(textArea);
    scroll.setFitToWidth(true);

    BorderPane borderPane = new BorderPane();
    borderPane.setTop(hbox);
    borderPane.setCenter(scroll);
    Scene scene = new Scene(borderPane, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void clearTextArea() {
    textArea.getChildren().clear();
  }

  private void startSocketServerAction() {
    if (portNumberTextField.getText().isEmpty()) {
      // Show an alert box or something
      addTextToArea("PLEASE INFORM THE PORT NUMBER\n");
      return;
    }
    int port;
    try {
      port = Integer.valueOf(portNumberTextField.getText());
    } catch (Exception e) {
      addTextToArea("Failed to convert port to number.\n");
      e.printStackTrace();
      return;
    }

    try {
      socketServer = new ServerSocket(port);
      // Starts a new String
      addTextToArea("Before Runnable\n");
      ServerSocketListener serverSocketListener = new ServerSocketListener();
      Thread serverThread = new Thread(serverSocketListener);
      makeRead = true;
      serverThread.start();
      addTextToArea("After Runnable\n");
    } catch (IOException e) {
      addTextToArea("Unable to process client request\n");
      e.printStackTrace();
    }
    addTextToArea("Waiting for clients to connect...\n");
  }

  private void stopSocketServerAction() {
    try {
      makeRead = false;
      socketServer.close();

      addTextToArea("Stopped listening on port " + String.valueOf("5060") + "\n");
    } catch (IOException e) {
      addTextToArea("Failed to close the socket server\n");
      e.printStackTrace();
    }
  }

  private class ServerSocketListener implements Runnable {

    @Override
    public void run() {
      try {
        Socket sock = socketServer.accept();
        addTextToArea("New connection from " + sock.getInetAddress().getHostAddress() + "\n");
        InputStream sock_in = sock.getInputStream();
        Scanner scanner = new Scanner(sock_in);
        while (makeRead) {
          if (scanner.hasNext()) {
            String data = scanner.nextLine();
            // code that updates UI
            addTextToArea(data);
          }
        }
        sock.close();
      } catch (SocketException e) {
        addTextToArea("Socket Closed\n");
      } catch (IOException e) {
        addTextToArea("Failed to accept connections\n");
        e.printStackTrace();
      } finally {
        try {
          socketServer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void addTextToArea(String data) {
    Platform.runLater(() -> {
      Text t = new Text(data + "\n");
      t.setFont(Font.font("Helvetica", 18));
      textArea.getChildren().add(t);
    });

  }

  public static void main(String[] args) {
    launch(args);
  }
}
