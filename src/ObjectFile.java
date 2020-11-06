import java.io.Serializable;

public class ObjectFile implements Serializable
{
    private static final long serialVersionUID = 2780459376294108400L;
    private byte[] objFile;
    private String objFileName;
    private String objFileType;

    private byte[] mtlFile;
    private String mtlFileName;
    private String mtlFileType;

    private byte[] pngFile;
    private String pngFileName;
    private String pngFileType;

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