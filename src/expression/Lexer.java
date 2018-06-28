package expression;

import org.jetbrains.annotations.*;

import java.util.*;

public class Lexer
{
    public static final int EOI = 0;
    public static final int SEMICOLON = 1;
    public static final int PLUS = 2;
    public static final int TIMES = 3;
    public static final int LEFT_PARENTHESES = 4;
    public static final int RIGHT_PARENTHESES = 5;
    public static final int NUMBER_OR_IDENTIFIER = 6;
    public static final int UNKNOWN_SYMBOL = 7;
    public static final String END = "end";

    private int lookAhead;
    private String symbolText;
    public int symbolLength;
    private int lineNumber;
    private String inputBuffer;
    private String remainingContents;

    private Scanner input;

    public String getSymbolText()
    {
        return symbolText;
    }

    public int getSymbolLength()
    {
        return symbolLength;
    }

    @Contract(pure = true)
    private String getTokenString()
    {
        String tokenType = "";
        switch (lookAhead)
        {
            case EOI:
                tokenType = "EOI";
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
        symbolText = "";
        symbolLength = 0;
        lineNumber = 0;
        inputBuffer = "";

        remainingContents = "";

        input = new Scanner(System.in);
    }

    public int getLineNumber()
    {
        return lineNumber;
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
                    if (line.equals(END))
                        break;

                    inputBuffer += line;
                }

                if (inputBuffer.isEmpty())
                {
                    remainingContents = "";
                    return EOI;
                }

                lineNumber++;
                remainingContents = inputBuffer;
                remainingContents.trim();
                inputBuffer = "";
            } // while (remainingContents.isEmpty())

            // This branch is unnecessary.
            if (remainingContents.isEmpty())
                return EOI;

            for (int i = 0; i < remainingContents.length(); i++)
            {
                symbolLength = 0;
                symbolText = remainingContents.substring(0, 1);
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
                        i--;
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
                                symbolLength++;
                            }

                            symbolText = remainingContents.substring(0, symbolLength);
                            remainingContents = remainingContents.substring(symbolLength);
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

    public void getNextToken()
    {
        lookAhead = getToken();
    }

    private void runLexer()
    {
        while (!match(EOI))
        {
            System.out.println("Token: " + getTokenString() + ", Symbol: " + getSymbolText());
            getNextToken();
        }
        System.out.println("Token: " + getTokenString() + ", Symbol: " + getSymbolText());
    }

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        lexer.runLexer();
    }
}

