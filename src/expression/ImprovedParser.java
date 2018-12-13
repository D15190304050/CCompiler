package expression;

import java.io.*;

/**
 * The {@link ImprovedParser} class represents an improved version of {@link BasicParser} by changing recursive call
 * into loop.
 * */
public class ImprovedParser
{
    /** The lexer to tokenize text in the input stream. */
    private Lexer lexer;

    /** A boolean value indicating whether the statements is legal. */
    private boolean isLegalStatement;

    /**
     * Initializes a new instance of {@link ImprovedParser} with the specified {@link Lexer}.
     * @param lexer The specified {@link Lexer}.
     * */
    public ImprovedParser(Lexer lexer)
    {
        isLegalStatement = true;
        this.lexer = lexer;
    }

    /**
     * Parses a "statements" with following production.
     * <p>
     * statements -> expression ; | expression ; statements
     * <p/>
     * */
    @SuppressWarnings("Duplicates")
    public void statements()
    {
        while (!lexer.match(TokenType.END_OF_FILE))
        {
            expression();

            // If the next token following semicolon is not END_OF_FILE, then use the parse rule on the right.
            // Else if the expression contains unknown symbol or unsupported operator, then it is an illegal statements.
            // Else => the expression doesn't end with a semicolon, it is an illegal statements.
            if (lexer.match(TokenType.SEMICOLON))
            {
                lexer.lookNextToken();
                System.out.println("The statement is " + (isLegalStatement ? "legal" : "illegal"));
                isLegalStatement = true;
            }
            else if (lexer.match(TokenType.UNKNOWN_SYMBOL) ||
                     lexer.match(TokenType.MINUS) ||
                     lexer.match(TokenType.DIVIDE))
            {
                System.out.println("Error: unknown symbol: " + lexer.getSymbolText() + " at line: " + lexer.getLineNumber() + ".");
                lexer.clearLine();
            }
            else
            {
                isLegalStatement = false;
                System.out.println("Line " + lexer.getLineNumber() + ": missing \";\".");
                lexer.clearLine();
            }
        }
    }

    /**
     * Parses an "expression" with following production.
     * <p>
     * expression -> term addExpression
     * <p/>
     * Since we have
     * <p>
     * addExpression -> + term addExpression | Empty
     * <p/>
     * So when lexer.match(Lexer.PLUS) is true, addExpression() will be called recursively, it is equal to
     * while (lexer.match(Lexer.PLUS))
     * {
     *     lexer.lookNextToken();
     *     term();
     * }
     * */
    private void expression()
    {
        term();

        while (lexer.match(TokenType.PLUS))
        {
            lexer.lookNextToken();
            term();
        }

        if (lexer.match(TokenType.UNKNOWN_SYMBOL))
        {
            isLegalStatement = false;
            System.out.println("Line " + lexer.getLineNumber() + ", unknown symbol \"" + lexer.getSymbolText() + "\"");
            return;
        }
        else
            return;
    }

    /**
     * Parses an "term" with following production.
     * <p>
     * term -> factor * term
     * <p/>
     * */
    private void term()
    {
        factor();

        while (lexer.match(TokenType.TIMES))
        {
            lexer.lookNextToken();
            factor();
        }
    }

    /**
     * Parses a "factor" with following production.
     * <p>
     * factor -> NUMBER_OR_ID | ( expression )
     * <p/>
     * */
    private void factor()
    {
        if (lexer.match(TokenType.NUMBER_OR_IDENTIFIER))
            lexer.lookNextToken();
        else if (lexer.match(TokenType.LEFT_PARENTHESES))
        {
            lexer.lookNextToken();
            expression();

            if (lexer.match(TokenType.RIGHT_PARENTHESES))
                lexer.lookNextToken();
            else
            {
                // There is a "(" without a ")".
                isLegalStatement = false;
                System.out.println("Line: " + lexer.getLineNumber() + ", missing \")\"");
            }
        }
        else
        {
            // Not number neither identifier.
            isLegalStatement = false;
            System.out.println("Line: " + lexer.getLineNumber() + ", unexpected symbol: \"" + lexer.getSymbolText() + "\".");
        }
    }

    /**
     * A unit test method for the {@link ImprovedParser} class.
     * */
    public static void main(String[] args) throws IOException
    {
        String sourceFilePath = "./out/production/CCompiler/expression/parserTest.txt";
        FileInputStream sourceFile = new FileInputStream(sourceFilePath);
        Lexer lexer = new Lexer(sourceFile);
        ImprovedParser parser = new ImprovedParser(lexer);
        parser.statements();
        lexer.close();
        sourceFile.close();
    }
}
