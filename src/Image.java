import java.io.Serializable;

public class Image implements Serializable {
    private static final long serialVersionUID = 2780459376294108401L;
    private String name = null;
    private long size = 0;
    private byte[] imageBytes = new byte[2^14];
    private String date = null;
    private String filetype = null;

    public Image(String name, long size, byte[] imageBytes, String date, String filetype)
    {
        this.name = name;
        this.size = size;
        this.imageBytes = imageBytes;
        this.date = date;
        this.filetype = filetype;
    }

    public String getName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }

    public byte[] getImageBytes()
    {
        return imageBytes;
    }

    public String getDate()
    {
        return date;
    }

    public String getFiletype()
    {
        return filetype;
    }
}
