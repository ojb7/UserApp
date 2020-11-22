
import java.io.Serializable;

/**
 * This ObjectFile class is will store the 3D-model and will be used to send the model to the user.
 *
 * @author Sondre Nerhus
 */
public class ObjectFile implements Serializable
{
    private static final long serialVersionUID = 2780459376294108400L;

    private final byte[] objFile;
    private final String objFileName;
    private final String objFileType;

    private final byte[] mtlFile;
    private final String mtlFileName;
    private final String mtlFileType;

    private final byte[] pngFile;
    private final String pngFileName;
    private final String pngFileType;

    /**
     * The constructor of the ObjectFile class.
     *
     * @param objFile The byte array of the object file for the model.
     * @param objFileName The name of the object file for the model.
     * @param objFileType The file type of the object file for the model.
     * @param mtlFile The byte array of the MTL file for the model.
     * @param mtlFileName The name of the MTL file for the model.
     * @param mtlFileType The file type of the MTL file for the model.
     * @param pngFile The byte array of the PNG file for the model.
     * @param pngFileName The name of the PNG file for the model.
     * @param pngFileType The file type of the PNG file for the model.
     */
    public ObjectFile(byte[] objFile, String objFileName, String objFileType, byte[] mtlFile, String mtlFileName, String mtlFileType, byte[] pngFile, String pngFileName, String pngFileType)
    {
        this.objFile = objFile;
        this.objFileName = objFileName;
        this.objFileType = objFileType;

        this.mtlFile = mtlFile;
        this.mtlFileName = mtlFileName;
        this.mtlFileType = mtlFileType;

        this.pngFile = pngFile;
        this.pngFileName = pngFileName;
        this.pngFileType = pngFileType;
    }

    public byte[] getObjFile() {
        return objFile;
    }

    public String getObjFileName() {
        return objFileName;
    }

    public String getObjFileType() {
        return objFileType;
    }

    public byte[] getMtlFile() {
        return mtlFile;
    }

    public String getMtlFileName() {
        return mtlFileName;
    }

    public String getMtlFileType() {
        return mtlFileType;
    }

    public byte[] getPngFile() {
        return pngFile;
    }

    public String getPngFileName() {
        return pngFileName;
    }

    public String getPngFileType() {
        return pngFileType;
    }
}