import java.io.Serializable;

/**
 * This ImageObject class is will store a image and will be used to send and receive images.
 *
 * @author Sondre Nerhus
 */
public class ImageObject implements Serializable
{
    private static final long serialVersionUID = 2780459376294108401L;
    private String name = null;
    private long size = 0;
    private byte[] imageBytes = new byte[2^14];
    private String date = null;
    private String filetype = null;

    /**
     * The constructor of the ImageObject class.
     *
     * @param name The name of the image.
     * @param size The size of the image.
     * @param imageBytes The image's byte array.
     * @param date The date the image was taken.
     * @param filetype The image's file type.
     */
    public ImageObject(String name, long size, byte[] imageBytes, String date, String filetype)
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