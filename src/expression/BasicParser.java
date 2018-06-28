package expression;

public class BasicParser
{
    private Lexer lexer;
    private boolean isLegalExpression;

    public BasicParser(Lexer lexer)
    {
        isLegalExpression = true;
        this.lexer = lexer;
    }

    /**
     * statements -> expression ; | expression ; statements
     * */
    public void statement()
    {
        expression();

        // Read next token.
        // If the next token following semicolon is not EOI, then use the parse rule on the right.
        if (lexer.match(Lexer.SEMICOLON))
            lexer.getNextToken();
        else
        {
            // If the expression doesn't end with a semicolon, then it is a legal statement.

            isLegalExpression = false;
            System.out.println("Line " + lexer.getLineNumber() + ": missing semicolon.");
            return;
        }

        // If there are still contents un-parsed, continue to parse.
        if (!lexer.match(Lexer.EOI))
            statement();

        if (isLegalExpression)
            System.out.println("The statement is legal");
    }

    /**
     * expression -> term addExpression
     * */
    public void expression()
    {
        term();
        addExpression();
    }

    /**
     * addExpression -> PLUS term addExpression | Empty
     * */
    private void addExpression()
    {
        if (lexer.match(Lexer.PLUS))
        {
            lexer.getNextToken();
            term();
            addExpression();
        }
        else if (lexer.match(Lexer.UNKNOWN_SYMBOL))
        {
            isLegalExpression = false;
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
     * term -> factor termPrime
     * */
    private void term()
    {
        factor();
        termPrime();
    }

    /**
     * termPrime -> * factor termPrime | Empty
     * */
    private void termPrime()
    {
        if (lexer.match(Lexer.TIMES))
        {
            lexer.getNextToken();
            factor();
            termPrime();
        }
        else
            return;
    }

    /**
     * factor -> NUMBER_OR_IDENTIFIER | LEFT_PARENTHESES Expression RIGHT_PARENTHESES
     * */
    private void factor()
    {
        if (lexer.match(Lexer.NUMBER_OR_IDENTIFIER))
            lexer.getNextToken();
        else if (lexer.match(Lexer.LEFT_PARENTHESES))
        {
            lexer.getNextToken();
            expression();
            if (lexer.match(Lexer.RIGHT_PARENTHESES))
                lexer.getNextToken();
            else
            {
                isLegalExpression = false;
                System.out.println("Line " + lexer.getLineNumber() + ": missing )");
                return;
            }
        }
        else
        {
            isLegalExpression = false;
            System.out.println("Line " + lexer.getLineNumber() + ": unexpected symbol \"" + lexer.getSymbolText() + "\"");
            return;
        }
    }

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        BasicParser basicParser = new BasicParser(lexer);
        basicParser.statement();
    }
}