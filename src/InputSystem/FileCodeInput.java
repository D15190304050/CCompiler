package InputSystem;

import java.io.*;

public class FileCodeInput implements ICodeInput
{
    private String inputBuffer;
    private int currentPosition;

    public FileCodeInput()
    {
        currentPosition = 0;
    }

    @Override
    public void open()
    {

    }

    @Override
    public void close()
    {

    }

    /**
     * Read contents from the given buffer.
     *
     * @param buffer
     * @param begin
     * @param length
     * @return the length of the content actually read.
     */
    @Override
    public int read(byte[] buffer, int begin, int length)
    {
        return 0;
    }
}
