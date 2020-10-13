import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient implements Runnable{
    private Socket connection;
    private ObjectOutputStream toServer;
    private BufferedReader fromServer;
    private String lastError = null;
    private String userId = null;


    /**
     *
     * @param host The IP adress of the host
     * @param port The port at the host
     * @return Returns true if connection is aestablished
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
            this.fromServer = new BufferedReader(new InputStreamReader(in));
            System.out.println("Connection established!");
            return true;
        } catch (UnknownHostException e) {
            this.lastError = "Could not connect to host...";
            System.err.println(this.lastError);
            return false;
        } catch (ConnectException e) {
            this.lastError = "Could not connect to port...";
            System.err.println(this.lastError);
            return false;
        } catch (IOException e) {
            this.lastError = "An I/O error occurred...";
            System.err.println(this.lastError);
            return false;
        }
    }

    /**
     * Disconnect from server
     */
    public synchronized void disconnect() {
        if (this.connection != null) {
            System.out.println("Disconnecting...");

            try {
                this.toServer.close();
                this.fromServer.close();
                this.connection.close();
            } catch (IOException e) {
                System.out.println("Error disconnecting: " + e
                        .getMessage());
                this.lastError = e.getMessage();
                this.connection = null;
            }
        } else {
            System.out.println("No connection...");
        }
        System.out.println("Disconnected succesfully!");
        this.connection = null;
    }

    /**
     * Checks if the connection to the server is active
     * @return Returns true if connection is active
     */
    public boolean isConnectionActive() {
        return (this.connection != null);
    }

    /**
     * Sends a command to the UGV server
     * @param cmd Command to be sent to server
     */
    public void sendCommand(Command cmd){
        try {
            this.toServer.writeObject(cmd);
            System.out.println("Sending command: " + cmd);
        } catch (IOException e) {
            System.out.println("Could not write object.");
        }
    }

    @Override
    public void run() {

    }
}