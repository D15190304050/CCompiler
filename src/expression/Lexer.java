package expression;

import org.jetbrains.annotations.*;
import java.util.*;

public class Lexer
{
    public static final int EOF = 0;
    public static final int SEMICOLON = 1;
    public static final int PLUS = 2;
    public static final int TIMES = 3;
    public static final int LEFT_PARENTHESES = 4;
    public static final int RIGHT_PARENTHESES = 5;
    public static final int NUMBER_OR_IDENTIFIER = 6;
    public static final int UNKNOWN_SYMBOL = 7;
    public static final String END = "end";

    private int lookAhead;
    private String tokenText;
    public int tokenLength;
    public int lineNumber;
    private String inputBuffer;
    private String remainingContents;

    private Scanner input;

    public String getTokenText()
    {
        return tokenText;
    }

    public int getTokenLength()
    {
        return tokenLength;
    }

    @Contract(pure = true)
    private String getTokenString()
    {
        String tokenType = "";
        switch (lookAhead)
        {
            case EOF:
                tokenType = "EOF";
                break;
            case PLUS:
                tokenType = "PLUS";
                break;
            case TIMES:
                tokenType = "TIMES";
                break;
            case NUMBER_OR_IDENTIFIER:
                tokenType = "NUMBER_OR_IDENTIFIER";
                break;
            case SEMICOLON:
                tokenType = "SEMICOLON";
                break;
            case LEFT_PARENTHESES:
                tokenType = "LEFT_PARENTHESES";
                break;
            case RIGHT_PARENTHESES:
                tokenType = "RIGHT_PARENTHESES";
                break;
        }

        return tokenType;
    }

    public Lexer()
    {
        lookAhead = -1;
        tokenText = "";
        tokenLength = 0;
        lineNumber = 0;
        inputBuffer = "";

        remainingContents = "";

        input = new Scanner(System.in);
    }

    private boolean isAlphaOrNumber(char c)
    {
        return ((Character.isAlphabetic(c)) || (Character.isDigit(c)));
    }

    private int getToken()
    {
        for (; ; )
        {
            while (remainingContents.isEmpty())
            {
                for (; ; )
                {
                    String line = input.nextLine();
                    if (line.equals(END)) break;

                    inputBuffer += line;
                }

                if (inputBuffer.isEmpty())
                {
                    remainingContents = "";
                    return EOF;
                }

                lineNumber++;
                remainingContents = inputBuffer;
                remainingContents.trim();
            } // while (remainingContents.isEmpty())

            // This branch is unnecessary.
            if (remainingContents.isEmpty()) return EOF;

            for (int i = 0; i < remainingContents.length(); i++)
            {
                tokenLength = 0;
                tokenText = remainingContents.substring(0, 1);
                char nextChar = remainingContents.charAt(i);

                switch (nextChar)
                {
                    case ';':
                        remainingContents = remainingContents.substring(1);
                        return SEMICOLON;
                    case '+':
                        remainingContents = remainingContents.substring(1);
                        return PLUS;
                    case '*':
                        remainingContents = remainingContents.substring(1);
                        return TIMES;
                    case '(':
                        remainingContents = remainingContents.substring(1);
                        return LEFT_PARENTHESES;
                    case ')':
                        remainingContents = remainingContents.substring(1);
                        return RIGHT_PARENTHESES;

                    case '\n':
                    case '\t':
                    case ' ':
                        remainingContents = remainingContents.substring(1);
                        break;

                    default:
                        if (!isAlphaOrNumber(nextChar))
                            return UNKNOWN_SYMBOL;
                        else
                        {
                            while ((i < remainingContents.length()) && isAlphaOrNumber(remainingContents.charAt(i)))
                            {
                                i++;
                                tokenLength++;
                            }

                            tokenText = remainingContents.substring(0, tokenLength);
                            remainingContents = remainingContents.substring(tokenLength);
                            return NUMBER_OR_IDENTIFIER;
                        }
                }
            }
        }
    }

    public boolean match(int token)
    {
        if (lookAhead == -1)
            lookAhead = getToken();

        return token == lookAhead;
    }

    private void getNextToken()
    {
        lookAhead = getToken();
    }

    private void runLexer()
    {
        while (!match(EOF))
        {
            System.out.println("Token: " + getTokenString() + ", Symbol: " + tokenText);
            getNextToken();
        }
    }

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        lexer.runLexer();
    }
}

