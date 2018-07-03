package InputSystem;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.*;
import java.util.*;

public class Input
{
    public static final int NO_MORE_CHARS = 0;
    public static final int FLUSH_SUCCESS = 1;
    public static final int FLUSH_FAILURE = -1;

    /** The maximum number of characters to look ahead. */
    private static final int MAX_LOOK_AHEAD = 16;

    /** The maximum length of a lex. */
    private static final int MAX_LEX = 1024;

    /** Size of the input buffer of the input system. */
    private static final int BUFFER_SIZE = (MAX_LEX * 3) + (MAX_LOOK_AHEAD * 2);

    /** The start index of the danger zone, which implies the input system to execute the fill buffer operation. */
    private static final int DANGER = (BUFFER_SIZE - MAX_LOOK_AHEAD);

    /** End index of the input buffer. */
    private static final int END = BUFFER_SIZE;

    /** The input buffer. */
    private final byte[] InputBuffer = new byte[BUFFER_SIZE];

    /** Logical end address of the input buffer. */
    private int bufferEnd;

    /** Index of the next character to read. */
    private int nextIndex;

    /** Start Index of the string currently parsed. */
    private int currentLexStartIndex;

    /** End index of the string currently parsed. */
    private int currentLexEndIndex;

    /** Start index of the string previously parsed. */
    private int previousLexStartIndex;

    /** Line number of the string previously parsed. */
    private int previousLexLineNumber;

    /** Length of the string previously parsed. */
    private int previousLexLength;

    /** Line number of the string currently parsed. */
    private int lineNumber;

    private boolean reachEof;

    private InputHandler inputHandler;

    public Input()
    {
        bufferEnd = BUFFER_SIZE;
        nextIndex = END;
        currentLexStartIndex = END;
        currentLexEndIndex = END;
        previousLexStartIndex = END;
        previousLexLineNumber = 0;
        previousLexLength = 0;
        lineNumber = 1;
        reachEof = false;
        inputHandler = null;
    }

    /**
     * Returns a value indicates whether there are contents can be read in the input buffer.
     * @return <code>true</code> if there are contents can be read in the input buffer, <code>false</code> otherwise.
     * */
    @Contract(pure = true)
    private boolean noMoreContents()
    {
        return (reachEof && (nextIndex >= bufferEnd));
    }

    @NotNull
    private InputHandler getInputHandler()
    {
        return new StandardInputHandler();
    }

    @NotNull
    private InputHandler getInputHandler(String fileName)
    {
        return new DiskFileHandler(fileName);
    }

    public void newFile(String fileName)
    {
        if (inputHandler != null)
            inputHandler.close();

        inputHandler = getInputHandler(fileName);
        inputHandler.open();

        reachEof = false;
        nextIndex = END;
        currentLexStartIndex = END;
        currentLexEndIndex = END;
        bufferEnd = END;
        lineNumber = 1;

    }

    public String getCurrentLex()
    {
        byte[] currentLexBytes = Arrays.copyOfRange(InputBuffer, currentLexStartIndex, currentLexEndIndex);
        return new String(currentLexBytes, StandardCharsets.UTF_8);
    }

    public int getCurrentLexLength()
    {
        return currentLexEndIndex - currentLexStartIndex;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getPreviousLex()
    {
        byte[] previousLexBytes = Arrays.copyOfRange(InputBuffer, previousLexStartIndex, previousLexStartIndex + previousLexLength);
        return new String(previousLexBytes, StandardCharsets.UTF_8);
    }

    public int getPreviousLexLength()
    {
        return previousLexLength;
    }

    public int getPreviousLexLineNumber()
    {
        return previousLexLineNumber;
    }

    public int peekNextChar()
    {
        if (currentLexStartIndex >= currentLexEndIndex)
            return -1;
        else
        {
            currentLexStartIndex++;
            return currentLexStartIndex;
        }
    }

    /**
     * Moves to next lex and the previous lex cannot be found in the input buffer.
     * */
    public int moveNext()
    {
        previousLexStartIndex = currentLexStartIndex;
        previousLexLineNumber = lineNumber;
        previousLexLength = currentLexEndIndex - currentLexStartIndex;
        return previousLexStartIndex;
    }

    /**
     * Retrieves and returns the next character in the input buffer.
     * This operation may trigger the flush() operation.
     * */
    public byte nextChar()
    {
        if (noMoreContents())
            return 0;
        else if ((!reachEof) && (flush(false) == FLUSH_FAILURE))
            return -1;
        else if (InputBuffer[nextIndex] == '\n')
            lineNumber++;

        return InputBuffer[nextIndex++];
    }

    /**
     * Flushes the input buffer. If nextIndex < DANGER, nothing will happen.
     * Otherwise, the un-processed contents will be moved and new contents will be filled into the input buffer.
     * */
    private int flush(boolean force)
    {
        if (noMoreContents())
            return NO_MORE_CHARS;
        else if (reachEof)
            return FLUSH_SUCCESS;

        if ((nextIndex >= DANGER) || force)
        {
            int leftEdge = previousLexStartIndex < currentLexStartIndex ? previousLexStartIndex : currentLexStartIndex;
            int shiftAmount = leftEdge;

            if (shiftAmount < MAX_LEX)
            {
                if (!force)
                    return FLUSH_FAILURE;
            }
        }

        return 0;
    }
}
