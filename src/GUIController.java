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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
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
    private Pane WASD;
    @FXML
    private AnchorPane liveStreamPane;
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
    @FXML
    private TitledPane statusPane;
    @FXML
    private ChoiceBox imagesNumberList;
    @FXML
    private ProgressBar meshroomProgressBar;
    @FXML
    private Button setNumberOfImagesButton;
    @FXML
    private Label progressLabel;

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

    // Server connection information
    private static final String HOST = "10.22.192.92";
    private static final String HOST_STASJ = "83.243.240.94";
    private static final String PORT = "42069";

    // UGV image from server
    private Image ugvImage;

    // Autonomous/Manual condition, started or stopped
    private String autoCondition = "Off";
    private String manualCondition = "Off";

    // Connection condition, "connected"/"notConnected"
    private String connectionCondition = "notConnected";

    // True when ugvId is on UGV list
    private boolean ugvCheckBox = false;

    // Threads
    private Thread imagePollThread;
    private Thread ugvListPollThread;
    private Thread objectFilePollThread;
    private Thread connectionThread;
    private Thread updateGUIThread;
    private Thread progressPollThread;

    // Total images UGV should take
    private int autoUgvImages = 0;

    // ObservableList of List of UGV from server
    private ObservableList<String> obsListUgv;
    // List og UGV ID's from server
    private List<String> ugvIdList;
    // UGV ID set from list of UGV's from server
    private int ugvId = -1;

    private int progressSteps = 14;
    private double progressDouble;
    List<String> progressText = new ArrayList<>();


    /**
     * This method is called only once, when the application starts.
     */
    @FXML
    public void initialize() {
        System.out.println("Initialize GUI controller...");

        // Create instance of TCP Client
        this.tcpClient = new TCPClient();

        // Set server information to corresponding fields
        this.hostField.setText(HOST_STASJ);
        this.portField.setText(PORT);

        // Set selection of UGV in UGV list to only one option
        this.listOfUGVs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Start thread for updating GUI elements
        updateGui();

        // Set the speed to initial value from speed controller on GUI
        this.speed = (int) speedValue.getValue();

        // Fill list of number of images to UGV
        List<String> listImages = new ArrayList<>();
        for (int i = 5; i <= 20; i++) {
            String numberString = "" + i * 3;
            listImages.add(numberString);
        }
        this.imagesNumberList.setItems(FXCollections.observableArrayList(listImages));

        // Adds all 3D modeling progress steps to the progressText list
        this.progressText.add(0, "Starting Meshroom Progress...");
        this.progressText.add(1, "CameraInit...");
        this.progressText.add(2, "FeatureExtraction...");
        this.progressText.add(3, "ImageMatching...");
        this.progressText.add(4, "FeatureMatching...");
        this.progressText.add(5, "StructureFromMotion...");
        this.progressText.add(6, "PrepareDenseScene...");
        this.progressText.add(7, "CameraConnection...");
        this.progressText.add(8, "DepthMap...");
        this.progressText.add(9, "DepthMapFilter...");
        this.progressText.add(10, "Meshing...");
        this.progressText.add(11, "MeshFiltering...");
        this.progressText.add(12, "Texturing...");
        this.progressText.add(13, "Done!");
        this.progressText.add(14, "");
    }


    /**
     * This method is called when Connect/Disconnect button is pressed. Tries to connect/disconnect from server.
     *
     * @param event The event when button is pressed
     */
    @FXML
    void startConnection(ActionEvent event) {
        if (this.connectionCondition.equalsIgnoreCase("connected")) {
            // Disconnect from server
            this.tcpClient.disconnect();
        } else if (this.connectionCondition.equalsIgnoreCase("notConnected")) {
            // Connect to server
            setupConnection(this.hostField.getText(), this.portField.getText());
        }
    }

    /**
     * This method is called when select UGV button is pressed. Tells server which UGV to connect to.
     *
     * @param event The event when button is pressed
     */
    @FXML
    void selectUgvFromList(ActionEvent event) {
        // String of selected UGV ID from UGV ID list
        String ugvIdSelected = listOfUGVs.getSelectionModel().getSelectedItem();
        if (ugvIdSelected != null) {
            // Convert String of UGV ID to int
            this.ugvId = Integer.parseInt(ugvIdSelected);
            // Send command to server which UGV ID to connect to
            System.out.println(">>> Sending: Selected UGV " + this.ugvId);
            this.tcpClient.setUgvIdToServer(this.ugvId);
        }
    }

    /**
     * This method is called when set number of images button is pressed. It sets the desired number of images the
     * UGV should take of any given object in auto mode).
     *
     * @param event The event when button is pressed
     */
    @FXML
    void setNumberOfUgvImages(ActionEvent event) {
        // String of number of images the UGV should take in auto mode
        String NumberOfImg = (String) this.imagesNumberList.getSelectionModel().getSelectedItem();
        if (NumberOfImg != null) {
            // Convert String of number of images to int
            this.autoUgvImages = Integer.parseInt(NumberOfImg);
            System.out.println("Selected " + autoUgvImages + " images to UGV...");
        }
    }

    /**
     * This method is called when any keys on keyboard is pressed. Will send manual control commands to UGV.
     *
     * @param event The event when any key is pressed
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        // Change the key state of forward, left, backward or right
        changeKeyState(event.getText(), true);

        if (this.autoCondition.equals("Off") && this.manualCondition.equals("On")) {
            if (checkKeyStateChanges()) {
                // Send key states to server if there are changes
                this.tcpClient.setManualDirectionsToServer(getWasdArray(), this.speed);
            }
        }
    }

    /**
     * This method is called when any keys on keyboard is released. Will send manual control commands to UGV.
     *
     * @param event The event when any key is released
     */
    @FXML
    private void handleKeyReleased(KeyEvent event) {
        // Change the key state of forward, left, backward or right
        changeKeyState(event.getText(), false);

        if (this.autoCondition.equals("Off") && this.manualCondition.equals("On")) {
            if (checkKeyStateChanges()) {
                // Send key states to server if there are changes
                this.tcpClient.setManualDirectionsToServer(getWasdArray(), this.speed);
            }
        }
    }

    /**
     * This method is called when the speed control slider is changed
     */
    @FXML
    private void speedValueControl() {
        this.speed = (int) speedValue.getValue();
    }

    /**
     * This method is called when start/stop button for manual mode is pressed. Changes the manual control state to UGV.
     *
     * @param event The event when button is pressed
     */
    @FXML
    void startStopManualUgv(ActionEvent event) {
        // If connection is on and autonomous is off
        if ((this.ugvId != -1) && (this.autoCondition.equalsIgnoreCase("Off"))) {
            if (this.manualCondition.equals("Off")) {
                // Update start/stop option
                this.manualCondition = "On";
                System.out.println(">>> UGV: Manual " + manualCondition);
                this.tcpClient.setManualOnToServer();

            } else if (this.manualCondition.equals("On")) {
                // Update start/stop option
                this.manualCondition = "Off";
                System.out.println(">>> UGV: Manual " + manualCondition);
                this.tcpClient.setManualOffToServer();
                changeKeyState("W", false);
                changeKeyState("A", false);
                changeKeyState("S", false);
                changeKeyState("D", false);
                updateWasdInGui();
            } else {
                System.err.println("Could not start/stop manual mode on UGV...");
            }
        }
    }

    /**
     * This method is called when start/stop button for auto mode is pressed. Changes the auto control state to UGV.
     *
     * @param event The event when button is pressed
     */
    @FXML
    void startStopUgv(ActionEvent event) {
        if ((this.ugvId != -1) && (this.manualCondition.equalsIgnoreCase("Off"))) {
            if (this.autoCondition.equals("Off")) {
                // Update start/stop condition
                this.autoCondition = "On";
                System.out.println(">>> UGV: Autonomous Mode " + this.autoCondition);
                this.tcpClient.setAutoOnToServer(this.autoUgvImages);
                // Start polling image from UGV
                startPollingUGVImage();
            } else if (this.autoCondition.equals("On")) {
                // Update start/stop option
                this.autoCondition = "Off";
                System.out.println(">>> UGV: Autonomous Mode " + this.autoCondition);
                this.tcpClient.setAutoOffToServer();
            } else {
                System.err.println("Could not start/stop auto mode on UGV...");
            }
        }
    }


    //--------------------------------- METHODS STARTING WORKING THREADS (START) ---------------------------------//

    /**
     * This method starts a new thread for updating all elements on GUI.
     * It should be running as long as the GUI is running
     */
    private void updateGui() {
        if (this.updateGUIThread == null) {
            // A thread for updating buttons on GUI
            this.updateGUIThread = new Thread(() -> {
                // Get thread ID and print to console
                long threadId = Thread.currentThread().getId();
                System.out.println("Starting update GUI elements thread " + threadId);

                boolean exitThread = false;
                while (!exitThread) {
                    // Update connection status on GUI
                    updateConnectionStatus();

                    // Updates manual and auto state on GUI if disconnected from server or UGV
                    updateAutoManualStatus();

                    // Update ping to server on GUI
                    updatePing();

                    // Updates the UGV ID
                    updateUgvIdStatus();

                    // Update set images pane
                    updateImageChoser();

                    // Updates the progress bar on GUI when server is processing 3D model
                    updateProgressBar();

                    // Update ImageView on GUI
                    updateImageOnGui();

                    // Updates the UGV ID list on the GUI
                    updateUgvListOnGui();

                    // Update WASD elements in GUI
                    updateWasdInGui();


                    // UPDATE ON GUI
                    Platform.runLater(() -> {
                        this.ugvStatusCheckBox.setSelected(this.ugvCheckBox);
                        this.ugvStatusCheckBox.setText("Connected to UGV" + this.ugvId);

                    });


                    // Set thread to sleep 100 milli seconds
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Update GUI elements thread " + threadId + " has been interupted...");
                    }
                }
                // Sets the thread to null when finished
                System.out.println("Update GUI elements thread " + threadId + " exiting...");
                this.updateGUIThread = null;
            });
            // Starts update GUI thread
            this.updateGUIThread.start();
        }
    }

    /**
     * This method will create a new thread to try to connect to the server.
     *
     * @param host The host IP address
     * @param port The port number at the host
     */
    private void setupConnection(String host, String port) {
        if (this.connectionThread == null) {

            // A thread for connection to server
            this.connectionThread = new Thread(() -> {
                // Get thread ID and print to console
                long threadId = Thread.currentThread().getId();
                System.out.println("Starting connection thread " + threadId);

                // Try to connect to server
                boolean connected = this.tcpClient.connect(host, Integer.parseInt(port));

                if (connected) {
                    // Tell server that this client is an User client
                    this.tcpClient.setUserStateToServer();
                    // Start polling list of UGV's from server
                    startPollingUgvList();
                }
                // Sets the thread to null when finished
                System.out.println("Connection thread " + threadId + " exiting...");
                this.connectionThread = null;
            });

            // Starts connection thread
            this.connectionThread.start();
        }
    }

    /**
     * This method will create a new thread and start asking for list of available UGV's from the server and store the
     * list for viewing in GUI.
     */
    private void startPollingUgvList() {
        if (this.ugvListPollThread == null) {
            // Create a new thread for polling UGV list
            this.ugvListPollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started UGV list polling in Thread " + threadId);

                while (this.tcpClient.isConnectionActive()) {
                    // Ask for UGV list from server
                    this.tcpClient.askUgvListFromServer();
                    // Try to receive object from server
                    Command cmdFromServer = this.tcpClient.receiveCommand();

                    if ((cmdFromServer != null) && (cmdFromServer.getCommand() != null)) {
                        if (cmdFromServer.getCommand().equalsIgnoreCase("listUGV")) {
                            // Set the UGV ID list to list from server
                            this.ugvIdList = cmdFromServer.getUGVs();
                            // Set the UGV list to an observable array list observable array list for the GUI
                            this.obsListUgv = FXCollections.observableArrayList(ugvIdList);
                        }
                    }
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // Sets the thread to null when finished
                System.out.println("UGV list polling thread " + threadId + " exiting...");
                this.ugvListPollThread = null;
            });
            // Start UGV list polling thread
            this.ugvListPollThread.start();
        }
    }

    /**
     * This method will create a new thread and start asking for images, taken by UGV, from the server and store the
     * image for viewing in GUI. When all images has been received, another process should start (Progress on 3D modeling).
     */
    private void startPollingUGVImage() {
        if (this.imagePollThread == null) {
            this.imagePollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started image polling in Thread " + threadId);

                // Int of images received from UGV
                int imageCounter = 0;

                while (this.autoCondition.equalsIgnoreCase("On")) {
                    // Ask server for images from UGV
                    this.tcpClient.askUgvImageFromServer();

                    // Receive ImageObject from server
                    ImageObject imageFromServer = this.tcpClient.receiveImageObject();

                    if (imageFromServer != null) {
                        System.out.println(">>> ImageObject from server!");
                        // Read the byte array from ImageObject
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageFromServer.getImageBytes());
                        // Instance og BufferedImage
                        BufferedImage bImage = null;
                        try {
                            System.out.println("Reading buffered image...");
                            // Create a BufferedImage of the bytes in ImageObject
                            bImage = ImageIO.read(bis);
                        } catch (IOException e) {
                            System.err.println("Error reading buffered image: " + e.getMessage());
                        }

                        if (bImage != null) {
                            // Convert the BufferedImage to Image
                            ugvImage = SwingFXUtils.toFXImage(bImage, null);
                            imageCounter++;
                            System.out.println("----------------------------------------------- Image count:" + imageCounter);
                        }

                        if (imageCounter == autoUgvImages) {
                            // Set autoCondition to "Off" when all images has received
                            this.autoCondition = "Off";
                        }
                    }
                    try {
                        // Set time to update image
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Start asking for 3D object from server when all UGV images has been received
                startPollingProgress();

                // Sets the thread to null when finished
                System.out.println("Image polling thread " + threadId + " exiting...");
                this.imagePollThread = null;
            });
            // Starts thread
            this.imagePollThread.start();
        }
    }

    /**
     * This method will create a new thread and start asking for progress in image to 3D model process, from the server,
     * and store the progress steps for viewing in GUI. When the process is done, another process should
     * start (Get 3D files from server).
     */
    private void startPollingProgress() {
        this.progressSteps = 0;
        if (this.progressPollThread == null) {
            this.progressPollThread = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Started progress polling in Thread " + threadId);

                while (this.progressSteps < 12) {
                    // Ask server for progress in 3D modelling
                    this.tcpClient.askServerProgress();

                    // Receive Command from server
                    Command progressFromServer = this.tcpClient.receiveCommand();

                    if ((progressFromServer != null) && (progressFromServer.getCommand().equalsIgnoreCase("progress"))) {
                        this.progressSteps = progressFromServer.getValue();
                    }

                    try {
                        // Set time to update image
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // Reset progress steps
                this.progressSteps = 13;

                // Start polling ObjectFile from server
                startPollingObjectFile();

                // Sets the thread to null when finished
                System.out.println("Progress polling thread " + threadId + " exiting...");
                this.progressPollThread = null;
            });
            // Starts polling progress thread
            this.progressPollThread.start();
        }
    }

    /**
     * This method will create a new thread and start asking for the finished 3D models from the server and store the
     * models on default location on the PC.
     */
    private void startPollingObjectFile() {
        if (this.objectFilePollThread == null) {

            // A thread for polling ObjectFile from server
            this.objectFilePollThread = new Thread(() -> {
                // Get thread ID and print to console
                long threadId = Thread.currentThread().getId();
                System.out.println("Starting ObjectFile polling thread " + threadId);

                boolean exitThread = false;
                while (!exitThread) {
                    // Ask for ObjectFile from server
                    this.tcpClient.askObjectFile();
                    // Receive ObjectFile from Server
                    ObjectFile objectFileFromServer = this.tcpClient.receiveObjectFile();

                    // If ObjectFile is not null, then save to Desktop
                    if (objectFileFromServer != null) {

                        // Get file bytes from ObjectFile from server
                        byte[] objectFileBytes = objectFileFromServer.getObjFile();
                        byte[] mtlFileBytes = objectFileFromServer.getMtlFile();
                        byte[] pngBytes = objectFileFromServer.getPngFile();

                        // Create new files
                        File fileObj = new File(System.getProperty("user.home"), objectFileFromServer.getObjFileName());
                        File fileMtl = new File(System.getProperty("user.home"), objectFileFromServer.getMtlFileName());
                        File filePng = new File(System.getProperty("user.home"), objectFileFromServer.getPngFileName());

                        // Try to save the files to default home location on PC
                        try {
                            Files.write(Path.of(fileObj.getPath()), objectFileBytes);
                            Files.write(Path.of(fileMtl.getPath()), mtlFileBytes);
                            Files.write(Path.of(filePng.getPath()), pngBytes);
                            System.out.println("All files saved succesfully!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Exits thread
                        exitThread = true;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Sets the thread to null when finished
                System.out.println("ObjectFile polling thread " + threadId + " exiting...");
                this.objectFilePollThread = null;
            });
            // Start ObjectFile polling thread
            this.objectFilePollThread.start();
        }
    }

    //--------------------------------- METHODS STARTING WORKING THREADS (STOP) ---------------------------------//

    //--------------------------------- HELPFUL LOGIC METHODS (START) ---------------------------------//

    /**
     * Updates the key states forward, left, backward, right (WASD)
     *
     * @param keyEvent        String for key pressed or released
     * @param pressedReleased Boolean true/false based on pressed/released
     */
    private void changeKeyState(String keyEvent, boolean pressedReleased) {
        // If statements to update the current state of forward, left, backward, right
        if (keyEvent.equalsIgnoreCase("W")) this.forward = pressedReleased;
        if (keyEvent.equalsIgnoreCase("A")) this.left = pressedReleased;
        if (keyEvent.equalsIgnoreCase("S")) this.backward = pressedReleased;
        if (keyEvent.equalsIgnoreCase("D")) this.right = pressedReleased;
    }

    /**
     * Checks if there are changes in key states (WASD)
     *
     * @return Returns true/false based on changes in key states (WASD)
     */
    private boolean checkKeyStateChanges() {
        // Boolean for the changed state of forward, left, backward and right
        boolean changed = false;

        // If statements for checking if there are any changed from previously
        if (this.forward0 != this.forward) changed = true;
        if (this.left0 != this.left) changed = true;
        if (this.backward0 != this.backward) changed = true;
        if (this.right0 != this.right) changed = true;
        // Update shadows
        this.forward0 = this.forward;
        this.left0 = this.left;
        this.backward0 = this.backward;
        this.right0 = this.right;

        // Return true/false for the changed state
        return changed;
    }

    /**
     * Creates an array of booleans of key states (WASD)
     *
     * @return Returns an array of key states (WASD)
     */
    private boolean[] getWasdArray() {
        // Create new array of boolean
        boolean[] wasd = new boolean[4];
        // Fill the array of boolean
        wasd[0] = this.forward;
        wasd[1] = this.left;
        wasd[2] = this.backward;
        wasd[3] = this.right;
        //Return the array of boolean
        return wasd;
    }

    //--------------------------------- HELPFUL LOGIC METHODS (START) ---------------------------------//

    //--------------------------------- UPDATE GUI METHODS (START) ---------------------------------//

    /**
     * This method calculates a progress number between 0 and 1 and updates the ProgressBar on GUI accordingly
     */
    private void updateProgressBar() {
        // Creates a number between 0 to 1, based on which step in progress it is
        this.progressDouble = ((double) this.progressSteps) / 12;

        // Set the progress to 0 if progress double is above 1
        if (this.progressDouble > 1) {
            this.progressDouble = 0;
        }
        // Update ProgressBar and its label on GUI
        Platform.runLater(() -> {
            this.meshroomProgressBar.setProgress(this.progressDouble);
            this.progressLabel.setText("Progress: " + this.progressText.get(this.progressSteps));
        });
    }

    /**
     * This method checks the state of "set images to UGV" pane and updates GUI accordingly
     */
    private void updateImageChoser() {
        if (this.autoCondition.equalsIgnoreCase("On") || (this.ugvId == -1)) {
            this.setNumberOfImagesButton.setDisable(true);
            this.imagesNumberList.setDisable(true);
        } else {
            this.setNumberOfImagesButton.setDisable(false);
            this.imagesNumberList.setDisable(false);
        }
    }

    /**
     * This method checks the connection to an UGV and updates start/stop of auto and manual mode
     */
    private void updateAutoManualStatus() {
        // If not connection to server and illegal UGV ID
        if (!this.tcpClient.isConnectionActive() || (this.ugvId == -1)) {
            // Set to default conditions
            this.manualCondition = "Off";
            this.autoCondition = "Off";

            // Update state on buttons on GUI
            this.manualModeButton.setDisable(true);
            this.startButton.setDisable(true);
        } else {
            // Update state on buttons on GUI
            this.manualModeButton.setDisable(false);
            this.startButton.setDisable(false);

        }
        Platform.runLater(() -> {
            // Update text on buttons
            this.startButton.setText("Auto: " + this.autoCondition);
            this.manualModeButton.setText("Manual: " + this.manualCondition);
        });
    }

    /**
     * This method checks the connection to UGV ID and updates GUI accordingly
     */
    private void updateUgvIdStatus() {
        // When no connection to server, set UGV ID to default value -1 (illegal)
        if (!this.tcpClient.isConnectionActive()) {
            this.ugvId = -1;
            this.ugvCheckBox = false;
        }

        // Disable selectUgvButton when list of UGV's is empty
        if ((this.ugvIdList != null) && (this.ugvIdList.isEmpty())) {
            this.ugvId = -1;
            this.ugvCheckBox = false;
        }

        // Update select button when the list does not contain the current UGV ID and update
        if ((this.ugvIdList != null) && (!this.ugvIdList.contains(Integer.toString(this.ugvId)))) {
            this.ugvId = -1;
            this.ugvCheckBox = false;
        } else if ((this.ugvIdList != null) && (this.ugvIdList.contains(Integer.toString(this.ugvId)))) {
            this.ugvCheckBox = true;
        }

        // If ugvId is a legal ID disable selectUgvButton
        if (ugvId != -1) {
            this.selectUgvButton.setDisable(true);
            this.ugvCheckBox = true;
        } else {
            this.selectUgvButton.setDisable(false);
            this.ugvCheckBox = false;
        }

        // Clear and update UGV list on GUI when connection is lost
        if (!this.tcpClient.isConnectionActive() && (ugvIdList != null)) {
            this.ugvIdList.clear();
            this.obsListUgv = FXCollections.observableArrayList(this.ugvIdList);
        }
    }

    /**
     * This method checks the connection state to server and updates GUI accordingly
     */
    private void updateConnectionStatus() {
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
        }
    }

    /**
     * This method gets the ping to server and updates ping text on GUI accordingly
     */
    private void updatePing() {
        if (this.tcpClient.isConnectionActive()) {
            Platform.runLater(() -> {
                this.statusPane.setText("Ping: " + this.tcpClient.getPing() + "ms");
            });
        } else {
            Platform.runLater(() -> {
                this.statusPane.setText("Status");
            });
        }
    }

    /**
     * This method checks for new live images from UGV and updates GUI accordingly
     */
    private void updateImageOnGui() {
        if (this.liveImage != null && this.ugvImage != null) {
            // Fit the image to the pane on GUI
            this.liveImage.fitWidthProperty().bind(liveStreamPane.widthProperty());
            this.liveImage.fitHeightProperty().bind(liveStreamPane.heightProperty());

            // Update ImageView on GUI
            this.liveImage.setImage(ugvImage);
        }
    }

    /**
     * This method updates GUI with new list of UGV's
     */
    private void updateUgvListOnGui() {
        Platform.runLater(() -> {
            this.listOfUGVs.setItems(this.obsListUgv);
        });
    }

    /**
     * This method updates the WASD keys visualization on GUI
     */
    private void updateWasdInGui() {
        if (this.forward) {
            this.wButtonIndicator.setFill(Color.BLUE);
        } else {
            this.wButtonIndicator.setFill(Color.GREY);
        }
        if (this.left) {
            this.aButtonIndicator.setFill(Color.BLUE);
        } else {
            this.aButtonIndicator.setFill(Color.GREY);
        }
        if (this.backward) {
            this.sButtonIndicator.setFill(Color.BLUE);
        } else {
            this.sButtonIndicator.setFill(Color.GREY);
        }
        if (this.right) {
            this.dButtonIndicator.setFill(Color.BLUE);
        } else {
            this.dButtonIndicator.setFill(Color.GREY);
        }
    }

    //--------------------------------- UPDATE GUI METHODS (STOP) ---------------------------------//

}