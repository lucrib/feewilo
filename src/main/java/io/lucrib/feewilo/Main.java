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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Main extends Application {

  private int port;
  private boolean makeRead = true;

  private ServerSocket socketServer;
  Thread serverThread;

  private TextField portNumberTextField;

  WebView webView = new WebView();
  WebEngine webEngine = webView.getEngine();

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

    webEngine.loadContent("<div id='content'></div>");

    BorderPane borderPane = new BorderPane();
    borderPane.setTop(hbox);
    borderPane.setCenter(webView);
    Scene scene = new Scene(borderPane, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void clearTextArea() {
    webEngine.loadContent("<div id='content'></div>");
  }

  private void startSocketServerAction() {
    if (portNumberTextField.getText().isEmpty()) {
      // Show an alert box or something
//      txtArea.appendText("PLEASE INFORM THE PORT NUMBER\n");
      return;
    }
    try {
      port = Integer.valueOf(portNumberTextField.getText());
    } catch (Exception e) {
//      txtArea.appendText("Failed to convert port to number.\n");
      e.printStackTrace();
      return;
    }

    try {
      socketServer = new ServerSocket(port);
      // Starts a new String
//      txtArea.appendText("Before Runnable\n");
      ServerSocketListener serverSocketListener = new ServerSocketListener();
      serverThread = new Thread(serverSocketListener);
      makeRead = true;
      serverThread.start();
//      txtArea.appendText("After Runnable\n");
    } catch (IOException e) {
//      txtArea.appendText("Unable to process client request\n");
      e.printStackTrace();
    }
//    txtArea.appendText("Waiting for clients to connect...\n");
  }

  private void stopSocketServerAction() {
    try {
      makeRead = false;
      socketServer.close();

//      txtArea.appendText("Stopped listening on port " + String.valueOf(this.port) + "\n");
    } catch (IOException e) {
//      txtArea.appendText("Failed to close the socket server\n");
      e.printStackTrace();
    }
  }

  private void updateWebEngine(String data) {
    JSObject jsobj = (JSObject) this.webEngine.executeScript("document.getElementById('content')");
    jsobj.eval("document.getElementById('content').innerHTML+=\"<span style='background-color: red'>" + data + "</span><br/>\"");
//    jsobj.call("append", "<b>" + data + "</b>");
  }

  private class ServerSocketListener implements Runnable {

    @Override
    public void run() {
      try {
        Socket sock = socketServer.accept();
//        txtArea.appendText("New connection from " + sock.getInetAddress().getHostAddress() + "\n");
        InputStream sock_in = sock.getInputStream();
        Scanner scanner = new Scanner(sock_in);
        while (makeRead) {
          if (scanner.hasNext()) {
            String data = scanner.nextLine();
//            System.out.println(data);
//            txtArea.appendText(data + '\n');
            Platform.runLater(() -> {
              // code that updates UI
              updateWebEngine(data);
            });
          }
        }
        sock.close();
      } catch (SocketException e) {
//        txtArea.appendText("Socket Closed\n");
      } catch (IOException e) {
//        txtArea.appendText("Failed to accept connections\n");
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

  public static void main(String[] args) {
    launch(args);
  }
}
