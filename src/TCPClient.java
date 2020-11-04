import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    private Socket connection;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

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
     * Sends a command to the UGV server
     *
     * @param cmd Command to be sent to server
     */
    public synchronized void sendCommand(Command cmd) {
        System.out.println(">>> Trying to send command: " + cmd.getCommand());
        if (isConnectionActive()) {
            try {
                this.toServer.writeObject(cmd);
                System.out.println("Succesfully sent command!");
            } catch (IOException e) {
                System.err.println("Could not write object...");
            }
        } else {
            System.err.println("No connection...");
        }
    }



    public synchronized Object receiveObject() {
        System.out.println("Trying to receive object from server...");
        Object objectFromServer = null;
        if (isConnectionActive()) {
            try {
                objectFromServer = this.fromServer.readObject();
                System.out.println("Received object from server!");
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



    //---------------------- Server Protocol Start ----------------------//

    public void askUgvListFromServer(){
        Command cmd = new Command("updateUGVList", 0, null, null);
        sendCommand(cmd);
    }

    public void askServerImageFromServer(){
        Command cmd = new Command("updateServerImage", 0, null, null);
        sendCommand(cmd);
    }

    public void askUgvImageFromServer(){
        Command cmd = new Command("updateUGVImage", 0, null, null);
        sendCommand(cmd);
    }

    public void setUserStateToServer(){
        Command cmd = new Command("User", 0, null, null);
        sendCommand(cmd);
    }

    public void setAutoOnToServer(int numberOfImages){
        Command cmd = new Command("start", numberOfImages, null, null);
        sendCommand(cmd);
    }

    public void setAutoOffToServer(){
        Command cmd = new Command("stop", 0, null, null);
        sendCommand(cmd);
    }


    public void setManualDirectionsToServer(boolean[] wasd, int speed){
        Command cmd = new Command("manual", speed, wasd, null);
        sendCommand(cmd);

    }

    public void setManualOnToServer(){
        boolean[] wasd = new boolean[4];
        Command cmd = new Command("manual", 0, wasd, null);
        sendCommand(cmd);
    }

    public void setManualOffToServer(){
        Command cmd = new Command("manualStop", 0, null, null);
        sendCommand(cmd);
    }


    public void setUgvIdToServer(int ugvId){
        Command cmd = new Command("UGVSelected", ugvId, null, null);
        sendCommand(cmd);
    }

    //---------------------- Server Protocol End ----------------------//

}