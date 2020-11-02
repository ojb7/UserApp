import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {
    private static final long serialVersionUID = 2780459376294108402L;
    String command;
    int value;
    boolean[] wasd;
    List<String> UGVs;

    public Command(String command, int value, boolean[] wasd, List<String> UGVs) {
        this.command = command;
        this.value = value;
        this.wasd = wasd;
        this.UGVs = UGVs;
    }

    public String getCommand() {
        return command;
    }

    public int getValue() {
        return value;
    }

    public boolean[] getWasd() {
        return wasd;
    }

    public List<String> getUGVs() {
        return UGVs;
    }
}