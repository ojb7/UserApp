import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class GUIController {
    @FXML
    private TextField portField;
    @FXML
    private TextField hostField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Pane WASD;
    @FXML
    private Rectangle wButtonIndicator;
    @FXML
    private Rectangle aButtonIndicator;
    @FXML
    private Rectangle sButtonIndicator;
    @FXML
    private Rectangle dButtonIndicator;
    @FXML
    private Button stopButton;
    @FXML
    private Button startButton;
    @FXML
    private Slider speedValue;
    @FXML
    private ListView<String> listOfUGVs;
    @FXML
    private Button selectUgvButton;
    @FXML
    private Button refrUgvListButton;

    private TCPClient tcpClient;
    private Thread pollUGVsThread;

    // UGV movement directions
    private boolean forward = false;
    private boolean backward = false;
    private boolean right = false;
    private boolean left = false;
    // UGV movement directions shadow
    private boolean forward0 = false;
    private boolean backward0 = false;
    private boolean right0 = false;
    private boolean left0 = false;

    // Speed value of UGV
    private int speed = 0;

    // Connection information
    private static final String HOST = "10.22.192.92";
    private static final String HOST_STASJ = "83.243.240.94";
    private static final String PORT = "42069";

    // UGV ID set from list of UGV's from server
    int UgvId = -1;


    private Thread userPollThread;


    ObservableList<String> obsListUgv;

    @FXML
    public void initialize() {
        System.out.println("Initialize GUI controller...");

        this.tcpClient = new TCPClient();
        this.hostField.setText(HOST_STASJ);
        this.portField.setText(PORT);
        this.disconnectButton.setDisable(true);
        this.hostField.setDisable(false);
        this.portField.setDisable(false);
    }

    @FXML
    void selectUgvFromList(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            String ugvIdSelected = listOfUGVs.getSelectionModel().getSelectedItem();
            int ugv = Integer.parseInt(ugvIdSelected);
            System.out.println("Sending: Selected UGV " + ugv);
            Command cmd = new Command("Selected UGV", ugv, null, null);
            this.tcpClient.sendCommand(cmd);
        }
    }


    @FXML
    void refreshUgvList(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            System.out.println("Trying to refresh UGV list..");
            startPollingUGV();
        }
    }


//    private void startPollingUGV() {
//        Command cmdToServer = new Command("UGVList", 0, null, null);
//        tcpClient.sendCommand(cmdToServer);
//        Command cmdFromServer = this.tcpClient.receiveCommand();
//        String cmd = cmdFromServer.getCommand();
//
//        if (cmd != null) {
//            if (cmd.equalsIgnoreCase("listUGV")) {
//                List<String> UgvId = cmdFromServer.getUGVs();
//                System.out.println("-----------------------List from server: " + UgvId);
//                obsListUgv = FXCollections.observableArrayList(UgvId);
//                listOfUGVs.setItems(obsListUgv);
//                listOfUGVs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//            }
//        }
//    }


    private void startPollingUGV() {
        if (this.userPollThread == null) {
            this.userPollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started UGV polling in Thread " + threadId);
                while (this.tcpClient.isConnectionActive()) {
                    Command cmd = new Command("UGVlist", 0, null, null);
                    this.tcpClient.sendCommand(cmd);
                    Command cmdFromServer = this.tcpClient.receiveCommand();
                    String cmdString = cmdFromServer.getCommand();

                    if (cmdString != null) {
                        if (cmdString.equalsIgnoreCase("listUGV")) {
                            List<String> UgvId = cmdFromServer.getUGVs();
                            System.out.println("-----------------------List from server: " + UgvId);
                            obsListUgv = FXCollections.observableArrayList(UgvId);
                            listOfUGVs.setItems(obsListUgv);
                            listOfUGVs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                System.out.println("User polling thread " + threadId + " exiting...");
                this.userPollThread = null;
            });
            this.userPollThread.start();
        }
    }


    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            String keyPressed = event.getText();
            if (keyPressed.equalsIgnoreCase("W")) this.forward = true;
            if (keyPressed.equalsIgnoreCase("A")) this.left = true;
            if (keyPressed.equalsIgnoreCase("S")) this.backward = true;
            if (keyPressed.equalsIgnoreCase("D")) this.right = true;
            System.out.println("Key pressed: " + keyPressed);
            sendKeyCommand(this.forward, this.left, this.backward, this.right);
            updateWasdInGui(this.forward, this.left, this.backward, this.right);
        }
    }

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            String keyReleased = event.getText();
            if (keyReleased.equalsIgnoreCase("W")) this.forward = false;
            if (keyReleased.equalsIgnoreCase("A")) this.left = false;
            if (keyReleased.equalsIgnoreCase("S")) this.backward = false;
            if (keyReleased.equalsIgnoreCase("D")) this.right = false;
            System.out.println("Key released: " + keyReleased);
            sendKeyCommand(this.forward, this.left, this.backward, this.right);
            updateWasdInGui(this.forward, this.left, this.backward, this.right);
        }
    }

    private void updateWasdInGui(boolean w, boolean a, boolean s, boolean d) {
        if (w) {
            wButtonIndicator.setFill(Color.BLUE);
            //System.out.println("Blue");
        } else {
            wButtonIndicator.setFill(Color.GREY);
            //System.out.println("Grey");
        }
        if (a) {
            aButtonIndicator.setFill(Color.BLUE);
            //System.out.println("Blue");
        } else {
            aButtonIndicator.setFill(Color.GREY);
            //System.out.println("Grey");
        }
        if (s) {
            sButtonIndicator.setFill(Color.BLUE);
            //System.out.println("Blue");
        } else {
            sButtonIndicator.setFill(Color.GREY);
            //System.out.println("Grey");
        }
        if (d) {
            dButtonIndicator.setFill(Color.BLUE);
            //System.out.println("Blue");
        } else {
            dButtonIndicator.setFill(Color.GREY);
            //System.out.println("Grey");
        }
    }

    @FXML
    private void speedValueControl() {
        this.speed = (int) speedValue.getValue();
        System.out.println("Speed value: " + speed);
    }

    private void sendKeyCommand(boolean forward, boolean left, boolean backward, boolean right) {
        boolean change = false;
        if (this.forward0 != forward) change = true;
        if (this.left0 != left) change = true;
        if (this.backward0 != backward) change = true;
        if (this.right0 != right) change = true;
        this.forward0 = forward;
        this.left0 = left;
        this.backward0 = backward;
        this.right0 = right;

        boolean[] wasd = new boolean[4];
        wasd[0] = forward;
        wasd[1] = left;
        wasd[2] = backward;
        wasd[3] = right;

        if (this.tcpClient.isConnectionActive() && change) {
            System.out.println("Directions: " + forward + ", " + left + ", " + backward + ", " + right);
            System.out.println("Speed: " + this.speed);
            Command cmd = new Command("directions", this.speed, wasd, null);
            this.tcpClient.sendCommand(cmd);
        }
    }


    @FXML
    void startConnection(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            this.tcpClient.disconnect();
        } else {
            setupConnection(this.hostField.getText(), this.portField.getText());
            this.disconnectButton.setDisable(false);
        }
    }

    @FXML
    void startUgv(ActionEvent event) {
        System.out.println("Sending: start");
        Command cmd = new Command("start", 0, null, null);
        this.tcpClient.sendCommand(cmd);
    }

    @FXML
    void stopUgv(ActionEvent event) {
        System.out.println("Sending: stop");
        Command cmd = new Command("stop", 0, null, null);
        this.tcpClient.sendCommand(cmd);
    }

    @FXML
    void disconnect(ActionEvent event) {
        this.tcpClient.disconnect();
        if (!this.tcpClient.isConnectionActive()) {
            this.connectButton.setText("Connect");
            this.connectButton.setDisable(false);
            this.hostField.setDisable(false);
            this.portField.setDisable(false);
            this.connectButton.setTextFill(Color.BLACK);
            this.disconnectButton.setDisable(true);
        }
    }


    private void setupConnection(String host, String port) {
        this.connectButton.setText("Connecting...");
        this.connectButton.setDisable(true);
        this.hostField.setDisable(true);
        this.portField.setDisable(true);
        boolean connected = this.tcpClient.connect(host, Integer.parseInt(port));
        if (connected) {
            userClientToServer();
            System.out.println("Telling server that this is an user client...");
            startPollingUGV();
            this.connectButton.setText("Connected!");
            this.connectButton.setTextFill(Color.GREEN);
        }
    }

    private void userClientToServer() {
        Command cmd = new Command("User", 0, null, null);
        this.tcpClient.sendCommand(cmd);
    }


}
