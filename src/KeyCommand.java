public class KeyCommand extends Command{
    public final boolean forward;
    public  final boolean left;
    public final boolean backwards;
    public final boolean right;

    public KeyCommand(boolean forward, boolean left, boolean backwards, boolean right) {
        this.forward = forward;
        this.left = left;
        this.backwards = backwards;
        this.right = right;
    }
}
