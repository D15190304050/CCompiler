package expression;

import java.io.*;
import java.util.*;

/**
 *  The {@link Lexer} class represents a simple lexer that can recognize a arithmetic expression consisting of numbers,
 *  operators such as "+", "-", "*", "/" and parentheses which ends with ";". It can extract symbol from the input
 *  expression end with ";" and gets the token of the symbol.
 * */
public class Lexer
{
    /** The symbol that indicates "end of file". */
    public static final String END_SYMBOL = "end";

    /** Next token to analyze. */
    private TokenType lookAhead;

    /** The text of the symbol. */
    private String symbolText;

    /** Current line number. */
    private int lineNumber;

    /** Remaining contents in this line to analyze. */
    private String remainingContents;

    /** CodeReader of this {@link Lexer}. */
    private Scanner input;

    /**
     * Initializes a new instance of {@link Lexer} with standard input stream.
     * */
    public Lexer()
    {
        this(System.in);
    }

    /**
     * Initializes a new instance of {@link Lexer} with the specified input stream.
     * @param input The specified input stream.
     * */
    public Lexer(InputStream input)
    {
        lookAhead = TokenType.INITIAL_STATE;
        symbolText = "";
        lineNumber = 0;
        remainingContents = "";
        this.input = new Scanner(input);
    }

    /**
     * Gets the text of the symbol.
     * @return The text of the symbol.
     * */
    public String getSymbolText()
    {
        return symbolText;
    }

    /**
     * Gets the number of current line being analyzed.
     * @return The number of current line being analyzed.
     * */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Returns a value indicating whether the given character is an alphabetic or number.
     * @return true if the given character is an alphabetic or number, otherwise, false.
     * */
    public boolean isAlphabeticOrNumber(char c)
    {
        return ((Character.isAlphabetic(c)) || (Character.isDigit(c)));
    }

    /**
     * Reads next non-empty line from input stream. Any empty line or line contains only white spaces will be filtered.
     * */
    private void readLine()
    {
        // Read contents from input.
        for (;;)
        {
            // Read next line and trim it.
            String line = input.nextLine().trim();

            // Filter all empty lines.
            if (!line.isEmpty())
            {
                remainingContents = line;
                lineNumber++;
                break;
            }
        }
    }

    /**
     * Extracts the number or identifier from the input stream and store it in the "symbolText" variable.
     * */
    private void getNumberOrIdentifier()
    {
        // Since when this method is called, remainingContents.charAt(0) is confirmed to be alphabetic or number.
        // So variable symbolLength starts from 1.
        int symbolLength = 1;
        while ((symbolLength < remainingContents.length()) &&
                isAlphabeticOrNumber(remainingContents.charAt(symbolLength)))
            symbolLength++;

        symbolText = remainingContents.substring(0, symbolLength);
        remainingContents = remainingContents.substring(symbolLength);
    }

    /**
     * Gets and returns the token of next symbol in the input stream.
     * */
    private TokenType getNextToken()
    {
        // Filter all the white spaces.
        remainingContents = remainingContents.trim();

        // Read new line if remainingContents is empty.
        if (remainingContents.isEmpty())
            readLine();

        // Handle the "end of file" symbol.
        if (remainingContents.equals(END_SYMBOL))
        {
            symbolText = END_SYMBOL;
            return TokenType.END_OF_FILE;
        }

        // Get next character to analyze.
        char nextChar = remainingContents.charAt(0);
        symbolText = remainingContents.substring(0, 1);
        switch (nextChar)
        {
            case ';':
                remainingContents = remainingContents.substring(1);
                return TokenType.SEMICOLON;
            case '+':
                remainingContents = remainingContents.substring(1);
                return TokenType.PLUS;
            case '-':
                remainingContents = remainingContents.substring(1);
                return TokenType.MINUS;
            case '*':
                remainingContents = remainingContents.substring(1);
                return TokenType.TIMES;
            case '/':
                remainingContents = remainingContents.substring(1);
                return TokenType.DIVIDE;
            case '(':
                remainingContents = remainingContents.substring(1);
                return TokenType.LEFT_PARENTHESES;
            case ')':
                remainingContents = remainingContents.substring(1);
                return TokenType.RIGHT_PARENTHESES;
            default:
                if (!isAlphabeticOrNumber(nextChar))
                    return TokenType.UNKNOWN_SYMBOL;
                else
                {
                    getNumberOrIdentifier();
                    return TokenType.NUMBER_OR_IDENTIFIER;
                }
        }
    }

    /**
     * Gets a value indicating whether the current token in the input stream equals the specified token.
     * @param token The specified token.
     * @return true if the next token in the input stream equals the given specified, otherwise, false.
     * */
    public boolean match(TokenType token)
    {
        if (lookAhead == TokenType.INITIAL_STATE)
            lookAhead = getNextToken();

        return token == lookAhead;
    }

    /**
     * Closes the input stream.
     * */
    public void close()
    {
        input.close();
    }

    /**
     * Clears contents in the line being analyzed currently.
     * */
    public void clearLine()
    {
        remainingContents = "";
        lookAhead = TokenType.INITIAL_STATE;
    }

    /**
     * Moves to next token in the input stream.
     * */
    public void lookNextToken()
    {
        lookAhead = getNextToken();
    }

    /**
     * A unit test method to tokenize all lines in the input stream.
     * */
    private void runLexer()
    {
        // Manually call the readLine() method so that the entire non-empty line can be shown in the console.
        readLine();
        System.out.println("Line number: " + lineNumber + ", content: " + remainingContents);

        while (!match(TokenType.END_OF_FILE))
        {
            if (match(TokenType.UNKNOWN_SYMBOL))
            {
                System.out.println("Error: unknown token: " + symbolText + " at line: " + lineNumber + ".");
                remainingContents = "";
            }
            else
                System.out.println("Token: " + lookAhead + ", Symbol: " + getSymbolText());

            if (remainingContents.isEmpty())
            {
                // Put a blank line if a non-empty line is completely tokenized.
                System.out.println();

                // Manually call the readLine() method so that the entire non-empty line can be shown in the console.
                readLine();
                System.out.println("Line number: " + lineNumber + ", content: " + remainingContents);
            }

            lookNextToken();
        }

        System.out.println("Token: " + lookAhead + ", Symbol: " + getSymbolText());
        input.close();
    }

    /**
     * A unit test method for the {@link Lexer} class.
     * */
    public static void main(String[] args) throws IOException
    {
        String sourceFilePath = "./out/production/CCompiler/expression/lexerTest.txt";
        FileInputStream sourceFile = new FileInputStream(sourceFilePath);
        Lexer lexer = new Lexer(sourceFile);
        lexer.runLexer();
        sourceFile.close();
    }
}
