import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    private Socket connection;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    // Ping variables
    private long timeSent;
    private long timeReceived;
    private long lastPing = 0;


    /**
     * @param host The IP address of the host
     * @param port The port at the host
     * @return Returns true if connection is established
     */
    public boolean connect(String host, int port) {
        System.out.println("Trying to connect...");
        try {
            // Tries to connect to the given host and port
            this.connection = new Socket(host, port);
            // Opens input stream from server
            InputStream in = this.connection.getInputStream();
            // Opens output stream to the server
            OutputStream out = this.connection.getOutputStream();
            this.toServer = new ObjectOutputStream(out);
            this.fromServer = new ObjectInputStream(in);
            System.out.println("Connection established!");
            return true;
        } catch (UnknownHostException e) {
            System.err.println("Could not connect to host...");
            return false;
        } catch (ConnectException e) {
            System.err.println("Could not connect to port...");
            return false;
        } catch (IOException e) {
            System.err.println("An I/O error occurred...");
            return false;
        }
    }

    /**
     * Disconnect from server
     */
    public synchronized void disconnect() {
        System.out.println("Trying to disconnect...");
        if (isConnectionActive()) {
            try {
                this.toServer.close();
                this.fromServer.close();
                this.connection.close();
                System.out.println("Disconnected succesfully!");
            } catch (IOException e) {
                System.err.println("Error disconnecting: " + e.getMessage());
                this.connection = null;
            }
        } else {
            System.err.println("No connection to disconnect...");
        }
        this.connection = null;
    }

    /**
     * Checks if the connection to the server is active
     *
     * @return Returns true if connection is active
     */
    public boolean isConnectionActive() {
        return (this.connection != null);
    }

    /**
     * Tries to send a command to the UGV server
     *
     * @param cmd Command to be sent to server
     */
    private synchronized void sendCommand(Command cmd) {
        if (isConnectionActive()) {
            try {
                this.toServer.writeObject(cmd);
                this.toServer.flush();
                System.out.println(">>>S Succesfully sent command: " + cmd.getCommand() + " to server!");
                this.timeSent = System.currentTimeMillis();

            } catch (IOException e) {
                System.err.println("Could not write object...");
            }
        } else {
            System.err.println("No connection...");
        }
    }


    /**
     * Tries to receive an Object from server
     *
     * @return Returns Object received from server
     */
    private synchronized Object receiveObject() {
        Object objectFromServer = null;
        if (isConnectionActive()) {
            try {
                objectFromServer = this.fromServer.readObject();
                System.out.println("<<<R Succesfully received object from server!");
                this.timeReceived = System.currentTimeMillis();
            } catch (IOException e) {
                System.err.println("An I/O error has occurred while receiving server commands, socket could be closed...");
                disconnect();
            } catch (ClassNotFoundException e) {
                System.err.println("An Class Not Found Exception occurred: " + e.getMessage());
                disconnect();
            }
        } else {
            System.err.println("Cannot receive object because there is no connection...");
        }
        return objectFromServer;
    }

    /**
     *
     * @return
     */
    public long getPing(){
        long ping = this.timeReceived - this.timeSent;

        if(ping > 0) {
            this.lastPing = ping;
        }
        return this.lastPing;
    }


    //---------------------- Server Protocol Start ----------------------//

    public void askUgvListFromServer() {
        Command cmd = new Command("updateUGVList", 0, null, null);
        sendCommand(cmd);
    }

    public void askServerImageFromServer() {
        Command cmd = new Command("updateServerImage", 0, null, null);
        sendCommand(cmd);
    }

    public void askUgvImageFromServer() {
        Command cmd = new Command("updateUGVImage", 0, null, null);
        sendCommand(cmd);
    }

    public void setUserStateToServer() {
        Command cmd = new Command("User", 0, null, null);
        sendCommand(cmd);
    }

    public void setAutoOnToServer(int numberOfImages) {
        Command cmd = new Command("start", numberOfImages, null, null);
        sendCommand(cmd);
    }

    public void setAutoOffToServer() {
        Command cmd = new Command("stop", 0, null, null);
        sendCommand(cmd);
    }

    public void setManualDirectionsToServer(boolean[] wasd, int speed) {
        Command cmd = new Command("manual", speed, wasd, null);
        sendCommand(cmd);
    }

    public void setManualOnToServer() {
        boolean[] wasd = new boolean[4];
        Command cmd = new Command("manual", 0, wasd, null);
        sendCommand(cmd);
    }

    public void setManualOffToServer() {
        boolean[] wasd = new boolean[4];
        Command cmd = new Command("manualStop", 0, wasd, null);
        sendCommand(cmd);
    }

    public void setUgvIdToServer(int ugvId) {
        Command cmd = new Command("UGVSelected", ugvId, null, null);
        sendCommand(cmd);
    }

    public void askObjectFile() {
        Command cmd = new Command("updateObjectFile", 0, null, null);
        sendCommand(cmd);
    }

    public void askServerProgress() {
        Command cmd = new Command("updateProgress", 0, null, null);
        sendCommand(cmd);
    }


    /**
     * Tries to receive an ObjectFile from server
     * @return Returns the ObjectFile from server
     */
    public ObjectFile receiveObjectFile() {
        Object objectFromServer = receiveObject();
        // Instance of ImageObject from server
        ObjectFile objectFileFromServer = null;
        if (objectFromServer instanceof ObjectFile) {
            objectFileFromServer = (ObjectFile) objectFromServer;
        }
        return objectFileFromServer;
    }

    /**
     * Tries to receive an ImageObject from server
     * @return Returns the ImageObject from server
     */
    public ImageObject receiveImageObject() {
        Object objectFromServer = receiveObject();
        ImageObject imageObjectFromServer = null;
        if (objectFromServer instanceof ImageObject) {
            imageObjectFromServer = (ImageObject) objectFromServer;
        }
        return imageObjectFromServer;
    }

    /**
     * Tries to receive an Command from server
     * @return Returns the Command from server
     */
    public Command receiveCommand() {
        Object objectFromServer = receiveObject();
        Command commandFromServer = null;
        if (objectFromServer instanceof Command) {
            commandFromServer = (Command) objectFromServer;
        }
        return commandFromServer;
    }

    //---------------------- Server Protocol End ----------------------//

}