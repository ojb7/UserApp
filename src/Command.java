import java.io.Serializable;
import java.util.List;

/**
 * This Command class is will store a command and will be used to send and receive commands.
 *
 * @author Sondre Nerhus
 */
public class Command implements Serializable
{
    private static final long serialVersionUID = 2780459376294108402L;
    private final String command;
    private final int value;
    private final boolean[] wasd;
    private final List<String> UGVs;

    /**
     * The constructor of the Command class.
     *
     * @param command The command that will read given as a string.
     * @param value A value that can accommodate the command.
     * @param wasd A boolean array representing the W, A, S and D-key from the user.
     * @param UGVs A list of connected UGV's.
     */
    public Command(String command, int value, boolean[] wasd, List<String> UGVs)
    {
        this.command = command;
        this.value = value;
        this.wasd = wasd;
        this.UGVs = UGVs;
    }

    public String getCommand()
    {
        return command;
    }

    public int getValue()
    {
        return value;
    }

    public boolean[] getWasd() { return wasd; }

    public List<String> getUGVs() { return UGVs; }
}