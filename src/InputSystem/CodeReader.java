package InputSystem;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.*;
import java.util.*;

public class CodeReader
{
    public static final int NO_MORE_CHARS = 0;
    public static final int FLUSH_SUCCESS = 1;
    public static final int FLUSH_FAILURE = -1;

    /** The maximum number of characters to look ahead. */
    private static final int MAX_LOOK_AHEAD = 16;

    /** The maximum length of a lex. */
    private static final int MAX_SYMBOL_LENGTH = 1024;

    /** Size of the input buffer of the input system. */
    private static final int BUFFER_SIZE = (MAX_SYMBOL_LENGTH * 3) + (MAX_LOOK_AHEAD * 2);

    /** The start index of the danger zone, which implies the input system to execute the fill buffer operation. */
    private static final int DANGER = (BUFFER_SIZE - MAX_LOOK_AHEAD);

    /** End index of the input buffer. */
    private static final int END = BUFFER_SIZE;

    /** The input buffer. */
    private final char[] InputBuffer = new char[BUFFER_SIZE];

    /** Logical end address of the input buffer. */
    private int bufferEnd;

    /** Index of the next character to read. */
    private int nextCharIndex;

    /** Start Index of the symbol currently parsed. */
    private int currentLexStartIndex;

    /** End index of the symbol currently parsed. */
    private int currentSymbolEndIndex;

    /** Start index of the symbol previously parsed. */
    private int previousSymbolStartIndex;

    /** Line number of the symbol previously parsed. */
    private int previousSymbolLineNumber;

    /** Length of the symbol previously parsed. */
    private int previousSymbolLength;

    /** Line number of the symbol currently parsed. */
    private int currentSymbolLineNumber;

    /** Actually, I don't know the usage of this variable, which makes this variable will probably be removed. */
    private int mLineNumber;

    private boolean hasNext;

    private ICodeInput codeInput;

    public CodeReader()
    {
        bufferEnd = BUFFER_SIZE;
        nextCharIndex = END;
        currentLexStartIndex = END;
        currentSymbolEndIndex = END;
        previousSymbolStartIndex = END;
        previousSymbolLineNumber = 0;
        previousSymbolLength = 0;
        currentSymbolLineNumber = 1;
        mLineNumber = 1;
        hasNext = false;
        codeInput = null;
    }

    /**
     * Returns a value indicates whether there are contents can be read in the input buffer.
     * @return <code>true</code> if there are contents can be read in the input buffer, <code>false</code> otherwise.
     * */
    @Contract(pure = true)
    private boolean noMoreContents()
    {
        return (hasNext && (nextCharIndex >= bufferEnd));
    }

    @NotNull
    private ICodeInput getCodeInput()
    {
        return new StdinCodeInput();
    }

    @NotNull
    private ICodeInput getInputHandler(String fileName) throws IOException
    {
//        return new FileCodeInput(fileName);
        return null;
    }

    public void newFile(String fileName) throws IOException
    {
        if (codeInput != null)
            codeInput.close();

        codeInput = getInputHandler(fileName);
        codeInput.open();

        hasNext = false;
        nextCharIndex = END;
        currentLexStartIndex = END;
        currentSymbolEndIndex = END;
        bufferEnd = END;
        currentSymbolLineNumber = 1;
        mLineNumber = 1;
    }

    public String getCurrentLex()
    {
//        byte[] currentLexBytes = Arrays.copyOfRange(InputBuffer, currentLexStartIndex, currentSymbolEndIndex);
//        return new String(currentLexBytes, StandardCharsets.UTF_8);
        return "";
    }

    public int getCurrentLexLength()
    {
        return currentSymbolEndIndex - currentLexStartIndex;
    }

    public int getCurrentSymbolLineNumber()
    {
        return currentSymbolLineNumber;
    }

    public String getPreviousLex()
    {
//        byte[] previousLexBytes = Arrays.copyOfRange(InputBuffer, previousSymbolStartIndex, previousSymbolStartIndex + previousSymbolLength);
//        return new String(previousLexBytes, StandardCharsets.UTF_8);
        return "";
    }

    public int markStart()
    {
        mLineNumber = currentSymbolLineNumber;
        currentLexStartIndex = nextCharIndex;
        currentSymbolEndIndex = nextCharIndex;
        return currentLexStartIndex;
    }

    public int markEnd()
    {
        mLineNumber = currentSymbolLineNumber;
        currentSymbolEndIndex = nextCharIndex;
        return currentSymbolEndIndex;
    }

    public int moveStart()
    {
        if (currentLexStartIndex >= currentSymbolEndIndex)
            return -1;
        else
        {
            currentLexStartIndex++;
            return currentLexStartIndex;
        }
    }

    public int toMark()
    {
        currentSymbolLineNumber = mLineNumber;
        nextCharIndex = currentSymbolEndIndex;
        return nextCharIndex;
    }

    public int getPreviousSymbolLength()
    {
        return previousSymbolLength;
    }

    public int getPreviousSymbolLineNumber()
    {
        return previousSymbolLineNumber;
    }

    public int peekNextChar()
    {
        if (currentLexStartIndex >= currentSymbolEndIndex)
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
        previousSymbolStartIndex = currentLexStartIndex;
        previousSymbolLineNumber = currentSymbolLineNumber;
        previousSymbolLength = currentSymbolEndIndex - currentLexStartIndex;
        return previousSymbolStartIndex;
    }

    /**
     * Retrieves and returns the next character in the input buffer.
     * This operation may trigger the flush() operation.
     * */
    public byte nextChar()
    {
        if (noMoreContents())
            return 0;
        else if ((!hasNext) && (flush(false) == FLUSH_FAILURE))
            return -1;
        else if (InputBuffer[nextCharIndex] == '\n')
            currentSymbolLineNumber++;

//        return InputBuffer[nextCharIndex++];
        return 0;
    }

    /**
     * Flushes the input buffer. If nextCharIndex < DANGER, nothing will happen.
     * Otherwise, the un-processed contents will be moved and new contents will be filled into the input buffer.
     * */
    private int flush(boolean force)
    {
        if (noMoreContents())
            return NO_MORE_CHARS;
        else if (hasNext)
            return FLUSH_SUCCESS;

        if ((nextCharIndex >= DANGER) || force)
        {
            int shiftAmount = previousSymbolStartIndex < currentLexStartIndex ? previousSymbolStartIndex : currentLexStartIndex;

            if (shiftAmount < MAX_SYMBOL_LENGTH)
            {
                if (!force)
                    return FLUSH_FAILURE;

                moveNext();
                shiftAmount = markStart();
            }

            int copyAmount = bufferEnd - shiftAmount;
            System.arraycopy(InputBuffer, 0, InputBuffer, shiftAmount, copyAmount);

            if (fillBuffer(copyAmount) == 0)
                System.err.println("Internal error (flush): CodeReader buffer is full, cannot read.");

            if (previousSymbolStartIndex != 0)
                previousSymbolStartIndex -= shiftAmount;

            currentLexStartIndex -= shiftAmount;
            currentSymbolEndIndex -= shiftAmount;
            nextCharIndex -= shiftAmount;
        }

        return FLUSH_SUCCESS;
    }

    /**
     * Reads contents from input stream and fills input buffer.
     * @return the length of the contents actually read.
     * */
    private int fillBuffer(int startIndex)
    {
        int need = ((END - startIndex) / MAX_SYMBOL_LENGTH) * MAX_SYMBOL_LENGTH;
        if (need < 0)
            System.err.println("Internal error (fillBuffer): Bad read-request start index.");

        if (need == 0)
            return 0;

//        int addedLength = codeInput.read(InputBuffer, startIndex, need);
//        if (addedLength == -1)
//            System.err.println("Cannot read input contents.");
//
//        bufferEnd = startIndex + addedLength;
//        if (addedLength < need)
//            hasNext = true;

//        return addedLength;
        return 0;
    }
}
