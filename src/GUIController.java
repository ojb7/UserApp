
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;


import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.util.List;

public class GUIController {
    @FXML
    private TextField portField;
    @FXML
    private TextField hostField;
    @FXML
    private Button connectButton;
    @FXML
    private Pane WASD;
    @FXML
    private Pane liveStreamPane;
    @FXML
    private Rectangle wButtonIndicator;
    @FXML
    private Rectangle aButtonIndicator;
    @FXML
    private Rectangle sButtonIndicator;
    @FXML
    private Rectangle dButtonIndicator;
    @FXML
    private Button startButton;
    @FXML
    private Slider speedValue;
    @FXML
    private ListView<String> listOfUGVs;
    @FXML
    private Button selectUgvButton;
    @FXML
    private ImageView liveImage;

    // TCP client
    private TCPClient tcpClient;

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
    private int ugvId = -1;

    // Autonomous condition, started or stopped
    private String autoCondition = "start";


    //private Thread userPollThread;
    private Thread imagePollThread;
    private Thread ugvListPollThread;


    ObservableList<String> obsListUgv;

    @FXML
    public void initialize() {
        System.out.println("Initialize GUI controller...");

        this.tcpClient = new TCPClient();
        this.hostField.setText(HOST_STASJ);
        this.portField.setText(PORT);
        this.hostField.setDisable(false);
        this.portField.setDisable(false);

        liveStreamPane.setStyle("-fx-background-color: #323437");


    }

    @FXML
    void selectUgvFromList(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            String ugvIdSelected = listOfUGVs.getSelectionModel().getSelectedItem();
            this.ugvId = Integer.parseInt(ugvIdSelected);
            System.out.println("Sending: Selected UGV " + this.ugvId);
            Command cmd = new Command("UGVSelected", this.ugvId, null, null);
            this.tcpClient.sendCommand(cmd);
            if (this.ugvId != -1) {
                // Live stream from server
                startPollingImage();
            }
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


    private void startPollingUgvList() {
        if (this.ugvListPollThread == null) {
            this.ugvListPollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started UGV list polling in Thread " + threadId);

                String cmdString = null;
                Command cmdFromServer = null;

                while (this.tcpClient.isConnectionActive()) {

                    Command cmd = new Command("updateUGVList", 0, null, null);
                    this.tcpClient.sendCommand(cmd);

                    Object objectFromServer = this.tcpClient.receiveObject();

                    if (objectFromServer instanceof Command) {
                        cmdFromServer = (Command) objectFromServer;
                        cmdString = cmdFromServer.getCommand();
                    }
                    if (cmdString != null) {
                        if (cmdString.equalsIgnoreCase("listUGV")) {
                            List<String> ugvIdList = cmdFromServer.getUGVs();
                            System.out.println("<<< List from server: " + ugvIdList);


                            if (ugvIdList.size() > 0) {
                                obsListUgv = FXCollections.observableArrayList(ugvIdList);
                            }

                            //this.listOfUGVs.setItems(null);
                            this.listOfUGVs.setItems(obsListUgv);
                            this.listOfUGVs.refresh();
                            this.listOfUGVs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("UGV list polling thread " + threadId + " exiting...");
                this.ugvListPollThread = null;
            });
            this.ugvListPollThread.start();
        }
    }

    private void startPollingImage() {
        if (this.imagePollThread == null) {
            this.imagePollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started image polling in Thread " + threadId);

                ImageObject imageFromServer;

                while (this.tcpClient.isConnectionActive()) {
                    Command cmd = new Command("updateServerImage", 0, null, null);
                    this.tcpClient.sendCommand(cmd);

                    ImageObject objectFromServer = (ImageObject) this.tcpClient.receiveObject();

                    if (objectFromServer != null) {
                        System.out.println("Object instance of ImageObject");
                        ByteArrayInputStream bis = new ByteArrayInputStream(objectFromServer.getImageBytes());
                        BufferedImage bImage = null;

                        try {
                            System.out.println("Reading buffered image...");
                            bImage = ImageIO.read(bis);
                        } catch (IOException e) {
                            System.err.println("Error reading buffered image: " + e.getMessage());
                        }

                        Image img = SwingFXUtils.toFXImage(bImage, null);

                        liveImage.setImage(img);
                        // Update ImageView on GUI

                        imageFromServer = null;
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Image polling thread " + threadId + " exiting...");
                this.imagePollThread = null;
            });
            this.imagePollThread.start();
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
            System.out.println(">>> Directions: " + forward + ", " + left + ", " + backward + ", " + right);
            System.out.println(">>> Speed: " + this.speed);
            Command cmd = new Command("manual", this.speed, wasd, null);
            this.tcpClient.sendCommand(cmd);
        }
    }


    @FXML
    void startStopUgv(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            // Create instance of Command to be sent to server
            Command cmd = null;
            if (this.autoCondition.equals("start")) {
                // Update start/stop option
                this.autoCondition = "stop";
                // Create command to send to server
                System.out.println(">>> UGV: start");
                cmd = new Command("start", 0, null, null);
                // Update text on button
                this.startButton.setText("Stop");
            } else if (this.autoCondition.equals("stop")) {
                // Update start/stop option
                this.autoCondition = "start";
                // Create command to send to server
                System.out.println(">>> UGV: stop");
                cmd = new Command("stop", 0, null, null);
                // Update text on button
                this.startButton.setText("Start");
            } else {
                System.err.println("Could not start/stop UGV...");
            }
            // Send command to server
            this.tcpClient.sendCommand(cmd);
        }
    }


//    @FXML
//    void disconnect(ActionEvent event) {
//        this.tcpClient.disconnect();
//        if (!this.tcpClient.isConnectionActive()) {
//            this.connectButton.setText("Connect");
//            this.connectButton.setDisable(false);
//            this.hostField.setDisable(false);
//            this.portField.setDisable(false);
//            this.connectButton.setTextFill(Color.BLACK);
//            this.disconnectButton.setDisable(true);
//        }
//    }

    @FXML
    void startConnection(ActionEvent event) {
        boolean connection = this.tcpClient.isConnectionActive();
        if (connection) {
            // When pressing disconnect
            this.tcpClient.disconnect();
            this.connectButton.setText("Connect");
        } else {
            // When pressing connect
            setupConnection(this.hostField.getText(), this.portField.getText());
            this.connectButton.setText("Disconnect");
        }
    }


    private void setupConnection(String host, String port) {
        this.connectButton.setText("Connecting...");
        boolean connected = this.tcpClient.connect(host, Integer.parseInt(port));
        if (connected) {
            System.out.println("Telling server that this is an user client...");
            userClientToServer();
            this.hostField.setDisable(true);
            this.portField.setDisable(true);

            // Update UGV list from server
            startPollingUgvList();


            // Live stream from server
            /////startPollingImage();


        } else {
            this.connectButton.setText("Connecting...");
            this.hostField.setDisable(false);
            this.portField.setDisable(false);
        }
    }

    private void userClientToServer() {
        Command cmd = new Command("User", 0, null, null);
        this.tcpClient.sendCommand(cmd);
    }
}
