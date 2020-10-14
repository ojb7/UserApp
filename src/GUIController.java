
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.io.IOException;



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

    private TCPClient tcpClient;

    // UGV movement directions
    private boolean forward = false;
    private boolean backward = false;
    private boolean right = false;
    private boolean left = false;
    private boolean forward0 = false;
    private boolean backward0 = false;
    private boolean right0 = false;
    private boolean left0 = false;

    private static final String HOST = "10.22.192.92";
    private static final String HOST_STASJ = "83.243.240.94";
    private static final String PORT = "42069";

    @FXML
    public void initialize() {
        this.hostField.setText(HOST_STASJ);
        this.portField.setText(PORT);
        this.disconnectButton.setDisable(true);
        this.hostField.setDisable(false);
        this.portField.setDisable(false);

        System.out.println("Initialize GUI controller...");
    }


    @FXML
    private void handleKeyPressed(KeyEvent event){
        String keyPressed = event.getText();
        if(keyPressed.equalsIgnoreCase("W")) this.forward = true;
        if(keyPressed.equalsIgnoreCase("A")) this.left = true;
        if(keyPressed.equalsIgnoreCase("S")) this.backward = true;
        if(keyPressed.equalsIgnoreCase("D")) this.right = true;
        System.out.println("Key pressed: " + keyPressed);
        //sendKeyCommand(this.forward, this.left, this.backward, this.right);
        updateWasdInGui(this.forward, this.left, this.backward, this.right);
    }

    @FXML
    private void handleKeyReleased(KeyEvent event){
        String keyReleased = event.getText();
        if(keyReleased.equalsIgnoreCase("W")) this.forward = false;
        if(keyReleased.equalsIgnoreCase("A")) this.left = false;
        if(keyReleased.equalsIgnoreCase("S")) this.backward = false;
        if(keyReleased.equalsIgnoreCase("D")) this.right = false;
        System.out.println("Key released: " + keyReleased);
        //sendKeyCommand(this.forward, this.left, this.backward, this.right);
        updateWasdInGui(this.forward, this.left, this.backward, this.right);
    }

    private void updateWasdInGui(boolean w, boolean a, boolean s, boolean d){
        if(w){
            wButtonIndicator.setFill(Color.BLUE);
            System.out.println("Blue");
        } else{
            wButtonIndicator.setFill(Color.GREY);
            System.out.println("Grey");
        }
        if(a){
            aButtonIndicator.setFill(Color.BLUE);
            System.out.println("Blue");
        } else{
            aButtonIndicator.setFill(Color.GREY);
            System.out.println("Grey");
        }
        if(s){
            sButtonIndicator.setFill(Color.BLUE);
            System.out.println("Blue");
        } else{
            sButtonIndicator.setFill(Color.GREY);
            System.out.println("Grey");
        }
        if(d){
            dButtonIndicator.setFill(Color.BLUE);
            System.out.println("Blue");
        } else{
            dButtonIndicator.setFill(Color.GREY);
            System.out.println("Grey");
        }
    }

    @FXML
    private void speedValueControl(DragEvent event){
        int speed = (int)speedValue.getValue();
        System.out.println("Speed value: " + speed);
    }

    private void sendKeyCommand(boolean forward, boolean left, boolean backward, boolean right) {
        boolean change = false;
        if(this.forward0 != forward) change = true;
        if(this.left0 != left) change = true;
        if(this.backward0 != backward) change = true;
        if(this.right0 != right) change = true;
        this.forward0 = forward;
        this.left0 = left;
        this.backward0 = backward;
        this.right = right;

        if (this.tcpClient.isConnectionActive() && change) {
            Command cmd = new KeyCommand(forward, left, backward, right);
            this.tcpClient.sendCommand(cmd);
        }
    }


    @FXML
    void startConnection(ActionEvent event) {
        this.tcpClient = new TCPClient();
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
        //Command cmd = new Command("Start");
        //this.tcpClient.sendCommand(cmd);
    }

    @FXML
    void stopUgv(ActionEvent event) {
        System.out.println("Sending: stop");
        //Command cmd = new Command("Stop");
        //this.tcpClient.sendCommand(cmd);
    }

    @FXML
    void disconnect(ActionEvent event) {
        this.tcpClient.disconnect();
        if(!this.tcpClient.isConnectionActive()){
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
        if(connected){
            this.connectButton.setText("Connected!");
            this.connectButton.setTextFill(Color.GREEN);
        }
    }



}
