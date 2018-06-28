package expression;

public class ImprovedParser
{
    private Lexer lexer;
    private boolean isLegalStatement;

    public ImprovedParser(Lexer lexer)
    {
        isLegalStatement = true;
        this.lexer = lexer;
    }

    /**
     * statements -> expression; | expression; statements
     * */
    public void statements()
    {
        while (!lexer.match(Lexer.EOI))
        {
            expression();

            if (lexer.match(Lexer.SEMICOLON))
                lexer.getNextToken();
            else
            {
                isLegalStatement = false;
                System.out.println("Line " + lexer.getLineNumber() + ", missing \";\"");
            }
        }

        if (isLegalStatement)
            System.out.println("The statement is legal.");
    }

    /**
     * expression -> term expressionPrime
     * expression -> PLUS term expressionPrime | Empty
     * */
    private void expression()
    {
        term();

        while (lexer.match(Lexer.PLUS))
        {
            lexer.getNextToken();
            term();
        }

        if (lexer.match(Lexer.UNKNOWN_SYMBOL))
        {
            isLegalStatement = false;
            System.out.println("Line " + lexer.getLineNumber() + ", unknown symbol \"" + lexer.getSymbolText() + "\"");
            return;
        }
        else
            return;
    }

    /**
     * expressionPrime can be reduced to expression().
     * Since when lexer.match(Lexer.PLUS) is true, expressionPrime() will be called recursively, it is equal to
     * while (lexer.match(Lexer.PLUS))
     * {
     *     lexer.getNextToken();
     *     term();
     * }
     * */
//    private void expressionPrime()
//    {
//        if (lexer.match(Lexer.PLUS))
//        {
//            lexer.getNextToken();
//            term();
//            expressionPrime();
//        }
//        else if (lexer.match(Lexer.UNKNOWN_SYMBOL))
//        {
//            isLegalStatement = false;
//            System.out.println("Line " + lexer.getLineNumber() + ", unknown symbol \"" + lexer.getSymbolText() + "\"");
//            return;
//        }
//        else
//            return;
//    }

    private void term()
    {
        factor();

        while (lexer.match(Lexer.TIMES))
        {
            lexer.getNextToken();
            factor();
        }
    }

    /**
     * termPrime can be reduced into term() like the reason above.
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
     * factor -> NUMBER_OR_IDENTIFIER | LEFT_PARENTHESES expression | RIGHT_PARENTHESES
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
                // There is a "(" without a ")".
                isLegalStatement = false;
                System.out.println("Line: " + lexer.getLineNumber() + ", missing \")\"");
                return;
            }
        }
        else
        {
            // Not number neither identifier.

            isLegalStatement = false;
            System.out.println("Line: " + lexer.getLineNumber() + ", unexpected symbol: \"" + lexer.getSymbolText() + "\"");
            return;
        }
    }

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        ImprovedParser parser = new ImprovedParser(lexer);
        parser.statements();
    }
}
