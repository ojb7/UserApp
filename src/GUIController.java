
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;


import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
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
    @FXML
    private Button manualModeButton;
    @FXML
    private CheckBox connectionCheckBox;
    @FXML
    private CheckBox ugvStatusCheckBox;

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

    // List og UGV ID's from server
    List<String> ugvIdList;
    // UGV ID set from list of UGV's from server
    private int ugvId = -1;

    // Autonomous condition, started or stopped
    private String autoCondition = "Off";
    private String manualCondition = "Off";

    // Connection condition, connected/notConnected
    String connectionCondition = "notConnected";


    //private Thread userPollThread;
    private Thread imagePollThread;
    private Thread ugvListPollThread;
    private Thread connectionThread;
    private Thread ugvStatusThread;

    // Total images UGV should take
    private int autoUgvImages = 15;

    ObservableList<String> obsListUgv;

    @FXML
    public void initialize() {
        System.out.println("Initialize GUI controller...");

        this.tcpClient = new TCPClient();
        this.hostField.setText(HOST_STASJ);
        this.portField.setText(PORT);

        liveStreamPane.setStyle("-fx-background-color: #323437");


    }


    @FXML
    void selectUgvFromList(ActionEvent event) {
        if (this.tcpClient.isConnectionActive()) {
            String ugvIdSelected = listOfUGVs.getSelectionModel().getSelectedItem();
            this.ugvId = Integer.parseInt(ugvIdSelected);
            System.out.println(">>> Sending: Selected UGV " + this.ugvId);
            this.tcpClient.setUgvIdToServer(this.ugvId);
            checkUgvIdStatus();
        }
    }


    private void startPollingUgvList() {
        if (this.ugvListPollThread == null) {
            // Create a new thread for polling UGV list
            this.ugvListPollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started UGV list polling in Thread " + threadId);

                String cmdString = null;
                Command cmdFromServer = null;

                while (this.tcpClient.isConnectionActive()) {
                    // Ask for UGV list from server
                    this.tcpClient.askUgvListFromServer();

                    // Try to receive object from server
                    Object objectFromServer = this.tcpClient.receiveObject();

                    if (objectFromServer instanceof Command) {
                        cmdFromServer = (Command) objectFromServer;
                        cmdString = cmdFromServer.getCommand();
                    }
                    if (cmdString != null) {
                        if (cmdString.equalsIgnoreCase("listUGV")) {
                            ugvIdList = cmdFromServer.getUGVs();
                            //System.out.println("<<< List from server: " + ugvIdList);

                            obsListUgv = FXCollections.observableArrayList(ugvIdList);

                            Platform.runLater(() -> {
                                this.listOfUGVs.setItems(obsListUgv);
                            });

                            this.listOfUGVs.refresh();
                            this.listOfUGVs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                    }
                    try {
                        Thread.sleep(500);
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

    private void startPollingUGVImage() {
        if (this.imagePollThread == null) {
            this.imagePollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started image polling in Thread " + threadId);

                ImageObject imageFromServer;

                while (this.tcpClient.isConnectionActive() && (ugvId != -1) && (this.autoCondition.equalsIgnoreCase("On"))) {

                    ////////////////////////////////////////////////////// Images from server------------------------
                    this.tcpClient.askUgvImageFromServer();

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

                        this.liveImage.setImage(img);
                        // Update ImageView on GUI

                        imageFromServer = null;
                    }
                    try {
                        // Set time to update image
                        Thread.sleep(1000);
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
        if (this.autoCondition.equalsIgnoreCase("Off") && this.manualCondition.equalsIgnoreCase("On")) {
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
    }

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        if (this.autoCondition.equalsIgnoreCase("Off") && this.manualCondition.equalsIgnoreCase("On")) {
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

            this.tcpClient.setManualDirectionsToServer(wasd, (int) speedValue.getValue());
        }
    }


    @FXML
    void startStopManualUgv(ActionEvent event) {
        // If connection is on and autonomous is off
        if (this.tcpClient.isConnectionActive() && (this.autoCondition.equalsIgnoreCase("Off"))) {
            if (this.manualCondition.equals("Off")) {
                // Update start/stop option
                this.manualCondition = "On";
                System.out.println(">>> UGV: Manual " + manualCondition);
                this.tcpClient.setManualOnToServer();
                // Update text on button
                this.manualModeButton.setText("Manual " + manualCondition);
            } else if (this.manualCondition.equals("On")) {
                // Update start/stop option
                this.manualCondition = "Off";
                System.out.println(">>> UGV: Manual " + manualCondition);
                this.tcpClient.setManualOffToServer();
                // Update text on button
                this.manualModeButton.setText("Manual " + manualCondition);
            } else {
                System.err.println("Could not start/stop manual mode on UGV...");
            }
        } else {
            // If not connected set to "Off"
            this.manualCondition = "Off";
            this.manualModeButton.setText("Manual " + manualCondition);
        }
    }


    @FXML
    void startStopUgv(ActionEvent event) {
        if (this.tcpClient.isConnectionActive() && (this.manualCondition.equalsIgnoreCase("Off"))) {
            if (this.autoCondition.equals("Off")) {
                // Update start/stop option
                this.autoCondition = "On";

                System.out.println(">>> UGV: Autonomous Mode " + this.autoCondition);
                this.tcpClient.setAutoOnToServer(this.autoUgvImages);

                // Update text on button
                this.startButton.setText("Auto " + this.autoCondition);
                // Start polling image from UGV
                startPollingUGVImage();
            } else if (this.autoCondition.equals("On")) {
                // Update start/stop option
                this.autoCondition = "Off";
                System.out.println(">>> UGV: Autonomous Mode " + this.autoCondition);
                this.tcpClient.setAutoOffToServer();
                // Update text on button
                this.startButton.setText("Auto " + this.autoCondition);
            } else {
                System.err.println("Could not start/stop auto mode on UGV...");
            }
        } else {
            // If not connected set to "Off"
            this.autoCondition = "Off";
            this.startButton.setText("Auto " + autoCondition);
        }
    }


    @FXML
    void startConnection(ActionEvent event) {
        if (this.connectionCondition.equalsIgnoreCase("connected")) {
            // Disconnect from server
            this.tcpClient.disconnect();
            this.connectionCondition = "notConnected";
        }
        if (this.connectionCondition.equalsIgnoreCase("notConnected")) {
            // Connect to server
            setupConnection(this.hostField.getText(), this.portField.getText());
            this.connectionCondition = "connected";
        }
    }


    private void checkUgvIdStatus() {
        if (this.ugvStatusThread == null) {
            // A thread for checking UGV status
            this.ugvStatusThread = new Thread(() -> {
                // Get thread ID and print to console
                long threadId = Thread.currentThread().getId();
                System.out.println("Starting checking UGV status thread " + threadId);

                // Boolean for exiting connection thread
                boolean exitThread = false;
                while (!exitThread) {

                    String id = Integer.toString(ugvId);
                    boolean legalUgvId = ugvIdList.contains(id);

                    if (legalUgvId) {
                        // Update UGV status check box in GUI
                        Platform.runLater(() -> {
                            this.ugvStatusCheckBox.setSelected(true);
                            this.ugvStatusCheckBox.setText("Connected to UGV" + ugvId);
                        });
                    } else {
                        // Update UGV status check box in GUI
                        Platform.runLater(() -> {
                            this.ugvStatusCheckBox.setSelected(false);
                            this.ugvStatusCheckBox.setText("Connected to UGV");
                        });
                        this.ugvId = -1;
                        exitThread = true;
                    }
                    // Set thread to sleep 100 milli seconds
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Check UGV status thread " + threadId + " has been interupted...");
                    }
                }
                // Set ugvStatusThread to null, thread is not in use
                System.out.println("Check UGV status thread " + threadId + " exiting...");
                this.ugvStatusThread = null;
            });

            // Start connection thread
            this.ugvStatusThread.start();
        }

    }

    private void setupConnection(String host, String port) {
        if (this.connectionThread == null) {

            // A thread for connection to server
            this.connectionThread = new Thread(() -> {
                // Get thread ID and print to console
                long threadId = Thread.currentThread().getId();
                System.out.println("Starting connection thread " + threadId);

                // Update connection button on GUI to "Connecting..."
                Platform.runLater(() -> {
                    this.connectButton.setText("Connecting...");
                });

                // Try to connect to server
                boolean connected = this.tcpClient.connect(host, Integer.parseInt(port));

                if (connected) {
                    // Tell server that this client is an User client
                    this.tcpClient.setUserStateToServer();
                    // Start polling list of UGV's from server
                    startPollingUgvList();
                }

                // Boolean for exiting connection thread
                boolean exitThread = false;
                while (!exitThread) {
                    if (this.tcpClient.isConnectionActive()) {
                        // When connection is active set connectionCondition to "connected"
                        this.connectionCondition = "connected";
                        Platform.runLater(() -> {
                            // Update server status checkbox on GUI
                            this.connectionCheckBox.setSelected(true);
                            // Update connection button to "Disconnect" on GUI
                            this.connectButton.setText("Disconnect");
                        });
                        // Disable host and port field on GUI
                        this.hostField.setDisable(true);
                        this.portField.setDisable(true);
                    } else {
                        // When connection is NOT active set connectionCondition to "notConnected"
                        this.connectionCondition = "notConnected";

                        Platform.runLater(() -> {
                            // Update server status checkbox on GUI
                            this.connectionCheckBox.setSelected(false);
                            // Update connection button to "Connect" on GUI
                            this.connectButton.setText("Connect");
                        });
                        // Enable host and port field on GUI
                        this.hostField.setDisable(false);
                        this.portField.setDisable(false);
                        // exitThread variable is set to true to exit connection thread
                        exitThread = true;
                    }
                    // Set thread to sleep 100 milli seconds
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Connection thread " + threadId + " has been interupted...");
                    }
                }
                // Set connectionThread to null, thread is not in use
                System.out.println("Connection thread " + threadId + " exiting...");
                this.connectionThread = null;
            });

            // Start connection thread
            this.connectionThread.start();
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

}