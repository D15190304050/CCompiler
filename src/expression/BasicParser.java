package expression;

import java.io.*;

/**
 * The {@link BasicParser} class represents a simple parser that supports expressions containing only numbers, operators
 * such as "+", "*" and parentheses and ending with ";".
 * @implNote This is a implementation which complies with the syntax production strictly.
 * */
public class BasicParser
{
    /** The lexer to tokenize text in the input stream. */
    private Lexer lexer;

    /** A boolean value indicating whether the statements is legal. */
    private boolean isLegalStatement;

    /**
     * Initializes a new instance of {@link BasicParser} with the specified {@link Lexer}.
     * @param lexer The specified {@link Lexer}.
     * */
    public BasicParser(Lexer lexer)
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

        // If there are still contents un-parsed in the input stream, continue to parse.
        if (!lexer.match(TokenType.END_OF_FILE))
            statements();
    }

    /**
     * Parses an "expression" with following production.
     * <p>
     * expression -> term addExpression
     * <p/>
     * */
    private void expression()
    {
        term();
        addExpression();
    }

    /**
     * Parses an "addExpression" with following production.
     * <p>
     * addExpression -> + term addExpression | Empty
     * <p/>
     * */
    private void addExpression()
    {
        if (lexer.match(TokenType.PLUS))
        {
            lexer.lookNextToken();
            term();
            addExpression();
        }
        else if (lexer.match(TokenType.UNKNOWN_SYMBOL))
        {
            isLegalStatement = false;
            System.out.println("Line " + lexer.getLineNumber() + ": unknown symbol: " + lexer.getSymbolText());
            return;
        }
        else
        {
            // Return if it is empty.
            return;
        }
    }

    /**
     * Parses a "term" with following production.
     * <p>
     * term -> factor subMultiplicationTerm
     * <p/>
     * */
    private void term()
    {
        factor();
        subMultiplicationTerm();
    }

    /**
     * Parses a "subMultiplicationTerm" with following production.
     * <p>
     * subMultiplicationTerm -> * factor subMultiplicationTerm | Empty
     * <p/>
     * */
    private void subMultiplicationTerm()
    {
        if (lexer.match(TokenType.TIMES))
        {
            lexer.lookNextToken();
            factor();
            subMultiplicationTerm();
        }
        else
            return;
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
                isLegalStatement = false;
                System.out.println("Line " + lexer.getLineNumber() + ": missing \")\"");
                return;
            }
        }
        else
        {
            isLegalStatement = false;
            System.out.println("Line " + lexer.getLineNumber() + ": unexpected symbol \"" + lexer.getSymbolText() + "\"");
            return;
        }
    }

    /**
     * A unit test method for the {@link BasicParser} class.
     * */
    public static void main(String[] args) throws IOException
    {
        String sourceFilePath = "./out/production/CCompiler/expression/parserTest.txt";
        FileInputStream sourceFile = new FileInputStream(sourceFilePath);
        Lexer lexer = new Lexer(sourceFile);
        BasicParser parser = new BasicParser(lexer);
        parser.statements();
        lexer.close();
        sourceFile.close();
    }
}