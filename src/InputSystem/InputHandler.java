package InputSystem;

/**
 * The {@link InputHandler} interface provides a set of methods to retrieve contents from standard input stream or a file.
 * */
public interface InputHandler
{
    void open();
    void close();

    /**
     * Read contents from the given buffer.
     * @return the length of the content actually read.
     * */
    int read(byte[] buffer, int begin, int length);
}
