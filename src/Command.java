import java.io.Serializable;

public class Command implements Serializable
{
    private static final long serialVersionUID = 2780459376294108402L;
    String command;
    int value;
    boolean[] wasd = new boolean[4];

    public Command(String command, int value, boolean[] wasd)
    {
        this.command = command;
        this.value = value;
        this.wasd = wasd;
    }

    public String getCommand()
    {
        return command;
    }

    public int getValue()
    {
        return value;
    }

    public boolean[] getWasd(){
        return wasd;
    }
}